package com.company.iaf.qms.engineering.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record DrawingIntermediateModel(Long id, long tenantId, long orgId, long revisionId,
                                       long parseJobId, String schemaVersion, String documentId,
                                       String revisionCode, JsonNode modelJson, int version,
                                       OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
