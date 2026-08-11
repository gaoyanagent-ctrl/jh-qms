package com.company.iaf.platform.system.application;

import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.I18nResource;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.domain.model.UserExperiencePreference;
import com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemConfigurationApplicationServiceTest {

    private final InMemoryRepository repository = new InMemoryRepository();
    private final SystemConfigurationApplicationService service = new SystemConfigurationApplicationService(repository);

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    void rejectsUnsupportedTheme() {
        SecurityContext.setUserId(9L);

        assertThatThrownBy(() -> service.saveTheme(1L, new ThemeConfig("blue", "#334155", "dark", Map.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported themeName");
    }

    @Test
    void savesValidThemeWithOperator() {
        SecurityContext.setUserId(9L);

        ThemeConfig saved = service.saveTheme(1L, new ThemeConfig("dark-industrial", "#34b6d8", "light", Map.of("font", "large")));

        assertThat(saved.themeName()).isEqualTo("dark-industrial");
        assertThat(repository.lastOperatorUserId).isEqualTo(9L);
    }

    @Test
    void rejectsUnsupportedBrandBackgroundType() {
        SecurityContext.setUserId(9L);

        assertThatThrownBy(() -> service.saveBrand(1L, new BrandConfig("IAF", null, null, "title", "subtitle", "ops", "desc", "video", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported loginBackgroundType");
    }

    @Test
    void savesUserPreferenceForCurrentUser() {
        UserExperiencePreference saved = service.saveMyPreference(1L, 7L, Map.of("themeName", "dark-industrial"));

        assertThat(saved.userId()).isEqualTo(7L);
        assertThat(saved.settings()).containsEntry("themeName", "dark-industrial");
    }

    @Test
    void themeAndPreferenceAreTenantScoped() {
        SecurityContext.setUserId(9L);
        service.saveTheme(1L, new ThemeConfig("dark-industrial", "#34b6d8", "light", Map.of("tenant", "one")));
        service.saveTheme(2L, new ThemeConfig("light-industrial", "#1d4ed8", "dark", Map.of("tenant", "two")));
        service.saveMyPreference(1L, 7L, Map.of("themeName", "dark-industrial"));
        service.saveMyPreference(2L, 7L, Map.of("themeName", "light-industrial"));

        assertThat(service.getTheme(1L).tokens()).containsEntry("tenant", "one");
        assertThat(service.getTheme(2L).tokens()).containsEntry("tenant", "two");
        assertThat(service.getMyPreference(1L, 7L).settings()).containsEntry("themeName", "dark-industrial");
        assertThat(service.getMyPreference(2L, 7L).settings()).containsEntry("themeName", "light-industrial");
    }

    private static final class InMemoryRepository implements SystemConfigurationRepository {
        private final Map<Long, ThemeConfig> themes = new java.util.concurrent.ConcurrentHashMap<>();
        private final Map<Long, BrandConfig> brands = new java.util.concurrent.ConcurrentHashMap<>();
        private final Map<String, UserExperiencePreference> preferences = new java.util.concurrent.ConcurrentHashMap<>();
        private long lastOperatorUserId;

        @Override
        public ThemeConfig getTheme(long tenantId) {
            return themes.getOrDefault(tenantId, ThemeConfig.defaults());
        }

        @Override
        public ThemeConfig saveTheme(long tenantId, long operatorUserId, ThemeConfig config) {
            this.lastOperatorUserId = operatorUserId;
            this.themes.put(tenantId, config);
            return config;
        }

        @Override
        public BrandConfig getBrand(long tenantId) {
            return brands.getOrDefault(tenantId, BrandConfig.defaults());
        }

        @Override
        public BrandConfig saveBrand(long tenantId, long operatorUserId, BrandConfig config) {
            this.brands.put(tenantId, config);
            return config;
        }

        @Override
        public List<I18nResource> listI18nResources(long tenantId, String locale) {
            return new ArrayList<>();
        }

        @Override
        public void replaceI18nResources(long tenantId, long operatorUserId, String locale, List<I18nResource> resources) {
        }

        @Override
        public UserExperiencePreference getUserPreference(long tenantId, long userId) {
            return preferences.getOrDefault(key(tenantId, userId), UserExperiencePreference.defaults(userId));
        }

        @Override
        public UserExperiencePreference saveUserPreference(long tenantId, long userId, UserExperiencePreference preference) {
            this.preferences.put(key(tenantId, userId), preference);
            return preference;
        }

        private static String key(long tenantId, long userId) {
            return tenantId + ":" + userId;
        }
    }
}
