package com.company.iaf.platform.system.infrastructure.persistence;

import com.company.iaf.platform.system.application.PlatformSystemErrorCode;
import com.company.iaf.platform.system.domain.model.BrandConfig;
import com.company.iaf.platform.system.domain.model.I18nResource;
import com.company.iaf.platform.system.domain.model.ThemeConfig;
import com.company.iaf.platform.system.domain.model.UserExperiencePreference;
import com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcSystemConfigurationRepository implements SystemConfigurationRepository {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcSystemConfigurationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ThemeConfig getTheme(long tenantId) {
        List<ThemeConfig> rows = jdbcTemplate.query("""
                        select theme_name, primary_color, sidebar_mode, tokens_json
                          from sys_theme_config
                         where tenant_id = ?
                           and config_key = 'current'
                           and deleted = false
                         order by id desc
                         limit 1
                        """,
                this::mapTheme,
                tenantId
        );
        return rows.isEmpty() ? ThemeConfig.defaults() : rows.getFirst();
    }

    @Override
    public ThemeConfig saveTheme(long tenantId, long operatorUserId, ThemeConfig config) {
        jdbcTemplate.update("""
                        insert into sys_theme_config (
                            tenant_id, config_key, theme_name, primary_color, sidebar_mode,
                            tokens_json, created_by, updated_by
                        )
                        values (?, 'current', ?, ?, ?, cast(? as jsonb), ?, ?)
                        on conflict (tenant_id, config_key)
                        do update set
                            theme_name = excluded.theme_name,
                            primary_color = excluded.primary_color,
                            sidebar_mode = excluded.sidebar_mode,
                            tokens_json = excluded.tokens_json,
                            updated_by = excluded.updated_by,
                            updated_at = current_timestamp,
                            version = sys_theme_config.version + 1,
                            deleted = false
                        """,
                tenantId,
                config.themeName(),
                config.primaryColor(),
                config.sidebarMode(),
                toJson(config.tokens()),
                operatorUserId,
                operatorUserId
        );
        return getTheme(tenantId);
    }

    @Override
    public BrandConfig getBrand(long tenantId) {
        List<BrandConfig> rows = jdbcTemplate.query("""
                        select brand_name, logo_url, favicon_url, login_hero_title,
                               login_hero_subtitle, login_ops_title, login_ops_description,
                               login_background_type, login_background_image_url
                          from sys_brand_config
                         where tenant_id = ?
                           and config_key = 'current'
                           and deleted = false
                         order by id desc
                         limit 1
                        """,
                this::mapBrand,
                tenantId
        );
        return rows.isEmpty() ? BrandConfig.defaults() : rows.getFirst();
    }

    @Override
    public BrandConfig saveBrand(long tenantId, long operatorUserId, BrandConfig config) {
        jdbcTemplate.update("""
                        insert into sys_brand_config (
                            tenant_id, config_key, brand_name, logo_url, favicon_url,
                            login_hero_title, login_hero_subtitle, login_ops_title,
                            login_ops_description, login_background_type, login_background_image_url,
                            created_by, updated_by
                        )
                        values (?, 'current', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        on conflict (tenant_id, config_key)
                        do update set
                            brand_name = excluded.brand_name,
                            logo_url = excluded.logo_url,
                            favicon_url = excluded.favicon_url,
                            login_hero_title = excluded.login_hero_title,
                            login_hero_subtitle = excluded.login_hero_subtitle,
                            login_ops_title = excluded.login_ops_title,
                            login_ops_description = excluded.login_ops_description,
                            login_background_type = excluded.login_background_type,
                            login_background_image_url = excluded.login_background_image_url,
                            updated_by = excluded.updated_by,
                            updated_at = current_timestamp,
                            version = sys_brand_config.version + 1,
                            deleted = false
                        """,
                tenantId,
                config.brandName(),
                config.logoUrl(),
                config.faviconUrl(),
                config.loginHeroTitle(),
                config.loginHeroSubtitle(),
                config.loginOpsTitle(),
                config.loginOpsDescription(),
                config.loginBackgroundType(),
                config.loginBackgroundImageUrl(),
                operatorUserId,
                operatorUserId
        );
        return getBrand(tenantId);
    }

    @Override
    public List<I18nResource> listI18nResources(long tenantId, String locale) {
        return jdbcTemplate.query("""
                        select locale, resource_key, resource_value
                          from sys_i18n_resource
                         where tenant_id = ?
                           and locale = ?
                           and deleted = false
                         order by resource_key
                        """,
                this::mapI18nResource,
                tenantId,
                locale
        );
    }

    @Override
    public void replaceI18nResources(long tenantId, long operatorUserId, String locale, List<I18nResource> resources) {
        jdbcTemplate.update("""
                        update sys_i18n_resource
                           set deleted = true,
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where tenant_id = ?
                           and locale = ?
                           and deleted = false
                        """,
                operatorUserId,
                tenantId,
                locale
        );
        for (I18nResource resource : resources) {
            jdbcTemplate.update("""
                            insert into sys_i18n_resource (
                                tenant_id, locale, resource_key, resource_value, created_by, updated_by
                            )
                            values (?, ?, ?, ?, ?, ?)
                            on conflict (tenant_id, locale, resource_key)
                            do update set
                                resource_value = excluded.resource_value,
                                deleted = false,
                                updated_by = excluded.updated_by,
                                updated_at = current_timestamp,
                                version = sys_i18n_resource.version + 1
                            """,
                    tenantId,
                    locale,
                    resource.resourceKey(),
                    resource.resourceValue(),
                    operatorUserId,
                    operatorUserId
            );
        }
    }

    @Override
    public UserExperiencePreference getUserPreference(long tenantId, long userId) {
        List<UserExperiencePreference> rows = jdbcTemplate.query("""
                        select user_id, preference_json
                          from sys_user_experience_preference
                         where tenant_id = ?
                           and user_id = ?
                           and deleted = false
                         limit 1
                        """,
                this::mapPreference,
                tenantId,
                userId
        );
        return rows.isEmpty() ? UserExperiencePreference.defaults(userId) : rows.getFirst();
    }

    @Override
    public UserExperiencePreference saveUserPreference(long tenantId, long userId, UserExperiencePreference preference) {
        jdbcTemplate.update("""
                        insert into sys_user_experience_preference (
                            tenant_id, user_id, preference_json, created_by, updated_by
                        )
                        values (?, ?, cast(? as jsonb), ?, ?)
                        on conflict (tenant_id, user_id)
                        do update set
                            preference_json = excluded.preference_json,
                            updated_by = excluded.updated_by,
                            updated_at = current_timestamp,
                            version = sys_user_experience_preference.version + 1,
                            deleted = false
                        """,
                tenantId,
                userId,
                toJson(preference.settings()),
                userId,
                userId
        );
        return getUserPreference(tenantId, userId);
    }

    private ThemeConfig mapTheme(ResultSet rs, int rowNum) throws SQLException {
        return new ThemeConfig(
                rs.getString("theme_name"),
                rs.getString("primary_color"),
                rs.getString("sidebar_mode"),
                readMap(rs.getString("tokens_json"))
        );
    }

    private BrandConfig mapBrand(ResultSet rs, int rowNum) throws SQLException {
        return new BrandConfig(
                rs.getString("brand_name"),
                rs.getString("logo_url"),
                rs.getString("favicon_url"),
                rs.getString("login_hero_title"),
                rs.getString("login_hero_subtitle"),
                rs.getString("login_ops_title"),
                rs.getString("login_ops_description"),
                rs.getString("login_background_type"),
                rs.getString("login_background_image_url")
        );
    }

    private I18nResource mapI18nResource(ResultSet rs, int rowNum) throws SQLException {
        return new I18nResource(
                rs.getString("locale"),
                rs.getString("resource_key"),
                rs.getString("resource_value")
        );
    }

    private UserExperiencePreference mapPreference(ResultSet rs, int rowNum) throws SQLException {
        return new UserExperiencePreference(
                rs.getLong("user_id"),
                readMap(rs.getString("preference_json"))
        );
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Configuration JSON could not be serialized");
        }
    }

    private Map<String, Object> readMap(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (Exception ex) {
            throw new BusinessException(PlatformSystemErrorCode.INVALID_CONFIGURATION, "Configuration JSON could not be parsed");
        }
    }
}
