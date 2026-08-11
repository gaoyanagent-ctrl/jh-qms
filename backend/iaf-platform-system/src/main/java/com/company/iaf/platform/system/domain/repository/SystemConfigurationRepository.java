package com.company.iaf.platform.system.domain.repository;

import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.I18nResource;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.domain.model.UserExperiencePreference;

import java.util.List;

public interface SystemConfigurationRepository {

    ThemeConfig getTheme(long tenantId);

    ThemeConfig saveTheme(long tenantId, long operatorUserId, ThemeConfig config);

    BrandConfig getBrand(long tenantId);

    BrandConfig saveBrand(long tenantId, long operatorUserId, BrandConfig config);

    List<I18nResource> listI18nResources(long tenantId, String locale);

    void replaceI18nResources(long tenantId, long operatorUserId, String locale, List<I18nResource> resources);

    UserExperiencePreference getUserPreference(long tenantId, long userId);

    UserExperiencePreference saveUserPreference(long tenantId, long userId, UserExperiencePreference preference);
}
