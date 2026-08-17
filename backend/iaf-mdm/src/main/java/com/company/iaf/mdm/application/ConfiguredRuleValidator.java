package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.domain.service.JsonLogicEvaluator;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;

@Component
@ConditionalOnProperty(name="iaf.mdm.enabled",havingValue="true",matchIfMissing=true)
public class ConfiguredRuleValidator {
    private final MdmRepository repository; private final JsonLogicEvaluator jsonLogic=new JsonLogicEvaluator();
    public ConfiguredRuleValidator(MdmRepository repository){this.repository=repository;}
    public List<String> validate(long tenantId,MdmModels.Model model,MdmDtos.SaveRecordRequest request){return validateDetailed(tenantId,model,request,null).errors().stream().map(issue->issue.fieldCode()==null?issue.message():issue.fieldCode()+": "+issue.message()).toList();}
    public MdmDtos.ValidationOutcome validateDetailed(long tenantId,MdmModels.Model model,MdmDtos.SaveRecordRequest request,String fieldCode){
        Map<String,Object> data=new LinkedHashMap<>(request.attributes());data.put("businessCode",request.businessCode());data.put("name",request.name());data.put("lifecycleStatus",request.lifecycleStatus());
        var issues=new ArrayList<MdmDtos.ValidationIssue>();
        for(var field:model.fields()){
            var config=field.referenceConfig();Object source=data.get(field.code());
            if(!"REFERENCE".equals(field.dataType())||config==null||source==null||String.valueOf(source).isBlank())continue;
            var base=new ArrayList<MdmDtos.ReferenceCondition>();base.add(new MdmDtos.ReferenceCondition(config.valueFieldCode(),source));
            boolean exists;
            if(config.statusFieldCode()!=null&&!config.statusFieldCode().isBlank()&&config.allowedStatuses()!=null&&!config.allowedStatuses().isEmpty()) exists=config.allowedStatuses().stream().anyMatch(status->{var conditions=new ArrayList<>(base);conditions.add(new MdmDtos.ReferenceCondition(config.statusFieldCode(),status));return repository.referenceExists(tenantId,config.targetModelCode(),conditions);});
            else exists=repository.referenceExists(tenantId,config.targetModelCode(),base);
            if(!exists&&(fieldCode==null||field.code().equals(fieldCode)))issues.add(new MdmDtos.ValidationIssue(field.code(),"BLOCK","引用的主数据不存在或状态不允许"));
        }
        var configured=new ArrayList<MdmModels.ValidationRule>();
        if(fieldCode==null){configured.addAll(repository.findValidationRules(tenantId,model.id(),"SAVE"));configured.addAll(repository.findValidationRules(tenantId,model.id(),"BLUR"));}
        else configured.addAll(repository.findValidationRules(tenantId,model.id(),"BLUR"));
        for(var rule:configured){
            if(fieldCode!=null&&!fieldCode.equals(rule.fieldCode()))continue;
            if(!rule.enabled()||!jsonLogic.matches(rule.condition(),data))continue;
            boolean passed=switch(rule.ruleType()){case "REFERENCE_EXISTS"->referenceExists(tenantId,rule,data);case "EXPRESSION"->jsonLogic.matches(rule.assertion(),data);default->true;};
            if(!passed)issues.add(new MdmDtos.ValidationIssue(rule.fieldCode(),rule.severity(),rule.message()));
        }
        var errors=issues.stream().filter(issue->!"WARNING".equals(issue.severity())).toList();
        var warnings=issues.stream().filter(issue->"WARNING".equals(issue.severity())).toList();
        return new MdmDtos.ValidationOutcome(errors.isEmpty(),errors,warnings);
    }
    private boolean referenceExists(long tenantId,MdmModels.ValidationRule rule,Map<String,Object> data){
        String targetModel=String.valueOf(rule.assertion().get("targetModel"));Object raw=rule.assertion().get("conditions");if(targetModel.isBlank()||!(raw instanceof List<?> conditions))return false;
        var resolved=new ArrayList<MdmDtos.ReferenceCondition>();for(Object item:conditions){if(!(item instanceof Map<?,?> condition))return false;String field=String.valueOf(condition.get("targetField"));Object value=condition.containsKey("sourceField")?data.get(String.valueOf(condition.get("sourceField"))):condition.get("value");resolved.add(new MdmDtos.ReferenceCondition(field,value));}
        return repository.referenceExists(tenantId,targetModel,resolved);
    }
}
