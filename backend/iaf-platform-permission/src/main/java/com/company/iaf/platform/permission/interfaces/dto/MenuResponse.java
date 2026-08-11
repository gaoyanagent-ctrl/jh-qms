package com.company.iaf.platform.permission.interfaces.dto;

import com.company.iaf.platform.permission.domain.model.Menu;

import java.time.OffsetDateTime;
import java.util.List;

public record MenuResponse(
        Long id,
        Long tenantId,
        Long parentId,
        String menuCode,
        String menuType,
        String titleKey,
        String routePath,
        String componentKey,
        String icon,
        Integer sortNo,
        Boolean visible,
        Boolean enabled,
        Integer version,
        List<String> permissionCodes,
        List<MenuResponse> children,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MenuResponse from(Menu menu, List<String> permissionCodes, List<MenuResponse> children) {
        return new MenuResponse(
                menu.id(),
                menu.tenantId(),
                menu.parentId(),
                menu.menuCode(),
                menu.menuType(),
                menu.titleKey(),
                menu.routePath(),
                menu.componentKey(),
                menu.icon(),
                menu.sortNo(),
                menu.visible(),
                menu.enabled(),
                menu.version(),
                permissionCodes == null ? List.of() : permissionCodes,
                children == null ? List.of() : children,
                menu.createdAt(),
                menu.updatedAt()
        );
    }
}
