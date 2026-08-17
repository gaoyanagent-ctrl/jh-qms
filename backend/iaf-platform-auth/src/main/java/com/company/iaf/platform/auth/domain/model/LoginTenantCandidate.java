package com.company.iaf.platform.auth.domain.model;

public record LoginTenantCandidate(
        long tenantId,
        String tenantCode,
        String tenantName,
        String passwordHash,
        String userStatus,
        String tenantStatus
) {
    public boolean enabled() {
        return "ENABLED".equals(userStatus) && "ENABLED".equals(tenantStatus);
    }
}
