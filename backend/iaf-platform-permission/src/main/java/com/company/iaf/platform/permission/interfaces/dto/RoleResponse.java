package com.company.iaf.platform.permission.interfaces.dto;

import com.company.iaf.platform.permission.domain.model.Role;
import com.company.iaf.platform.permission.domain.model.RoleStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response payload for platform role APIs. {@code permissions} and
 * {@code menuCodes} contain the current role bindings.
 */
public record RoleResponse(
        Long id,
        Long tenantId,
        String roleCode,
        String roleName,
        String roleType,
        RoleStatus status,
        Integer version,
        List<String> permissions,
        List<String> menuCodes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RoleResponse from(Role role, List<String> permissions) {
        return from(role, permissions, List.of());
    }

    public static RoleResponse from(Role role, List<String> permissions, List<String> menuCodes) {
        return new RoleResponse(
                role.id(),
                role.tenantId(),
                role.roleCode(),
                role.roleName(),
                role.roleType(),
                role.status(),
                role.version(),
                permissions == null ? List.of() : permissions,
                menuCodes == null ? List.of() : menuCodes,
                role.createdAt(),
                role.updatedAt()
        );
    }

    public static RoleResponse from(Role role) {
        return from(role, List.of());
    }
}
