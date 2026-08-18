package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name="iaf.mdm.enabled",havingValue="true",matchIfMissing=true)
public class MdmModelDictionaryExcelService {
    private static final long MAX_FILE_SIZE=20L*1024*1024;
    private static final Set<String> TYPES=Set.of("STRING","TEXT","INTEGER","DECIMAL","BOOLEAN","DATE","DATETIME","ENUM","REFERENCE");
    private final MdmRepository repository;
    public MdmModelDictionaryExcelService(MdmRepository repository){this.repository=repository;}

    @RequiresPermission("mdm:model:view")
    public byte[] template(){
        try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()){
            sheet(workbook,"模型定义",List.of("数据域编码","模型编码","模型名称","记录类型","数据版本","生效日期","组织范围","记录审批"),List.of("manufacturing","material","物料主数据","MASTER","是","否","否","否"));
            sheet(workbook,"字段定义",List.of("模型编码","字段编码","字段名称","数据类型","最大长度","必填","唯一","只读","可搜索","可排序","列表显示","帮助说明","排序号"),List.of("material","materialType","物料类型","ENUM","32","是","否","否","是","是","是","物料分类","10"));
            sheet(workbook,"枚举选项",List.of("模型编码","字段编码","选项值","显示名称","排序号"),List.of("material","materialType","RAW","原材料","10"));
            sheet(workbook,"关联定义",List.of("模型编码","字段编码","目标模型编码","值字段编码","显示字段编码","状态字段编码","允许状态"),List.of());
            sheet(workbook,"布局配置",List.of("模型编码","视图","分区编码","分区名称","字段编码","排序号"),List.of());
            sheet(workbook,"校验规则",List.of("模型编码","规则编码","规则名称","当前字段","目标模型编码","触发时机","异常策略","提示文案"),List.of());
            workbook.write(output);return output.toByteArray();
        }catch(Exception failure){throw new BusinessException(MdmErrorCode.IMPORT_FILE_INVALID,"Unable to generate model dictionary template");}
    }

    @RequiresPermission("mdm:model:view") @Transactional(readOnly=true)
    public MdmDtos.ModelDictionaryPreview preview(long tenantId,MultipartFile file){
        validateFile(file);
        try(var workbook=WorkbookFactory.create(file.getInputStream())){
            var issues=new ArrayList<MdmDtos.ModelDictionaryIssue>();
            var models=parseModels(workbook,issues);var fields=parseFields(workbook,issues);
            applyEnums(workbook,fields,issues);applyReferences(workbook,fields,issues);
            var currentModels=repository.findModels(tenantId);validateDefinitions(models,fields,currentModels,issues);
            var existing=currentModels.stream().collect(Collectors.toMap(MdmModels.Model::code,Function.identity()));
            var changes=new ArrayList<MdmDtos.ModelDictionaryChange>();
            for(var model:models.values()){
                var current=existing.get(model.code);int adds=0,updates=0,unchanged=0;
                var currentFields=current==null?Map.<String,MdmModels.Field>of():current.fields().stream().collect(Collectors.toMap(MdmModels.Field::code,Function.identity()));
                for(var field:fields.getOrDefault(model.code,List.of())){var old=currentFields.get(field.code);if(old==null)adds++;else if(field.same(old))unchanged++;else updates++;}
                var own=issues.stream().filter(item->model.code.equals(item.field())||item.field().startsWith(model.code+".")).toList();
                changes.add(new MdmDtos.ModelDictionaryChange(model.code,model.name,current==null?"CREATE":"UPDATE",current==null?null:current.status(),adds,updates,unchanged,own));
            }
            int creates=(int)changes.stream().filter(item->"CREATE".equals(item.changeType())).count();
            int totalFields=fields.values().stream().mapToInt(List::size).sum();
            return new MdmDtos.ModelDictionaryPreview(issues.isEmpty(),models.size(),totalFields,creates,changes.size()-creates,changes,issues);
        }catch(BusinessException failure){throw failure;}catch(Exception failure){throw invalid("无法读取模型数据字典，请使用系统模板并确认文件未损坏");}
    }

    private LinkedHashMap<String,ModelRow> parseModels(Workbook workbook,List<MdmDtos.ModelDictionaryIssue> issues){
        var result=new LinkedHashMap<String,ModelRow>();var sheet=requireSheet(workbook,"模型定义");var columns=columns(sheet);
        for(int index=1;index<=sheet.getLastRowNum();index++){var row=sheet.getRow(index);if(blank(row))continue;String domain=text(row,columns,"数据域编码"),code=text(row,columns,"模型编码"),name=text(row,columns,"模型名称");
            if(domain.isBlank())issue(issues,"模型定义",index+1,code,"数据域编码不能为空");if(!code.matches("^[a-z][A-Za-z0-9_]{1,63}$"))issue(issues,"模型定义",index+1,code,"模型编码格式错误");if(name.isBlank())issue(issues,"模型定义",index+1,code,"模型名称不能为空");
            if(result.putIfAbsent(code,new ModelRow(code,name,domain,textOr(row,columns,"记录类型","MASTER")))!=null)issue(issues,"模型定义",index+1,code,"模型编码重复");}
        if(result.isEmpty())throw invalid("模型定义 Sheet 没有数据行");return result;
    }

    private Map<String,List<FieldRow>> parseFields(Workbook workbook,List<MdmDtos.ModelDictionaryIssue> issues){
        var result=new LinkedHashMap<String,List<FieldRow>>();var seen=new HashSet<String>();var sheet=requireSheet(workbook,"字段定义");var columns=columns(sheet);
        for(int index=1;index<=sheet.getLastRowNum();index++){var row=sheet.getRow(index);if(blank(row))continue;String model=text(row,columns,"模型编码"),code=text(row,columns,"字段编码"),name=text(row,columns,"字段名称"),type=text(row,columns,"数据类型").toUpperCase(Locale.ROOT),key=model+"."+code;
            if(!code.matches("^[a-z][A-Za-z0-9_]{1,63}$"))issue(issues,"字段定义",index+1,key,"字段编码格式错误");if(name.isBlank())issue(issues,"字段定义",index+1,key,"字段名称不能为空");if(!TYPES.contains(type))issue(issues,"字段定义",index+1,key,"不支持的数据类型: "+type);if(!seen.add(key))issue(issues,"字段定义",index+1,key,"同一模型内字段编码重复");
            result.computeIfAbsent(model,ignored->new ArrayList<>()).add(new FieldRow(model,code,name,type,integer(row,columns,"最大长度"),yes(row,columns,"必填"),yes(row,columns,"唯一"),yes(row,columns,"只读"),yes(row,columns,"可搜索"),yes(row,columns,"可排序"),yes(row,columns,"列表显示"),text(row,columns,"帮助说明"),integerOr(row,columns,"排序号",(index+1)*10)));}
        return result;
    }

    private void applyEnums(Workbook workbook,Map<String,List<FieldRow>> fields,List<MdmDtos.ModelDictionaryIssue> issues){var sheet=workbook.getSheet("枚举选项");if(sheet==null)return;var columns=columns(sheet);for(int index=1;index<=sheet.getLastRowNum();index++){var row=sheet.getRow(index);if(blank(row))continue;String model=text(row,columns,"模型编码"),code=text(row,columns,"字段编码"),value=text(row,columns,"选项值");var field=find(fields,model,code);if(field==null)issue(issues,"枚举选项",index+1,model+"."+code,"找不到对应字段");else if(value.isBlank())issue(issues,"枚举选项",index+1,model+"."+code,"选项值不能为空");else field.enums.add(value);}}
    private void applyReferences(Workbook workbook,Map<String,List<FieldRow>> fields,List<MdmDtos.ModelDictionaryIssue> issues){var sheet=workbook.getSheet("关联定义");if(sheet==null)return;var columns=columns(sheet);for(int index=1;index<=sheet.getLastRowNum();index++){var row=sheet.getRow(index);if(blank(row))continue;String model=text(row,columns,"模型编码"),code=text(row,columns,"字段编码");var field=find(fields,model,code);if(field==null)issue(issues,"关联定义",index+1,model+"."+code,"找不到对应字段");else field.reference=new ReferenceRow(text(row,columns,"目标模型编码"),text(row,columns,"值字段编码"),text(row,columns,"显示字段编码"),text(row,columns,"状态字段编码"),split(text(row,columns,"允许状态")));}}

    private void validateDefinitions(Map<String,ModelRow> models,Map<String,List<FieldRow>> fields,List<MdmModels.Model> existing,List<MdmDtos.ModelDictionaryIssue> issues){
        var modelCodes=new HashSet<>(models.keySet());existing.forEach(item->modelCodes.add(item.code()));var fieldCodes=new HashMap<String,Set<String>>();
        existing.forEach(item->{var codes=item.fields().stream().map(MdmModels.Field::code).collect(Collectors.toCollection(HashSet::new));codes.addAll(Set.of("businessCode","name","lifecycleStatus"));fieldCodes.put(item.code(),codes);});fields.forEach((model,list)->{var codes=list.stream().map(item->item.code).collect(Collectors.toCollection(HashSet::new));codes.addAll(Set.of("businessCode","name","lifecycleStatus"));fieldCodes.put(model,codes);});
        fields.forEach((model,list)->{if(!models.containsKey(model))issue(issues,"字段定义",0,model,"字段引用了未在模型定义中声明的模型");for(var field:list){String key=model+"."+field.code;if("ENUM".equals(field.type)&&field.enums.isEmpty())issue(issues,"枚举选项",0,key,"ENUM 字段至少需要一个选项");if("REFERENCE".equals(field.type)){if(field.reference==null)issue(issues,"关联定义",0,key,"REFERENCE 字段缺少关联定义");else{var targets=fieldCodes.getOrDefault(field.reference.targetModel,Set.of());if(!modelCodes.contains(field.reference.targetModel)||!targets.contains(field.reference.valueField)||!targets.contains(field.reference.displayField)||(!field.reference.statusField.isBlank()&&!targets.contains(field.reference.statusField)))issue(issues,"关联定义",0,key,"目标模型或关联字段不存在");}}else if(field.reference!=null)issue(issues,"关联定义",0,key,"只有 REFERENCE 字段可以配置关联");}});
    }

    private void validateFile(MultipartFile file){if(file==null||file.isEmpty())throw invalid("请选择 Excel 文件");if(file.getSize()>MAX_FILE_SIZE)throw invalid("文件不能超过 20MB");String name=Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);if(!name.endsWith(".xlsx")&&!name.endsWith(".xls"))throw invalid("只支持 .xlsx 或 .xls 文件");}
    private Sheet requireSheet(Workbook workbook,String name){var sheet=workbook.getSheet(name);if(sheet==null)throw invalid("缺少“"+name+"”Sheet");return sheet;}
    private Map<String,Integer> columns(Sheet sheet){var header=sheet.getRow(0);if(header==null)throw invalid(sheet.getSheetName()+" 缺少表头");var result=new HashMap<String,Integer>();for(var cell:header)result.put(new DataFormatter().formatCellValue(cell).trim(),cell.getColumnIndex());return result;}
    private void sheet(Workbook workbook,String name,List<String> headers,List<String> example){var value=workbook.createSheet(name);var header=value.createRow(0);var style=workbook.createCellStyle();var font=workbook.createFont();font.setBold(true);style.setFont(font);for(int i=0;i<headers.size();i++){var cell=header.createCell(i);cell.setCellValue(headers.get(i));cell.setCellStyle(style);value.setColumnWidth(i,Math.min(36,Math.max(14,headers.get(i).length()*3))*256);}if(!example.isEmpty()){var row=value.createRow(1);for(int i=0;i<example.size();i++)row.createCell(i).setCellValue(example.get(i));}value.createFreezePane(0,1);}
    private String text(Row row,Map<String,Integer> columns,String name){var index=columns.get(name);return index==null?"":new DataFormatter().formatCellValue(row.getCell(index)).trim();}
    private String textOr(Row row,Map<String,Integer> columns,String name,String fallback){var value=text(row,columns,name);return value.isBlank()?fallback:value;}
    private boolean yes(Row row,Map<String,Integer> columns,String name){return Set.of("是","Y","YES","TRUE","1").contains(text(row,columns,name).toUpperCase(Locale.ROOT));}
    private Integer integer(Row row,Map<String,Integer> columns,String name){String value=text(row,columns,name);if(value.isBlank())return null;try{return (int)Double.parseDouble(value);}catch(Exception ignored){return null;}}
    private int integerOr(Row row,Map<String,Integer> columns,String name,int fallback){var value=integer(row,columns,name);return value==null?fallback:value;}
    private boolean blank(Row row){if(row==null)return true;var formatter=new DataFormatter();for(var cell:row)if(!formatter.formatCellValue(cell).trim().isEmpty())return false;return true;}
    private List<String> split(String value){return value.isBlank()?List.of():Arrays.stream(value.split("[,，;；|]")).map(String::trim).filter(item->!item.isEmpty()).toList();}
    private FieldRow find(Map<String,List<FieldRow>> fields,String model,String code){return fields.getOrDefault(model,List.of()).stream().filter(item->item.code.equals(code)).findFirst().orElse(null);}
    private void issue(List<MdmDtos.ModelDictionaryIssue> issues,String sheet,int row,String field,String message){issues.add(new MdmDtos.ModelDictionaryIssue(sheet,row,field,"ERROR",message));}
    private BusinessException invalid(String message){return new BusinessException(MdmErrorCode.IMPORT_FILE_INVALID,message);}
    private record ModelRow(String code,String name,String domain,String recordType){}
    private static final class FieldRow{final String model,code,name,type,help;final Integer length;final boolean required,unique,readonly,searchable,sortable,listVisible;final int sortNo;final List<String> enums=new ArrayList<>();ReferenceRow reference;FieldRow(String model,String code,String name,String type,Integer length,boolean required,boolean unique,boolean readonly,boolean searchable,boolean sortable,boolean listVisible,String help,int sortNo){this.model=model;this.code=code;this.name=name;this.type=type;this.length=length;this.required=required;this.unique=unique;this.readonly=readonly;this.searchable=searchable;this.sortable=sortable;this.listVisible=listVisible;this.help=help;this.sortNo=sortNo;}boolean same(MdmModels.Field old){return name.equals(old.name())&&type.equals(old.dataType())&&Objects.equals(length,old.length())&&required==old.required()&&unique==old.unique()&&readonly==old.readonly()&&searchable==old.searchable()&&sortable==old.sortable()&&listVisible==old.listVisible()&&Objects.equals(help,Objects.toString(old.helpText(),""))&&sortNo==old.sortNo()&&new HashSet<>(enums).equals(new HashSet<>(old.enumOptions()))&&sameReference(old.referenceConfig());}private boolean sameReference(MdmModels.ReferenceConfig old){if(reference==null)return old==null;if(old==null)return false;return reference.targetModel.equals(old.targetModelCode())&&reference.valueField.equals(old.valueFieldCode())&&reference.displayField.equals(old.displayFieldCode())&&Objects.equals(reference.statusField,Objects.toString(old.statusFieldCode(),""))&&new HashSet<>(reference.allowedStatuses).equals(new HashSet<>(old.allowedStatuses()));}}
    private record ReferenceRow(String targetModel,String valueField,String displayField,String statusField,List<String> allowedStatuses){}
}
