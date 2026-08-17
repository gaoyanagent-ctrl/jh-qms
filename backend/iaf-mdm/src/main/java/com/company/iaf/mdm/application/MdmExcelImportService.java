package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class MdmExcelImportService {
    private static final int MAX_ROWS = 1000;
    private final MdmApplicationService mdm;

    public MdmExcelImportService(MdmApplicationService mdm) { this.mdm = mdm; }

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
    public MdmDtos.ImportPreview preview(long tenantId, String modelCode, MultipartFile file) {
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
            return new MdmDtos.ImportPreview(file.getOriginalFilename(), parsed.records(), new MdmDtos.BatchValidationResult(checked.valid(), checked.total(), rows));
        } catch (BusinessException failure) { throw failure;
        } catch (Exception failure) { throw invalid("无法读取 Excel 文件，请使用系统模板并确认文件未损坏"); }
    }

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
    private record Column(String name, String code, String type, boolean required, List<String> options, String help) {}
    private record ParsedRows(List<MdmDtos.SaveRecordRequest> records, List<Integer> rowNumbers) {}
}
