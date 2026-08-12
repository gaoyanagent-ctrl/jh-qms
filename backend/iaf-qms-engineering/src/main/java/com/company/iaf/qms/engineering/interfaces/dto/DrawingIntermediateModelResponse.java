package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingIntermediateModel;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record DrawingIntermediateModelResponse(long id, long revisionId, long parseJobId,
        String schemaVersion, String documentId, String revisionCode, JsonNode model,
        OffsetDateTime createdAt) {
    public static DrawingIntermediateModelResponse from(DrawingIntermediateModel value) {
        return new DrawingIntermediateModelResponse(value.id(), value.revisionId(), value.parseJobId(),
                value.schemaVersion(), value.documentId(), value.revisionCode(), value.modelJson(),
                value.createdAt());
    }
}
