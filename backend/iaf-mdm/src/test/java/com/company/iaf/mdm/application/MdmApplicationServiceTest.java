package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmApplicationServiceTest {
    @Test void userOutsideConfiguredRoleCannotApprove() {
        var repository=mock(MdmRepository.class);var recordId=UUID.randomUUID();
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,true,"PUBLISHED",1,Map.of("approval",Map.of("roleId",3)),List.of());
        when(repository.findModel(1,"material")).thenReturn(Optional.of(model));
        when(repository.userHasRole(1,9,3)).thenReturn(false);

        assertThatThrownBy(()->new MdmApplicationService(repository,rules(repository)).approve(1,9,"material",recordId,"同意"))
                .isInstanceOf(com.company.iaf.shared.exception.BusinessException.class);
        verify(repository,never()).transitionRecord(anyLong(),anyLong(),any(),anyList(),anyString(),anyLong());
    }
    @Test void approvalRequiredRecordIsSubmittedAndAudited() {
        var repository=mock(MdmRepository.class);var recordId=UUID.randomUUID();
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,true,"PUBLISHED",1,Map.of("approval",Map.of("roleId",3)),List.of());
        var draft=new MdmModels.Record(recordId,7,"material","M-1","物料","DRAFT",1,1,"GROUP",List.of(),null,null,Map.of(),0,null,null);
        var pending=new MdmModels.Record(recordId,7,"material","M-1","物料","PENDING_APPROVAL",2,1,"GROUP",List.of(),null,null,Map.of(),1,null,null);
        when(repository.findModel(1,"material")).thenReturn(Optional.of(model));
        when(repository.findRecord(1,7,recordId)).thenReturn(Optional.of(draft),Optional.of(pending));
        when(repository.transitionRecord(1,7,recordId,List.of("DRAFT","REJECTED"),"PENDING_APPROVAL",9)).thenReturn(true);

        new MdmApplicationService(repository,rules(repository)).submit(1,9,"material",recordId,"请审批");

        verify(repository).insertRecordAction(1,9,recordId,"SUBMIT","DRAFT","PENDING_APPROVAL","请审批");
        verify(repository).insertVersion(1,9,pending,"SUBMIT","请审批");
    }
    @Test void publishedModelCanBeSavedAsNextDraft() {
        var repository=mock(MdmRepository.class);
        var published=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of());
        var draft=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"DRAFT",1,Map.of(),List.of());
        when(repository.findModel(1,"material")).thenReturn(Optional.of(published),Optional.of(draft));
        var field=new MdmDtos.FieldDraft("materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW","FINISHED"),null,10,null);
        var uiSchema=Map.<String,Object>of("approval",Map.of("roleId",3));
        var request=new MdmDtos.SaveModelDraftRequest(true,List.of(field),uiSchema);
        when(repository.roleExists(1,3)).thenReturn(true);

        new MdmApplicationService(repository, rules(repository)).saveDraft(1,9,"material",request);

        verify(repository).replaceDraft(1,9,7,true,List.of(field),uiSchema);
    }

    @Test void returnsImmutableHistoryForRecordInRequestedModel() {
        var repository=mock(MdmRepository.class); var recordId=UUID.randomUUID();
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of());
        var record=new MdmModels.Record(recordId,7,"material","M-1","物料", "ACTIVE",2,1,"GROUP",List.of(),null,null,Map.of(),1,null,null);
        when(repository.findModel(1,"material")).thenReturn(Optional.of(model));
        when(repository.findRecord(1,7,recordId)).thenReturn(Optional.of(record));
        when(repository.findRecordVersions(1,7,recordId)).thenReturn(List.of());

        new MdmApplicationService(repository, rules(repository)).recordVersions(1,"material",recordId);

        verify(repository).findRecordVersions(1,7,recordId);
    }

    @Test void batchPrecheckReturnsRowLevelDuplicateAndModelErrors() {
        var repository=mock(MdmRepository.class);
        var requiredField=new MdmModels.Field(1,"materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW"),null,10,null);
        var model=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of(requiredField));
        when(repository.findModel(1,"material")).thenReturn(Optional.of(model));
        var first=new MdmDtos.SaveRecordRequest("M-1","物料1","DRAFT","GROUP",List.of(),null,null,Map.of("materialType","BAD"),null,"导入");
        var second=new MdmDtos.SaveRecordRequest("M-1","物料2","DRAFT","GROUP",List.of(),null,null,Map.of("materialType","RAW"),null,"导入");

        var result=new MdmApplicationService(repository, rules(repository)).validateBatch(1,"material",new MdmDtos.BatchRecordRequest(List.of(first,second)));

        org.assertj.core.api.Assertions.assertThat(result.valid()).isFalse();
        org.assertj.core.api.Assertions.assertThat(result.rows().get(0).errors()).contains("materialType: invalid option");
        org.assertj.core.api.Assertions.assertThat(result.rows().get(1).errors()).contains("businessCode: duplicated in batch");
    }
    private ConfiguredRuleValidator rules(MdmRepository repository){return new ConfiguredRuleValidator(repository);}
}
