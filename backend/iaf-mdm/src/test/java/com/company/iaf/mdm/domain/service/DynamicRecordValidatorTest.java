package com.company.iaf.mdm.domain.service;
import com.company.iaf.mdm.domain.model.MdmModels; import org.junit.jupiter.api.Test; import java.util.*; import static org.assertj.core.api.Assertions.assertThat;
class DynamicRecordValidatorTest {
 @Test void validatesRequiredTypeAndEnumFromMetadata(){var fields=List.of(new MdmModels.Field(1,"materialType","物料类型","ENUM",true,false,false,true,true,true,20,List.of("RAW","FINISHED"),null,1));var model=new MdmModels.Model(1,"manufacturing","material","物料","MASTER",true,true,true,true,"PUBLISHED",1,Map.of(),fields);var validator=new DynamicRecordValidator();assertThat(validator.validate(model,Map.of())).contains("materialType: required");assertThat(validator.validate(model,Map.of("materialType","UNKNOWN"))).contains("materialType: invalid option");assertThat(validator.validate(model,Map.of("materialType","RAW"))).isEmpty();}
}
