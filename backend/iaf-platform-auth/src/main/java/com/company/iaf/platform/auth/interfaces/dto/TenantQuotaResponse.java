package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.TenantQuota;

public record TenantQuotaResponse(
        long tenantId,
        String quotaKey,
        long quotaLimit,
        long quotaUsed
) {
    public static TenantQuotaResponse from(TenantQuota quota) {
        return new TenantQuotaResponse(quota.tenantId(), quota.quotaKey(), quota.quotaLimit(), quota.quotaUsed());
    }
}
