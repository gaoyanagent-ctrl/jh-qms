package com.company.iaf.platform.system.domain.model;

import java.util.Map;

public record UserExperiencePreference(
        long userId,
        Map<String, Object> settings
) {
    public static UserExperiencePreference defaults(long userId) {
        return new UserExperiencePreference(userId, Map.of(
                "themeName", "light-industrial",
                "formInteractionMode", "drawer",
                "density", "standard",
                "fontSize", "default",
                "sidebarMode", "dark",
                "sidebarCollapsed", false,
                "sidebarWidth", 248,
                "motionLevel", "subtle",
                "surfaceWidth", "wide",
                "workspaceMode", "simple"
        ));
    }
}
