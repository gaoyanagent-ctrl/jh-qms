package com.company.iaf.platform.auth.domain.model;

import java.time.OffsetDateTime;

public record Tenant(
        Long id,
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
}
