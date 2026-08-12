package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.DrawingParseJob;
import java.util.List;
import java.util.Optional;

public interface DrawingParseJobRepository {
    long enqueue(long actorId, long tenantId, long orgId, long revisionId, long fileId, String parserType, int attemptNo);
    Optional<DrawingParseJob> findLatest(long tenantId, long orgId, long revisionId);
    List<DrawingParseJob> findLatestByDrawingId(long tenantId, long orgId, long drawingId);
}
