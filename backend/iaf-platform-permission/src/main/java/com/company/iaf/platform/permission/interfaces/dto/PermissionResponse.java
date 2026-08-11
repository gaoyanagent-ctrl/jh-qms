package com.company.iaf.platform.permission.interfaces.dto;

import com.company.iaf.platform.permission.domain.model.Permission;

public record PermissionResponse(
        Long id,
        Long tenantId,
        String permissionCode,
        String permissionName,
        String resourceType,
        String moduleCode,
        String actionCode
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.id(),
                permission.tenantId(),
                permission.permissionCode(),
                permission.permissionName(),
                permission.resourceType(),
                permission.moduleCode(),
                permission.actionCode()
        );
    }
}
