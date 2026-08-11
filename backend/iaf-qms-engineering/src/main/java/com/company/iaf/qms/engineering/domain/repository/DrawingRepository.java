package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.Drawing;

import java.util.List;
import java.util.Optional;

public interface DrawingRepository {
    Optional<Drawing> findById(long tenantId, long orgId, long id);
    boolean existsByDrawingNo(long tenantId, long partId, String drawingNo);
    List<Drawing> findByPartId(long tenantId, long orgId, long partId);
    long insert(long operatorUserId, Drawing drawing);
}
