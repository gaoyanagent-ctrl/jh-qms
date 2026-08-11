package com.company.iaf.platform.permission.domain.model;

/**
 * Platform permission aggregate. The triple
 * {@code module_code : action_code} plus the {@code resource_type}
 * describe what a caller is allowed to do.
 */
public record Permission(
        Long id,
        Long tenantId,
        String permissionCode,
        String permissionName,
        String resourceType,
        String moduleCode,
        String actionCode
) {
}