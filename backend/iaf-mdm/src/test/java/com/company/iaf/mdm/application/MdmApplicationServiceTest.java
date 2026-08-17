package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class MdmApplicationServiceTest {
    @Test void publishedModelCanBeSavedAsNextDraft() {
        var repository=mock(MdmRepository.class);
        var published=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"PUBLISHED",1,Map.of(),List.of());
        var draft=new MdmModels.Model(7,"manufacturing","material","物料","MASTER",true,true,true,false,"DRAFT",1,Map.of(),List.of());
        when(repository.findModel(1,"material")).thenReturn(Optional.of(published),Optional.of(draft));
        var field=new MdmDtos.FieldDraft("materialType","物料类型","ENUM",true,false,false,true,true,true,32,List.of("RAW","FINISHED"),null,10);
        var request=new MdmDtos.SaveModelDraftRequest(List.of(field),Map.of());

        new MdmApplicationService(repository).saveDraft(1,9,"material",request);

        verify(repository).replaceDraft(1,9,7,List.of(field),Map.of());
    }
}
