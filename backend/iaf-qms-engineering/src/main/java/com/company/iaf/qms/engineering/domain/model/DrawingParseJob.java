package com.company.iaf.qms.engineering.domain.model;

import java.time.OffsetDateTime;

public record DrawingParseJob(Long id, long tenantId, long orgId, long revisionId, long fileId,
                              int attemptNo, ParseJobStatus status, String parserType,
                              String errorCode, String errorMessage, int version,
                              OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
