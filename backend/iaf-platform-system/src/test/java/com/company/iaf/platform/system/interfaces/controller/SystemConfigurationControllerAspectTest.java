package com.company.iaf.platform.system.interfaces.controller;

import com.company.iaf.platform.core.security.PermissionChecker;
import com.company.iaf.platform.core.security.RequiresPermissionAspect;
import com.company.iaf.platform.system.application.SystemConfigurationApplicationService;
import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.I18nResource;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.domain.model.UserExperiencePreference;
import com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = SystemConfigurationControllerAspectTest.TestConfig.class)
class SystemConfigurationControllerAspectTest {

    @Autowired
    private SystemConfigurationApplicationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigurationController(service))
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
        TenantContext.clear();
    }

    @Test
    void getThemeReturns200WhenPermissionGranted() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:theme:view"));

        mockMvc.perform(get("/api/platform/theme/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.themeName").value("light-industrial"));
    }

    @Test
    void getThemeReturns403WhenPermissionMissing() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:brand:view"));

        mockMvc.perform(get("/api/platform/theme/current"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    void savePreferenceReturns401WhenUserUnauthenticated() throws Exception {
        SecurityContext.setPermissions(Set.of("platform:preference:me"));

        mockMvc.perform(put("/api/platform/preferences/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"settings\":{\"themeName\":\"dark-industrial\"}}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.UNAUTHORIZED.code()));
    }

    @Test
    void savePreferenceReturns200WhenPermissionGranted() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:preference:me"));

        mockMvc.perform(put("/api/platform/preferences/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"settings\":{\"themeName\":\"dark-industrial\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(99))
                .andExpect(jsonPath("$.data.settings.themeName").value("dark-industrial"));
    }

    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
            String code = exception.errorCode().code();
            int status = switch (code) {
                case "COMMON_UNAUTHORIZED" -> 401;
                case "COMMON_FORBIDDEN" -> 403;
                default -> 400;
            };
            return ResponseEntity.status(status).body(Result.fail(code, exception.getMessage()));
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        SystemConfigurationRepository systemConfigurationRepository() {
            return new InMemorySystemConfigurationRepository();
        }

        @Bean
        SystemConfigurationApplicationService systemConfigurationApplicationService(SystemConfigurationRepository repository) {
            return new SystemConfigurationApplicationService(repository);
        }

        @Bean
        PermissionChecker permissionChecker() {
            return new PermissionChecker();
        }

        @Bean
        RequiresPermissionAspect requiresPermissionAspect(PermissionChecker permissionChecker) {
            return new RequiresPermissionAspect(permissionChecker);
        }
    }

    private static final class InMemorySystemConfigurationRepository implements SystemConfigurationRepository {

        private ThemeConfig theme = ThemeConfig.defaults();
        private BrandConfig brand = BrandConfig.defaults();
        private UserExperiencePreference preference = UserExperiencePreference.defaults(99L);

        @Override
        public ThemeConfig getTheme(long tenantId) {
            return theme;
        }

        @Override
        public ThemeConfig saveTheme(long tenantId, long operatorUserId, ThemeConfig config) {
            this.theme = config;
            return theme;
        }

        @Override
        public BrandConfig getBrand(long tenantId) {
            return brand;
        }

        @Override
        public BrandConfig saveBrand(long tenantId, long operatorUserId, BrandConfig config) {
            this.brand = config;
            return brand;
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
            return preference;
        }

        @Override
        public UserExperiencePreference saveUserPreference(long tenantId, long userId, UserExperiencePreference preference) {
            this.preference = preference;
            return preference;
        }
    }
}
