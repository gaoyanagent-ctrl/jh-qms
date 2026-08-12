package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.*;
import java.util.List;
import java.util.Optional;

public interface DrawingParseResultRepository {
    void save(long actorId, long tenantId, long orgId, long revisionId, long parseJobId,
              long sourceFileId, DrawingParseResult result);
    Optional<DrawingIntermediateModel> findModel(long tenantId, long orgId, long revisionId);
    List<DrawingEntity> findEntities(long tenantId, long orgId, long revisionId);
    List<SourceEvidence> findEvidence(long tenantId, long orgId, long revisionId);
    Optional<SourceEvidence> findEvidenceById(long tenantId, long orgId, long revisionId, long evidenceId);
}
