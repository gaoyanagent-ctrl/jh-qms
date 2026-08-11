package com.company.iaf.platform.permission.domain.model;

import java.time.OffsetDateTime;

/**
 * Platform navigation menu aggregate. Menus describe route visibility and
 * navigation metadata; backend permission checks remain enforced by API
 * permission annotations.
 */
public record Menu(
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
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
