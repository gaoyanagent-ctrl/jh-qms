package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.infrastructure.persistence.InspectionStandardService;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QmsInspectionStandardControllerTest {
    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void getBindsRevisionIdWithoutCompilerParameterMetadata() throws Exception {
        InspectionStandardService service = mock(InspectionStandardService.class);
        when(service.get(1, 10, 5)).thenReturn(null);
        var mvc = MockMvcBuilders.standaloneSetup(new QmsInspectionStandardController(service)).build();
        TenantContext.setTenantId(1L);
        SecurityContext.setCurrentOrgId(10L);

        mvc.perform(get("/api/qms/drawing-revisions/5/inspection-standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).get(1, 10, 5);
    }
}
