package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.PartApplicationService;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.company.iaf.qms.engineering.interfaces.dto.PartCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.PartResponse;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QmsPartControllerTest {

    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void createUsesAuthenticatedTenantAndOrganizationAndUnifiedEnvelope() throws Exception {
        PartApplicationService service = mock(PartApplicationService.class);
        PartResponse response = new PartResponse(
                7L, 10L, "P-100", null, "Bracket", null, null, null, null,
                PartStatus.ACTIVE, 0, OffsetDateTime.parse("2026-08-11T09:00:00Z"),
                OffsetDateTime.parse("2026-08-11T09:00:00Z")
        );
        when(service.create(eq(1L), eq(10L), any(PartCreateRequest.class))).thenReturn(response);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QmsPartController(service)).build();
        TenantContext.setTenantId(1L);
        SecurityContext.setCurrentOrgId(10L);

        mvc.perform(post("/api/qms/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new PartCreateRequest("P-100", null, "Bracket", null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.partNo").value("P-100"));
    }

    @Test
    void createRejectsInvalidBody() throws Exception {
        PartApplicationService service = mock(PartApplicationService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QmsPartController(service)).build();
        TenantContext.setTenantId(1L);
        SecurityContext.setCurrentOrgId(10L);

        mvc.perform(post("/api/qms/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partNo\":\"\",\"partName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
