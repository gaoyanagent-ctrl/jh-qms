package com.company.iaf.mdm.domain.service;

import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ModelDefinitionValidatorTest {
    private final ModelDefinitionValidator validator = new ModelDefinitionValidator();
    @Test void acceptsValidDefinition() {
        var field=new MdmDtos.FieldDraft("materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW"),null,10,null);
        assertThat(validator.validate(List.of(field)).valid()).isTrue();
    }
    @Test void reportsDuplicateAndMissingEnumOptions() {
        var first=new MdmDtos.FieldDraft("kind","类型","ENUM",false,false,false,false,false,true,null,List.of(),null,10,null);
        var second=new MdmDtos.FieldDraft("kind","重复","STRING",false,false,false,false,false,true,null,List.of(),null,20,null);
        assertThat(validator.validate(List.of(first,second)).errors()).hasSize(2);
    }
}
