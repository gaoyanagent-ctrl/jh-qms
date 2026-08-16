package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.infrastructure.persistence.ValidationPlanService;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QmsValidationPlanControllerTest {
    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void generateBindsInspectionStandardIdWithoutCompilerParameterMetadata() throws Exception {
        ValidationPlanService service = mock(ValidationPlanService.class);
        when(service.generate(1, 10, 5)).thenReturn(null);
        var mvc = MockMvcBuilders.standaloneSetup(new QmsValidationPlanController(service)).build();
        TenantContext.setTenantId(1L);
        SecurityContext.setCurrentOrgId(10L);

        mvc.perform(post("/api/qms/inspection-standards/5/validation-plan/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).generate(1, 10, 5);
    }
}
