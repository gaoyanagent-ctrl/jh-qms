package com.company.iaf.platform.system.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.I18nResource;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.domain.model.UserExperiencePreference;
import com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SystemConfigurationApplicationService {

    private final SystemConfigurationRepository repository;

    public SystemConfigurationApplicationService(SystemConfigurationRepository repository) {
        this.repository = repository;
    }

    @RequiresPermission("platform:theme:view")
    @Transactional(readOnly = true)
    public ThemeConfig getTheme(long tenantId) {
        return repository.getTheme(tenantId);
    }

    @RequiresPermission("platform:theme:update")
    @Transactional
    public ThemeConfig saveTheme(long tenantId, ThemeConfig config) {
        validateTheme(config);
        return repository.saveTheme(tenantId, currentUserId(), config);
    }

    @RequiresPermission("platform:brand:view")
    @Transactional(readOnly = true)
    public BrandConfig getBrand(long tenantId) {
        return repository.getBrand(tenantId);
    }

    @RequiresPermission("platform:brand:update")
    @Transactional
    public BrandConfig saveBrand(long tenantId, BrandConfig config) {
        validateBrand(config);
        return repository.saveBrand(tenantId, currentUserId(), config);
    }

    @RequiresPermission("platform:i18n:view")
    @Transactional(readOnly = true)
    public List<I18nResource> listI18nResources(long tenantId, String locale) {
        return repository.listI18nResources(tenantId, locale);
    }

    @RequiresPermission("platform:i18n:update")
    @Transactional
    public void replaceI18nResources(long tenantId, String locale, List<I18nResource> resources) {
        repository.replaceI18nResources(tenantId, currentUserId(), locale, resources);
    }

    @RequiresPermission("platform:preference:me")
    @Transactional(readOnly = true)
    public UserExperiencePreference getMyPreference(long tenantId, long userId) {
        return repository.getUserPreference(tenantId, userId);
    }

    @RequiresPermission("platform:preference:me")
    @Transactional
    public UserExperiencePreference saveMyPreference(long tenantId, long userId, Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Preference settings must not be empty");
        }
        return repository.saveUserPreference(tenantId, userId, new UserExperiencePreference(userId, settings));
    }

    private static void validateTheme(ThemeConfig config) {
        if (!"light-industrial".equals(config.themeName()) && !"dark-industrial".equals(config.themeName())) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Unsupported themeName: " + config.themeName());
        }
        if (!"dark".equals(config.sidebarMode()) && !"light".equals(config.sidebarMode())) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Unsupported sidebarMode: " + config.sidebarMode());
        }
    }

    private static void validateBrand(BrandConfig config) {
        if (!"token".equals(config.loginBackgroundType()) && !"image".equals(config.loginBackgroundType())) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Unsupported loginBackgroundType: " + config.loginBackgroundType());
        }
    }

    private static long currentUserId() {
        return SecurityContext.getUserId().orElse(0L);
    }
}
