package com.company.iaf.platform.permission.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuCreateRequest(
        Long parentId,
        @NotBlank String menuCode,
        @NotBlank String menuType,
        @NotBlank String titleKey,
        String routePath,
        String componentKey,
        String icon,
        Integer sortNo,
        @NotNull Boolean visible,
        @NotNull Boolean enabled
) {
}
