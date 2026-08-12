package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.DrawingRevision;

import java.util.List;
import java.util.Optional;

public interface DrawingRevisionRepository {
    Optional<DrawingRevision> findById(long tenantId, long orgId, long id);
    boolean existsByRevisionCode(long tenantId, long drawingId, String revisionCode);
    int reserveNextSequence(long tenantId, long drawingId);
    List<DrawingRevision> findByDrawingId(long tenantId, long orgId, long drawingId);
    long insert(long operatorUserId, DrawingRevision revision);
    boolean attachFile(long operatorUserId, long tenantId, long orgId, long revisionId,
                       long fileId, String fileType, String checksum, int expectedVersion);
}
