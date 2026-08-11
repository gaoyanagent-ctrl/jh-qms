package com.company.iaf.platform.system.domain.model;

import java.util.Map;

public record ThemeConfig(
        String themeName,
        String primaryColor,
        String sidebarMode,
        Map<String, Object> tokens
) {
    public static ThemeConfig defaults() {
        return new ThemeConfig("light-industrial", "#334155", "dark", Map.of());
    }
}
