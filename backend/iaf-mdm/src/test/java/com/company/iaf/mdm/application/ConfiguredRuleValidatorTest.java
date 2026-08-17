package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfiguredRuleValidatorTest {
    @Test void validatesReferenceMetadataWithoutBusinessSpecificRule(){
        var repository=mock(MdmRepository.class);var config=new MdmModels.ReferenceConfig("material","businessCode","name","lifecycleStatus",List.of("ACTIVE"));
        var field=new MdmModels.Field(2,"componentMaterialCode","组件物料","REFERENCE",true,false,false,true,true,true,128,List.of(),null,10,config);
        var model=new MdmModels.Model(8,"manufacturing","bomItem","BOM","MASTER",true,true,false,false,"PUBLISHED",1,Map.of(),List.of(field));
        when(repository.findValidationRules(1,8,"SAVE")).thenReturn(List.of());
        var request=new MdmDtos.SaveRecordRequest("B-1","BOM","ACTIVE","GROUP",List.of(),null,null,Map.of("componentMaterialCode","M-404"),null,null);
        assertThat(new ConfiguredRuleValidator(repository).validate(1,model,request)).containsExactly("componentMaterialCode: 引用的主数据不存在或状态不允许");
        verify(repository).referenceExists(1,"material",List.of(new MdmDtos.ReferenceCondition("businessCode","M-404"),new MdmDtos.ReferenceCondition("lifecycleStatus","ACTIVE")));
    }
    @Test void resolvesSourceFieldForGenericReferenceRule(){
        var repository=mock(MdmRepository.class);var model=new MdmModels.Model(8,"manufacturing","bomItem","BOM","MASTER",true,true,false,false,"PUBLISHED",1,Map.of(),List.of());
        var assertion=Map.<String,Object>of("targetModel","material","conditions",List.of(Map.of("targetField","businessCode","sourceField","componentMaterialCode"),Map.of("targetField","lifecycleStatus","value","ACTIVE")));
        when(repository.findValidationRules(1,8,"SAVE")).thenReturn(List.of(new MdmModels.ValidationRule(1,8,"component-active","component","SAVE","REFERENCE_EXISTS","componentMaterialCode","ERROR","引用物料无效",Map.of(),assertion,true,1)));
        when(repository.referenceExists(eq(1),eq("material"),anyList())).thenReturn(false);
        var request=new MdmDtos.SaveRecordRequest("B-1","BOM","ACTIVE","GROUP",List.of(),null,null,Map.of("componentMaterialCode","M-1"),null,null);
        assertThat(new ConfiguredRuleValidator(repository).validate(1,model,request)).containsExactly("componentMaterialCode: 引用物料无效");
        verify(repository).referenceExists(1,"material",List.of(new MdmDtos.ReferenceCondition("businessCode","M-1"),new MdmDtos.ReferenceCondition("lifecycleStatus","ACTIVE")));
    }
    @Test void blurValidationReturnsWarningsWithoutBlocking(){
        var repository=mock(MdmRepository.class);var model=new MdmModels.Model(8,"manufacturing","bomItem","BOM","MASTER",true,true,false,false,"PUBLISHED",1,Map.of(),List.of());
        var assertion=Map.<String,Object>of("targetModel","material","conditions",List.of(Map.of("targetField","businessCode","sourceField","componentMaterialCode")));
        when(repository.findValidationRules(1,8,"BLUR")).thenReturn(List.of(new MdmModels.ValidationRule(1,8,"component-warning","component","BLUR","REFERENCE_EXISTS","componentMaterialCode","WARNING","物料尚未生效",Map.of(),assertion,true,1)));
        var request=new MdmDtos.SaveRecordRequest("B-1","BOM","DRAFT","GROUP",List.of(),null,null,Map.of("componentMaterialCode","M-1"),null,null);

        var outcome=new ConfiguredRuleValidator(repository).validateDetailed(1,model,request,"componentMaterialCode");

        assertThat(outcome.valid()).isTrue();
        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.warnings()).extracting(MdmDtos.ValidationIssue::message).containsExactly("物料尚未生效");
    }
}
