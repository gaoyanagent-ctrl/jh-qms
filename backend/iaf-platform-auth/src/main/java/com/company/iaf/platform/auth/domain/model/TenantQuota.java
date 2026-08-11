package com.company.iaf.platform.auth.domain.model;

public record TenantQuota(
        long tenantId,
        String quotaKey,
        long quotaLimit,
        long quotaUsed
) {
    public boolean exceededBy(long increment) {
        return quotaLimit >= 0 && quotaUsed + increment > quotaLimit;
    }
}
