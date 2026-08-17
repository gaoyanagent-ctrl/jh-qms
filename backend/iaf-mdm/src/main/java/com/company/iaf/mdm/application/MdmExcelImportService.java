package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.domain.repository.MdmImportObjectStorage;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class MdmExcelImportService {
    private static final int MAX_ROWS = 1000;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private final MdmApplicationService mdm;
    private final MdmRepository repository;
    private final MdmImportObjectStorage storage;

    public MdmExcelImportService(MdmApplicationService mdm, MdmRepository repository, MdmImportObjectStorage storage) { this.mdm = mdm; this.repository = repository; this.storage = storage; }

    @RequiresPermission("mdm:record:view")
    public byte[] template(long tenantId, String modelCode) {
        MdmModels.Model model = mdm.schema(tenantId, modelCode);
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var data = workbook.createSheet("导入数据");
            var header = data.createRow(0);
            var columns = columns(model);
            var style = headerStyle(workbook);
            for (int i = 0; i < columns.size(); i++) {
                var cell = header.createCell(i); cell.setCellValue(columns.get(i).name()); cell.setCellStyle(style);
                data.setColumnWidth(i, Math.min(50, Math.max(14, columns.get(i).name().length() * 3)) * 256);
            }
            addEnumValidations(data, columns);
            var help = workbook.createSheet("字段说明");
            var helpHeader = help.createRow(0);
            List.of("字段名称", "字段编码", "类型", "必填", "可选值", "说明").forEach(value -> helpHeader.createCell(helpHeader.getLastCellNum() < 0 ? 0 : helpHeader.getLastCellNum()).setCellValue(value));
            for (int i = 0; i < columns.size(); i++) {
                var column = columns.get(i); var row = help.createRow(i + 1);
                row.createCell(0).setCellValue(column.name()); row.createCell(1).setCellValue(column.code());
                row.createCell(2).setCellValue(column.type()); row.createCell(3).setCellValue(column.required() ? "是" : "否");
                row.createCell(4).setCellValue(String.join("、", column.options())); row.createCell(5).setCellValue(column.help() == null ? "" : column.help());
            }
            for (int i = 0; i < 6; i++) help.autoSizeColumn(i);
            workbook.write(output); return output.toByteArray();
        } catch (Exception failure) {
            throw new BusinessException(MdmErrorCode.IMPORT_FILE_INVALID, "Unable to generate import template");
        }
    }

    @RequiresPermission("mdm:record:create")
    @Transactional
    public MdmDtos.ImportPreview preview(long tenantId, long actorId, String modelCode, MultipartFile file) {
        validateFile(file);
        MdmModels.Model model = mdm.schema(tenantId, modelCode);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("导入数据"); if (sheet == null) sheet = workbook.getSheetAt(0);
            ParsedRows parsed = parse(sheet, model);
            if (parsed.records().isEmpty()) throw invalid("导入文件没有数据行");
            if (parsed.records().size() > MAX_ROWS) throw invalid("同步预检查最多支持 1000 行");
            var request = new MdmDtos.BatchRecordRequest(parsed.records());
            var checked = mdm.validateBatch(tenantId, modelCode, request);
            var rows = new ArrayList<MdmDtos.BatchRowValidation>();
            for (int i = 0; i < checked.rows().size(); i++) {
                var row = checked.rows().get(i);
                rows.add(new MdmDtos.BatchRowValidation(parsed.rowNumbers().get(i), row.businessCode(), row.valid(), row.errors()));
            }
            var validation = new MdmDtos.BatchValidationResult(checked.valid(), checked.total(), rows);
            var task = persistTask(tenantId, actorId, model, file, parsed.records(), validation);
            return new MdmDtos.ImportPreview(task.id(), task.status(), file.getOriginalFilename(), parsed.records(), validation);
        } catch (BusinessException failure) { throw failure;
        } catch (Exception failure) { throw invalid("无法读取 Excel 文件，请使用系统模板并确认文件未损坏"); }
    }

    @RequiresPermission("mdm:record:view")
    @Transactional(readOnly = true)
    public MdmDtos.ImportDownload sourceFile(long tenantId,String modelCode,UUID taskId){var model=mdm.schema(tenantId,modelCode);requireTask(tenantId,model.id(),taskId);var artifact=repository.findImportArtifact(tenantId,model.id(),taskId).orElseThrow(()->new BusinessException(MdmErrorCode.IMPORT_ARTIFACT_NOT_FOUND));try(var input=storage.get(artifact.objectKey())){return new MdmDtos.ImportDownload(input.readAllBytes(),artifact.fileName(),artifact.mediaType());}catch(BusinessException failure){throw failure;}catch(Exception failure){throw new BusinessException(MdmErrorCode.IMPORT_STORAGE_FAILED);}}

    @RequiresPermission("mdm:record:view")
    @Transactional(readOnly = true)
    public MdmDtos.ImportDownload resultFile(long tenantId,String modelCode,UUID taskId){var model=mdm.schema(tenantId,modelCode);var task=requireTask(tenantId,model.id(),taskId);var validation=repository.findImportTaskValidation(tenantId,model.id(),taskId);try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()){var sheet=workbook.createSheet("校验结果");var header=sheet.createRow(0);List.of("Excel 行","业务编码","结果","错误信息").forEach(value->header.createCell(header.getLastCellNum()<0?0:header.getLastCellNum()).setCellValue(value));for(int index=0;index<validation.rows().size();index++){var item=validation.rows().get(index);var row=sheet.createRow(index+1);row.createCell(0).setCellValue(item.rowNo());row.createCell(1).setCellValue(item.businessCode());row.createCell(2).setCellValue(item.valid()?"通过":"错误");row.createCell(3).setCellValue(String.join("；",item.errors()));}for(int i=0;i<4;i++)sheet.autoSizeColumn(i);workbook.write(output);return new MdmDtos.ImportDownload(output.toByteArray(),stripExtension(task.fileName())+"-校验结果.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");}catch(Exception failure){throw new BusinessException(MdmErrorCode.IMPORT_FILE_INVALID,"Unable to generate import result");}}

    @RequiresPermission("mdm:record:view")
    @Transactional(readOnly = true)
    public List<MdmModels.ImportTask> tasks(long tenantId, String modelCode) {
        var model=mdm.schema(tenantId,modelCode); return repository.findImportTasks(tenantId,model.id());
    }

    @RequiresPermission("mdm:record:view")
    @Transactional(readOnly = true)
    public MdmDtos.BatchValidationResult errors(long tenantId, String modelCode, UUID taskId) {
        var model=mdm.schema(tenantId,modelCode); requireTask(tenantId,model.id(),taskId); return repository.findImportTaskValidation(tenantId,model.id(),taskId);
    }

    @RequiresPermission("mdm:record:create")
    @Transactional
    public MdmModels.ImportTask commit(long tenantId,long actorId,String modelCode,UUID taskId) {
        var model=mdm.schema(tenantId,modelCode); var task=requireTask(tenantId,model.id(),taskId);
        if("COMMITTED".equals(task.status())) return task;
        if(!"READY".equals(task.status())) throw new BusinessException(MdmErrorCode.IMPORT_TASK_NOT_READY);
        var records=repository.findImportTaskRecords(tenantId,model.id(),taskId);
        var validation=mdm.validateBatch(tenantId,modelCode,new MdmDtos.BatchRecordRequest(records));
        if(!validation.valid()) { repository.refreshImportTaskValidation(tenantId,model.id(),taskId,actorId,validation); return requireTask(tenantId,model.id(),taskId); }
        if(!repository.claimImportTask(tenantId,model.id(),taskId,actorId)) throw new BusinessException(MdmErrorCode.IMPORT_TASK_NOT_READY);
        var created=mdm.createBatch(tenantId,actorId,modelCode,new MdmDtos.BatchRecordRequest(records));
        repository.completeImportTask(tenantId,model.id(),taskId,actorId,created.size()); return requireTask(tenantId,model.id(),taskId);
    }

    private MdmModels.ImportTask requireTask(long tenantId,long modelId,UUID taskId){return repository.findImportTask(tenantId,modelId,taskId).orElseThrow(()->new BusinessException(MdmErrorCode.IMPORT_TASK_NOT_FOUND));}

    private ParsedRows parse(Sheet sheet, MdmModels.Model model) {
        Row header = sheet.getRow(sheet.getFirstRowNum()); if (header == null) throw invalid("缺少表头");
        var formatter = new DataFormatter(); var evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Map<String,Integer> indexes = new HashMap<>();
        for (Cell cell : header) indexes.put(formatter.formatCellValue(cell, evaluator).trim(), cell.getColumnIndex());
        Integer business = index(indexes, "业务编码", "businessCode"), name = index(indexes, "名称", "name");
        if (business == null || name == null) throw invalid("表头必须包含“业务编码”和“名称”");
        List<MdmDtos.SaveRecordRequest> result = new ArrayList<>(); List<Integer> rowNumbers = new ArrayList<>();
        for (int rowNo = header.getRowNum() + 1; rowNo <= sheet.getLastRowNum(); rowNo++) {
            Row row = sheet.getRow(rowNo); if (row == null || blank(row, formatter, evaluator)) continue;
            Map<String,Object> attributes = new LinkedHashMap<>();
            for (var field : model.fields()) {
                Integer position = index(indexes, field.name(), field.code());
                if (position != null) attributes.put(field.code(), value(row.getCell(position), field.dataType(), formatter, evaluator));
            }
            result.add(new MdmDtos.SaveRecordRequest(text(row, business, formatter, evaluator), text(row, name, formatter, evaluator),
                    "DRAFT", "GROUP", List.of(), null, null, attributes, null, "Excel 文件导入"));
            rowNumbers.add(rowNo + 1);
        }
        return new ParsedRows(result, rowNumbers);
    }

    private Object value(Cell cell, String type, DataFormatter formatter, FormulaEvaluator evaluator) {
        String raw = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim(); if (raw.isEmpty()) return null;
        try {
            return switch (type) {
                case "INTEGER" -> cell != null && cell.getCellType() == CellType.NUMERIC ? (long) cell.getNumericCellValue() : Long.valueOf(raw);
                case "DECIMAL" -> cell != null && cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : Double.valueOf(raw);
                case "BOOLEAN" -> List.of("true", "1", "是", "yes").contains(raw.toLowerCase(Locale.ROOT));
                case "DATE" -> cell != null && DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() : LocalDate.parse(raw).toString();
                default -> raw;
            };
        } catch (RuntimeException ignored) { return raw; }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("请选择 Excel 文件");
        if (file.getSize() > MAX_FILE_SIZE) throw invalid("Excel 文件不能超过 20 MB");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) throw invalid("仅支持 .xlsx 或 .xls 文件");
    }
    private List<Column> columns(MdmModels.Model model) {
        var result = new ArrayList<Column>(); result.add(new Column("业务编码", "businessCode", "STRING", true, List.of(), "主数据业务唯一编码")); result.add(new Column("名称", "name", "STRING", true, List.of(), "主数据名称"));
        model.fields().forEach(field -> result.add(new Column(field.name(), field.code(), field.dataType(), field.required(), field.enumOptions(), field.helpText()))); return result;
    }
    private void addEnumValidations(XSSFSheet sheet, List<Column> columns) {
        var helper = new XSSFDataValidationHelper(sheet);
        for (int i = 0; i < columns.size(); i++) if (!columns.get(i).options().isEmpty() && String.join(",", columns.get(i).options()).length() < 255) {
            var validation = helper.createValidation(helper.createExplicitListConstraint(columns.get(i).options().toArray(String[]::new)), new CellRangeAddressList(1, MAX_ROWS, i, i)); validation.setShowErrorBox(true); sheet.addValidationData(validation);
        }
    }
    private CellStyle headerStyle(Workbook workbook) { var style = workbook.createCellStyle(); var font = workbook.createFont(); font.setBold(true); style.setFont(font); style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); return style; }
    private boolean blank(Row row, DataFormatter formatter, FormulaEvaluator evaluator) { for (Cell cell : row) if (!formatter.formatCellValue(cell, evaluator).trim().isEmpty()) return false; return true; }
    private String text(Row row, int index, DataFormatter formatter, FormulaEvaluator evaluator) { Cell cell = row.getCell(index); return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim(); }
    private Integer index(Map<String,Integer> indexes, String... names) { for (String name : names) if (indexes.containsKey(name)) return indexes.get(name); return null; }
    private BusinessException invalid(String message) { return new BusinessException(MdmErrorCode.IMPORT_FILE_INVALID, message); }
    private MdmModels.ImportTask persistTask(long tenantId,long actorId,MdmModels.Model model,MultipartFile file,List<MdmDtos.SaveRecordRequest> records,MdmDtos.BatchValidationResult validation){String original=Optional.ofNullable(file.getOriginalFilename()).orElse("import.xlsx");String extension=original.toLowerCase(Locale.ROOT).endsWith(".xls")?"xls":"xlsx";String key=tenantId+"/mdm-imports/"+UUID.randomUUID()+"/source."+extension;String media=extension.equals("xls")?"application/vnd.ms-excel":"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";try(var input=file.getInputStream()){storage.put(key,input,file.getSize(),media);try{return repository.insertImportTask(tenantId,actorId,model.id(),model.code(),original,records,validation,key,media,file.getSize());}catch(RuntimeException failure){storage.delete(key);throw failure;}}catch(BusinessException failure){throw failure;}catch(Exception failure){storage.delete(key);throw new BusinessException(MdmErrorCode.IMPORT_STORAGE_FAILED);}}
    private String stripExtension(String name){int position=name.lastIndexOf('.');return position>0?name.substring(0,position):name;}
    private record Column(String name, String code, String type, boolean required, List<String> options, String help) {}
    private record ParsedRows(List<MdmDtos.SaveRecordRequest> records, List<Integer> rowNumbers) {}
}
