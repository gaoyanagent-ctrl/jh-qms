package com.company.iaf.platform.permission.domain.model;

import java.time.OffsetDateTime;

/**
 * Platform role aggregate. Represents a named bundle of permissions
 * that can be assigned to a user.
 */
public record Role(
        Long id,
        Long tenantId,
        String roleCode,
        String roleName,
        String roleType,
        RoleStatus status,
        Integer version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}