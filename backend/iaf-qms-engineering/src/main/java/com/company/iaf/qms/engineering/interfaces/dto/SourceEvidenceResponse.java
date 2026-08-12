package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.EvidenceExtractorType;
import com.company.iaf.qms.engineering.domain.model.SourceEvidence;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SourceEvidenceResponse(long id, long sourceFileId, long drawingRevisionId,
        long parseJobId, String evidenceKey, String entityId, String entityHandle, String sheetNo, Integer pageNo,
        BoundingBox bbox, String rawText, String normalizedText, EvidenceExtractorType extractorType,
        String extractorVersion, String modelName, String modelVersion, BigDecimal confidence,
        OffsetDateTime createdAt) {
    public static SourceEvidenceResponse from(SourceEvidence value) {
        return new SourceEvidenceResponse(value.id(), value.sourceFileId(), value.drawingRevisionId(),
                value.parseJobId(), value.evidenceKey(), value.entityId(), value.entityHandle(), value.sheetNo(), value.pageNo(),
                new BoundingBox(value.bboxX(), value.bboxY(), value.bboxW(), value.bboxH()), value.rawText(),
                value.normalizedText(), value.extractorType(), value.extractorVersion(), value.modelName(),
                value.modelVersion(), value.confidence(), value.createdAt());
    }
    public record BoundingBox(BigDecimal x, BigDecimal y, BigDecimal width, BigDecimal height) { }
}
