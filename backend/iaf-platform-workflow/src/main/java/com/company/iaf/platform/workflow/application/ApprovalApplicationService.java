package com.company.iaf.platform.workflow.application;

import java.util.Optional;

public interface ApprovalApplicationService {
    ApprovalRecord submit(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    ApprovalRecord approve(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    ApprovalRecord reject(long tenantId, long orgId, String businessType, long businessId, long actorId, String comment);
    Optional<ApprovalRecord> findLatest(long tenantId, long orgId, String businessType, long businessId);
}
