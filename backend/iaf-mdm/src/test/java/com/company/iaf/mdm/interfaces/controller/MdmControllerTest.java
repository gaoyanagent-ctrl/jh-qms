package com.company.iaf.mdm.interfaces.controller;

import com.company.iaf.mdm.application.MdmApplicationService;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MdmControllerTest {
    @AfterEach void clearContext() { TenantContext.clear(); }

    @Test void resolvesExplicitModelAndPagingParameterNames() throws Exception {
        var service=mock(MdmApplicationService.class); TenantContext.setTenantId(1L);
        when(service.schema(1L,"material")).thenReturn(null);
        when(service.records(1L,"material",null,1,20)).thenReturn(new PageResult<>(List.of(),0,1,20));
        var mvc=MockMvcBuilders.standaloneSetup(new MdmController(service)).build();
        mvc.perform(get("/api/mdm/models/material/schema")).andExpect(status().isOk());
        mvc.perform(get("/api/mdm/models/material/records").param("pageNo","1").param("pageSize","20")).andExpect(status().isOk());
        verify(service).schema(1L,"material"); verify(service).records(1L,"material",null,1,20);
    }
}
