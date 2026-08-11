package com.company.iaf.platform.auth.domain.model;

public record TenantInfo(
        long tenantId,
        String tenantCode,
        String status
) {
    public boolean enabled() {
        return "ENABLED".equals(status);
    }
}
