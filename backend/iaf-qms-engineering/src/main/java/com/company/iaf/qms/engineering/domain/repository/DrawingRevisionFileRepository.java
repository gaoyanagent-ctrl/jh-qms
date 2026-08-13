package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFile;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFileRole;
import java.util.List;
import java.util.Optional;

public interface DrawingRevisionFileRepository {
    void attach(long actorId, long tenantId, long orgId, long revisionId, long fileId, DrawingRevisionFileRole role);
    Optional<DrawingRevisionFile> find(long tenantId, long orgId, long revisionId, DrawingRevisionFileRole role);
    List<DrawingRevisionFile> findAll(long tenantId, long orgId, long revisionId);
}
