package com.company.iaf.platform.system.interfaces.dto;

import com.company.iaf.platform.system.domain.model.UserExperiencePreference;

import java.util.Map;

public record UserPreferenceResponse(
        long userId,
        Map<String, Object> settings
) {
    public static UserPreferenceResponse from(UserExperiencePreference preference) {
        return new UserPreferenceResponse(preference.userId(), preference.settings());
    }
}
