package com.company.iaf.mdm.interfaces.controller;

import com.company.iaf.mdm.application.MdmApplicationService;
import com.company.iaf.mdm.application.MdmExcelImportService;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.tenant.TenantContext;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MdmControllerTest {
    @AfterEach void clearContext() { TenantContext.clear(); SecurityContext.clear(); }

    @Test void resolvesExplicitModelAndPagingParameterNames() throws Exception {
        var service=mock(MdmApplicationService.class); TenantContext.setTenantId(1L); SecurityContext.setUserId(9L);
        when(service.schema(1L,"material")).thenReturn(null);
        when(service.records(1L,"material",null,1,20)).thenReturn(new PageResult<>(List.of(),0,1,20));
        when(service.validateBatch(eq(1L),eq("material"),any())).thenReturn(new com.company.iaf.mdm.interfaces.dto.MdmDtos.BatchValidationResult(true,1,List.of()));
        var recordId=UUID.randomUUID(); when(service.recordVersions(1L,"material",recordId)).thenReturn(List.of());
        var excel=mock(MdmExcelImportService.class); when(excel.template(1L,"material")).thenReturn(new byte[]{1,2});
        when(excel.preview(eq(1L),eq(9L),eq("material"),any())).thenReturn(new com.company.iaf.mdm.interfaces.dto.MdmDtos.ImportPreview(UUID.randomUUID(),"READY","material.xlsx",List.of(),new com.company.iaf.mdm.interfaces.dto.MdmDtos.BatchValidationResult(true,0,List.of())));
        var mvc=MockMvcBuilders.standaloneSetup(new MdmController(service,excel)).build();
        mvc.perform(get("/api/mdm/models/material/schema")).andExpect(status().isOk());
        mvc.perform(get("/api/mdm/models/material/records").param("pageNo","1").param("pageSize","20")).andExpect(status().isOk());
        mvc.perform(get("/api/mdm/models/material/records/{id}/versions",recordId)).andExpect(status().isOk());
        mvc.perform(post("/api/mdm/models/material/records/batch-validate").contentType("application/json").content("{\"records\":[{\"businessCode\":\"M-1\",\"name\":\"物料\",\"attributes\":{}}]}" )).andExpect(status().isOk());
        mvc.perform(get("/api/mdm/models/material/import-template")).andExpect(status().isOk());
        mvc.perform(multipart("/api/mdm/models/material/imports").file(new MockMultipartFile("file","material.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",new byte[]{1}))).andExpect(status().isOk());
        verify(service).schema(1L,"material"); verify(service).records(1L,"material",null,1,20); verify(service).recordVersions(1L,"material",recordId);
    }
}
