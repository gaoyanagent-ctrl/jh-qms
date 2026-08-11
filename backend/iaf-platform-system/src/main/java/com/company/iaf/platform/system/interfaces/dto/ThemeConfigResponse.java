package com.company.iaf.platform.system.interfaces.dto;

import com.company.iaf.platform.system.domain.model.ThemeConfig;

import java.util.Map;

public record ThemeConfigResponse(
        String themeName,
        String primaryColor,
        String sidebarMode,
        Map<String, Object> tokens
) {
    public static ThemeConfigResponse from(ThemeConfig config) {
        return new ThemeConfigResponse(config.themeName(), config.primaryColor(), config.sidebarMode(), config.tokens());
    }

    public ThemeConfig toDomain() {
        return new ThemeConfig(themeName, primaryColor, sidebarMode, tokens == null ? Map.of() : tokens);
    }
}
