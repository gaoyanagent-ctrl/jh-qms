package com.company.iaf.qms.engineering.domain.repository;

public interface QmsAuditTrail {
    void record(long tenantId, long actorId, String action, String objectType, long objectId, Object afterState);
}
