package com.company.iaf.qms.engineering.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SourceEvidence(Long id, long tenantId, long orgId, long sourceFileId,
                             long drawingRevisionId, long parseJobId, String evidenceKey, String entityId,
                             String entityHandle, String sheetNo, Integer pageNo,
                             BigDecimal bboxX, BigDecimal bboxY, BigDecimal bboxW, BigDecimal bboxH,
                             String rawText, String normalizedText, EvidenceExtractorType extractorType,
                             String extractorVersion, String modelName, String modelVersion,
                             BigDecimal confidence, int version, OffsetDateTime createdAt,
                             OffsetDateTime updatedAt) { }
