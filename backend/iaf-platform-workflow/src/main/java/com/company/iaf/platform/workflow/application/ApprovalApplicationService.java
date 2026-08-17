package com.company.iaf.platform.workflow.application;

import java.util.Optional;
import java.util.List;

public interface ApprovalApplicationService {
    ApprovalRecord submit(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    ApprovalRecord approve(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    ApprovalRecord reject(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    Optional<ApprovalRecord> findLatest(long tenantId, long orgId, String businessType, long businessId);
    default List<ApprovalRecord> findLatestByBusinessType(long tenantId, String businessType) { return List.of(); }
}
