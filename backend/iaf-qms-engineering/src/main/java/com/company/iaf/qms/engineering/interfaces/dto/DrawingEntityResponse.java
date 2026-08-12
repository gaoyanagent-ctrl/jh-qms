package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingEntity;
import com.company.iaf.qms.engineering.domain.model.DrawingEntityType;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record DrawingEntityResponse(long id, String entityId, String sourceEntityHandle,
        DrawingEntityType entityType, String layer, String sheetNo, BoundingBox bbox,
        JsonNode geometry, String rawText, String normalizedText, JsonNode style) {
    public static DrawingEntityResponse from(DrawingEntity value) {
        BoundingBox bbox = value.bboxX() == null ? null
                : new BoundingBox(value.bboxX(), value.bboxY(), value.bboxW(), value.bboxH());
        return new DrawingEntityResponse(value.id(), value.entityId(), value.sourceEntityHandle(),
                value.entityType(), value.layerName(), value.sheetNo(), bbox, value.geometry(),
                value.rawText(), value.normalizedText(), value.style());
    }
    public record BoundingBox(BigDecimal x, BigDecimal y, BigDecimal width, BigDecimal height) { }
}
