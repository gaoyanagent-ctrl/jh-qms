package com.company.iaf.qms.engineering.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DrawingEntity(Long id, long tenantId, long orgId, long revisionId, long parseJobId,
                            String entityId, String sourceEntityHandle, DrawingEntityType entityType,
                            String layerName, String sheetNo, BigDecimal bboxX, BigDecimal bboxY,
                            BigDecimal bboxW, BigDecimal bboxH, JsonNode geometry, String rawText,
                            String normalizedText, JsonNode style, int version,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
