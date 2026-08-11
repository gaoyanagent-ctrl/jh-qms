package com.company.iaf.platform.system.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UserPreferenceRequest(
        @NotNull Map<String, Object> settings
) {
}
