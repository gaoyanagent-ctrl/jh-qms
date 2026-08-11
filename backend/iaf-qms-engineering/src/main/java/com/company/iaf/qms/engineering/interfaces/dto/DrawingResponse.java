package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "QMS drawing")
public record DrawingResponse(
        Long id,
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
    public static DrawingResponse from(Drawing drawing) {
        return new DrawingResponse(
                drawing.id(), drawing.partId(), drawing.drawingNo(), drawing.drawingName(),
                drawing.drawingType(), drawing.sourceSystem(), drawing.status(), drawing.version(),
                drawing.createdAt(), drawing.updatedAt()
        );
    }
}
