package com.company.iaf.qms.engineering.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DrawingRevision(
        Long id,
        long tenantId,
        long orgId,
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
        Long releasedBy,
        OffsetDateTime releasedAt,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DrawingRevision metadataDraft(
            long tenantId,
            long orgId,
            long drawingId,
            String revisionCode,
            int revisionSeq,
            LocalDate effectiveDate,
            Long supersedesRevisionId
    ) {
        if (revisionSeq < 1) {
            throw new IllegalArgumentException("revisionSeq must be positive");
        }
        return new DrawingRevision(
                null, tenantId, orgId, drawingId, revisionCode, revisionSeq,
                null, null, effectiveDate, null, supersedesRevisionId,
                ParseStatus.PENDING, ReviewStatus.PENDING, DrawingRevisionStatus.DRAFT,
                null, null, null, 0, null, null
        );
    }
}
