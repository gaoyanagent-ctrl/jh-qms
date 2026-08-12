package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingParseResultQueryService;
import com.company.iaf.qms.engineering.domain.model.EvidenceExtractorType;
import com.company.iaf.qms.engineering.interfaces.dto.SourceEvidenceResponse;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QmsDrawingParseResultControllerTest {
    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void evidenceUsesAuthenticatedScopeAndReturnsViewerBoundingBox() throws Exception {
        DrawingParseResultQueryService service = mock(DrawingParseResultQueryService.class);
        SourceEvidenceResponse response = new SourceEvidenceResponse(8, 11, 5, 7, "EV-1",
                "DIM-1", null, "1", 1,
                new SourceEvidenceResponse.BoundingBox(new BigDecimal("10.5"), new BigDecimal("20.5"),
                        new BigDecimal("30.0"), new BigDecimal("8.0")),
                "8±0.5", "8±0.5", EvidenceExtractorType.PDF_VECTOR, "1.0", null, null,
                new BigDecimal("0.9800"), OffsetDateTime.parse("2026-08-12T00:00:00Z"));
        when(service.evidence(1, 10, 5)).thenReturn(List.of(response));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QmsDrawingParseResultController(service)).build();
        TenantContext.setTenantId(1L);
        SecurityContext.setCurrentOrgId(10L);

        mvc.perform(get("/api/qms/drawing-revisions/5/evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].evidenceKey").value("EV-1"))
                .andExpect(jsonPath("$.data[0].bbox.x").value(10.5))
                .andExpect(jsonPath("$.data[0].confidence").value(0.98));
        verify(service).evidence(1, 10, 5);
    }
}
