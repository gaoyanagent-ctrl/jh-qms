package com.company.iaf.platform.permission.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for {@code PUT /api/platform/roles/{id}/permissions}.
 * The list replaces the role's current permission set atomically.
 */
public record AssignRolePermissionsRequest(
        @NotNull
        List<String> permissionCodes
) {
}