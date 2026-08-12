package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingParseJob;
import com.company.iaf.qms.engineering.domain.model.ParseJobStatus;
import java.time.OffsetDateTime;

public record DrawingParseJobResponse(Long id, long revisionId, int attemptNo, ParseJobStatus status,
                                      String parserType, String errorCode, String errorMessage,
                                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static DrawingParseJobResponse from(DrawingParseJob job) {
        return new DrawingParseJobResponse(job.id(), job.revisionId(), job.attemptNo(), job.status(),
                job.parserType(), job.errorCode(), job.errorMessage(), job.createdAt(), job.updatedAt());
    }
}
