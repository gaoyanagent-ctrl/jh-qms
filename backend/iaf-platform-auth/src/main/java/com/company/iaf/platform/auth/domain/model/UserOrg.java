package com.company.iaf.platform.auth.domain.model;

import java.time.OffsetDateTime;

public record UserOrg(
        Long id,
        Long tenantId,
        Long userId,
        Long orgId,
        String orgCode,
        String orgName,
        String orgType,
        boolean primary,
        int scopeWeight,
        OffsetDateTime validFrom,
        OffsetDateTime validTo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
