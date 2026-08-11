package com.company.iaf.platform.system.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ThemeConfigRequest(
        @NotBlank String themeName,
        @NotBlank String primaryColor,
        @NotBlank String sidebarMode,
        Map<String, Object> tokens
) {
}
