package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfiguredRuleValidatorTest {
    @Test void resolvesSourceFieldForGenericReferenceRule(){
        var repository=mock(MdmRepository.class);var model=new MdmModels.Model(8,"manufacturing","bomItem","BOM","MASTER",true,true,false,false,"PUBLISHED",1,Map.of(),List.of());
        var assertion=Map.<String,Object>of("targetModel","material","conditions",List.of(Map.of("targetField","businessCode","sourceField","componentMaterialCode"),Map.of("targetField","lifecycleStatus","value","ACTIVE")));
        when(repository.findValidationRules(1,8,"SAVE")).thenReturn(List.of(new MdmModels.ValidationRule(1,8,"component-active","component","SAVE","REFERENCE_EXISTS","componentMaterialCode","ERROR","引用物料无效",Map.of(),assertion,true,1)));
        when(repository.referenceExists(eq(1),eq("material"),anyList())).thenReturn(false);
        var request=new MdmDtos.SaveRecordRequest("B-1","BOM","ACTIVE","GROUP",List.of(),null,null,Map.of("componentMaterialCode","M-1"),null,null);
        assertThat(new ConfiguredRuleValidator(repository).validate(1,model,request)).containsExactly("componentMaterialCode: 引用物料无效");
        verify(repository).referenceExists(1,"material",List.of(new MdmDtos.ReferenceCondition("businessCode","M-1"),new MdmDtos.ReferenceCondition("lifecycleStatus","ACTIVE")));
    }
}
