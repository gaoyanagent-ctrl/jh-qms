package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionStatus;
import com.company.iaf.qms.engineering.domain.model.ParseStatus;
import com.company.iaf.qms.engineering.domain.model.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "QMS drawing revision")
public record DrawingRevisionResponse(
        Long id,
        long drawingId,
        String revisionCode,
        int revisionSeq,
        Long fileId,
        String fileType,
        LocalDate effectiveDate,
        LocalDate releaseDate,
        Long supersedesRevisionId,
        ParseStatus parseStatus,
        ReviewStatus reviewStatus,
        DrawingRevisionStatus status,
        String checksum,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DrawingRevisionResponse from(DrawingRevision revision) {
        return new DrawingRevisionResponse(
                revision.id(), revision.drawingId(), revision.revisionCode(), revision.revisionSeq(),
                revision.fileId(), revision.fileType(), revision.effectiveDate(), revision.releaseDate(),
                revision.supersedesRevisionId(), revision.parseStatus(), revision.reviewStatus(),
                revision.status(), revision.checksum(), revision.version(), revision.createdAt(), revision.updatedAt()
        );
    }
}
