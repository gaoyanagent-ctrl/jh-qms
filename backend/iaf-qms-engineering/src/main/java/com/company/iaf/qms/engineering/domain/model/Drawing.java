package com.company.iaf.qms.engineering.domain.model;

import java.time.OffsetDateTime;

public record Drawing(
        Long id,
        long tenantId,
        long orgId,
        long partId,
        String drawingNo,
        String drawingName,
        DrawingType drawingType,
        DrawingSourceSystem sourceSystem,
        DrawingStatus status,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
