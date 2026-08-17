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
    public List<String> validate(long tenantId,MdmModels.Model model,MdmDtos.SaveRecordRequest request){
        Map<String,Object> data=new LinkedHashMap<>(request.attributes());data.put("businessCode",request.businessCode());data.put("name",request.name());data.put("lifecycleStatus",request.lifecycleStatus());
        var errors=new ArrayList<String>();
        for(var rule:repository.findValidationRules(tenantId,model.id(),"SAVE")){
            if(!rule.enabled()||!jsonLogic.matches(rule.condition(),data))continue;
            boolean passed=switch(rule.ruleType()){case "REFERENCE_EXISTS"->referenceExists(tenantId,rule,data);case "EXPRESSION"->jsonLogic.matches(rule.assertion(),data);default->true;};
            if(!passed)errors.add(rule.fieldCode()==null?rule.message():rule.fieldCode()+": "+rule.message());
        }return errors;
    }
    private boolean referenceExists(long tenantId,MdmModels.ValidationRule rule,Map<String,Object> data){
        String targetModel=String.valueOf(rule.assertion().get("targetModel"));Object raw=rule.assertion().get("conditions");if(targetModel.isBlank()||!(raw instanceof List<?> conditions))return false;
        var resolved=new ArrayList<MdmDtos.ReferenceCondition>();for(Object item:conditions){if(!(item instanceof Map<?,?> condition))return false;String field=String.valueOf(condition.get("targetField"));Object value=condition.containsKey("sourceField")?data.get(String.valueOf(condition.get("sourceField"))):condition.get("value");resolved.add(new MdmDtos.ReferenceCondition(field,value));}
        return repository.referenceExists(tenantId,targetModel,resolved);
    }
}
