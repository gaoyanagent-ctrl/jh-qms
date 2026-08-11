package com.company.iaf.platform.auth.interfaces.dto;

import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantStatus;

import java.time.OffsetDateTime;

public record TenantResponse(
        long tenantId,
        String tenantCode,
        String tenantName,
        TenantStatus status,
        String initializationStatus,
        String initializationError,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.tenantId(),
                tenant.tenantCode(),
                tenant.tenantName(),
                tenant.status(),
                tenant.initializationStatus(),
                tenant.initializationError(),
                tenant.version(),
                tenant.createdAt(),
                tenant.updatedAt()
        );
    }
}
