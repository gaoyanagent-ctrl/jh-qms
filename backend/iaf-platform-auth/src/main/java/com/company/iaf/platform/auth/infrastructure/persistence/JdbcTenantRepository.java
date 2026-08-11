package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTenantRepository implements TenantRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTenantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TenantInfo> findByTenantCode(String tenantCode) {
        List<TenantInfo> tenants = jdbcTemplate.query("""
                        select tenant_id, tenant_code, status
                          from sys_tenant
                         where tenant_code = ?
                           and deleted = false
                        """,
                (resultSet, rowNum) -> new TenantInfo(
                        resultSet.getLong("tenant_id"),
                        resultSet.getString("tenant_code"),
                        resultSet.getString("status")
                ),
                tenantCode
        );
        return tenants.stream().findFirst();
    }

    @Override
    public Optional<Tenant> findById(long tenantId) {
        List<Tenant> rows = jdbcTemplate.query("""
                        select id, tenant_id, tenant_code, tenant_name, status,
                               initialization_status, initialization_error,
                               version, created_at, updated_at
                          from sys_tenant
                         where tenant_id = ?
                           and deleted = false
                        """,
                this::mapTenant,
                tenantId
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Tenant> findPage(String keyword, int pageNo, int pageSize) {
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        int offset = Math.max(0, pageNo - 1) * Math.max(1, pageSize);
        return jdbcTemplate.query("""
                        select id, tenant_id, tenant_code, tenant_name, status,
                               initialization_status, initialization_error,
                               version, created_at, updated_at
                          from sys_tenant
                         where (? is null or tenant_code ilike ? or tenant_name ilike ?)
                           and deleted = false
                         order by tenant_id
                         limit ? offset ?
                        """,
                this::mapTenant,
                pattern, pattern, pattern, Math.max(1, pageSize), offset
        );
    }

    @Override
    public long count(String keyword) {
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        Long count = jdbcTemplate.queryForObject("""
                        select count(*)
                          from sys_tenant
                         where (? is null or tenant_code ilike ? or tenant_name ilike ?)
                           and deleted = false
                        """,
                Long.class,
                pattern, pattern, pattern
        );
        return count == null ? 0 : count;
    }

    @Override
    public boolean existsByTenantCode(String tenantCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sys_tenant where tenant_code = ? and deleted = false",
                Integer.class,
                tenantCode
        );
        return count != null && count > 0;
    }

    @Override
    public long createTenant(long operatorUserId, String tenantCode, String tenantName) {
        Long id = jdbcTemplate.queryForObject("select nextval(pg_get_serial_sequence('sys_tenant', 'id'))", Long.class);
        if (id == null) {
            throw new IllegalStateException("Could not allocate tenant id");
        }
        jdbcTemplate.update("""
                        insert into sys_tenant (
                            id, tenant_id, tenant_code, tenant_name, status,
                            initialization_status, created_by, updated_by
                        )
                        values (?, ?, ?, ?, 'ENABLED', 'PENDING', ?, ?)
                        """,
                id, id, tenantCode, tenantName, operatorUserId, operatorUserId
        );
        return id;
    }

    @Override
    public boolean updateTenant(long operatorUserId, long tenantId, String tenantName, int expectedVersion) {
        int rows = jdbcTemplate.update("""
                        update sys_tenant
                           set tenant_name = ?,
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where tenant_id = ?
                           and version = ?
                           and deleted = false
                        """,
                tenantName, operatorUserId, tenantId, expectedVersion
        );
        return rows > 0;
    }

    @Override
    public boolean updateStatus(long operatorUserId, long tenantId, TenantStatus status, int expectedVersion) {
        int rows = jdbcTemplate.update("""
                        update sys_tenant
                           set status = ?,
                               disabled_at = case when ? = 'DISABLED' then current_timestamp else null end,
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where tenant_id = ?
                           and version = ?
                           and deleted = false
                        """,
                status.name(), status.name(), operatorUserId, tenantId, expectedVersion
        );
        return rows > 0;
    }

    @Override
    public void initializeDefaults(long operatorUserId, long tenantId, String tenantCode, String tenantName, String adminUsername, String adminPasswordHash) {
        jdbcTemplate.update("""
                        insert into sys_org (
                            tenant_id, parent_id, org_code, org_name, org_type, status,
                            sort_no, created_by, updated_by
                        )
                        values (?, null, 'ROOT', ?, 'COMPANY', 'ENABLED', 0, ?, ?)
                        on conflict (tenant_id, org_code) do nothing
                        """,
                tenantId, tenantName, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_user (
                            tenant_id, username, display_name, password_hash, status, primary_org_id,
                            created_by, updated_by
                        )
                        values (?, ?, 'Tenant Administrator', ?, 'ENABLED',
                                (select id from sys_org where tenant_id = ? and org_code = 'ROOT'),
                                ?, ?)
                        on conflict (tenant_id, username) do nothing
                        """,
                tenantId, adminUsername, adminPasswordHash, tenantId, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_role (tenant_id, role_code, role_name, role_type, status, created_by, updated_by)
                        values (?, 'platform_admin', 'Platform Administrator', 'INTERNAL', 'ENABLED', ?, ?)
                        on conflict (tenant_id, role_code) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_permission (
                            tenant_id, permission_code, permission_name, resource_type, module_code, action_code,
                            created_by, updated_by
                        )
                        select ?, permission_code, permission_name, resource_type, module_code, action_code, ?, ?
                          from sys_permission
                         where tenant_id = 1
                           and deleted = false
                        on conflict (tenant_id, permission_code) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_user_role (tenant_id, user_id, role_id, created_by, updated_by)
                        select ?, u.id, r.id, ?, ?
                          from sys_user u
                          join sys_role r on r.tenant_id = u.tenant_id and r.role_code = 'platform_admin'
                         where u.tenant_id = ?
                           and u.username = ?
                        on conflict (tenant_id, user_id, role_id) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId, adminUsername
        );
        jdbcTemplate.update("""
                        insert into sys_role_permission (tenant_id, role_id, permission_id, created_by, updated_by)
                        select ?, r.id, p.id, ?, ?
                          from sys_role r
                          join sys_permission p on p.tenant_id = r.tenant_id
                         where r.tenant_id = ?
                           and r.role_code = 'platform_admin'
                        on conflict (tenant_id, role_id, permission_id) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId
        );
        jdbcTemplate.update("""
                        insert into sys_user_org (
                            tenant_id, user_id, org_id, is_primary, scope_weight,
                            created_by, updated_by
                        )
                        select ?, u.id, o.id, true, 100, ?, ?
                          from sys_user u
                          join sys_org o on o.tenant_id = u.tenant_id and o.org_code = 'ROOT'
                         where u.tenant_id = ?
                           and u.username = ?
                        on conflict (tenant_id, user_id, org_id) where deleted = false do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId, adminUsername
        );
        initializeMenus(operatorUserId, tenantId);
        initializeSystemConfig(operatorUserId, tenantId, tenantName);
        upsertQuota(operatorUserId, tenantId, "USER_COUNT", 100L);
        jdbcTemplate.update("""
                        update sys_tenant
                           set initialization_status = 'COMPLETED',
                               initialization_error = null,
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where tenant_id = ?
                        """,
                operatorUserId, tenantId
        );
    }

    @Override
    public List<TenantQuota> listQuotas(long tenantId) {
        return jdbcTemplate.query("""
                        select tenant_id, quota_key, quota_limit, quota_used
                          from sys_tenant_quota
                         where tenant_id = ?
                           and deleted = false
                         order by quota_key
                        """,
                this::mapQuota,
                tenantId
        );
    }

    @Override
    public Optional<TenantQuota> findQuota(long tenantId, String quotaKey) {
        List<TenantQuota> rows = jdbcTemplate.query("""
                        select tenant_id, quota_key, quota_limit, quota_used
                          from sys_tenant_quota
                         where tenant_id = ?
                           and quota_key = ?
                           and deleted = false
                        """,
                this::mapQuota,
                tenantId, quotaKey
        );
        return rows.stream().findFirst();
    }

    @Override
    public void upsertQuota(long operatorUserId, long tenantId, String quotaKey, long quotaLimit) {
        jdbcTemplate.update("""
                        insert into sys_tenant_quota (
                            tenant_id, quota_key, quota_limit, quota_used, created_by, updated_by
                        )
                        values (?, ?, ?, 0, ?, ?)
                        on conflict (tenant_id, quota_key)
                        do update set
                            quota_limit = excluded.quota_limit,
                            quota_used = (
                                select count(*) from sys_user
                                 where tenant_id = excluded.tenant_id
                                   and status = 'ENABLED'
                                   and deleted = false
                            ),
                            updated_by = excluded.updated_by,
                            updated_at = current_timestamp,
                            version = sys_tenant_quota.version + 1,
                            deleted = false
                        """,
                tenantId, quotaKey, quotaLimit, operatorUserId, operatorUserId
        );
    }

    @Override
    public long countActiveUsers(long tenantId) {
        Long count = jdbcTemplate.queryForObject("""
                        select count(*)
                          from sys_user
                         where tenant_id = ?
                           and status = 'ENABLED'
                           and deleted = false
                        """,
                Long.class,
                tenantId
        );
        return count == null ? 0 : count;
    }

    private void initializeMenus(long operatorUserId, long tenantId) {
        jdbcTemplate.update("""
                        insert into sys_menu (
                            tenant_id, parent_id, menu_code, menu_name, menu_type,
                            title_i18n_key, route_path, component_key, icon, sort_no,
                            visible, enabled, created_by, updated_by
                        )
                        select ?, null, menu_code, menu_name, menu_type,
                               title_i18n_key, route_path, component_key, icon, sort_no,
                               visible, enabled, ?, ?
                          from sys_menu
                         where tenant_id = 1
                           and parent_id is null
                           and deleted = false
                        on conflict (tenant_id, menu_code) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_menu (
                            tenant_id, parent_id, menu_code, menu_name, menu_type,
                            title_i18n_key, route_path, component_key, icon, sort_no,
                            visible, enabled, created_by, updated_by
                        )
                        select ?, target_parent.id, source.menu_code, source.menu_name, source.menu_type,
                               source.title_i18n_key, source.route_path, source.component_key, source.icon, source.sort_no,
                               source.visible, source.enabled, ?, ?
                          from sys_menu source
                          join sys_menu source_parent on source_parent.id = source.parent_id
                          join sys_menu target_parent
                            on target_parent.tenant_id = ?
                           and target_parent.menu_code = source_parent.menu_code
                         where source.tenant_id = 1
                           and source.parent_id is not null
                           and source.deleted = false
                        on conflict (tenant_id, menu_code) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId
        );
        jdbcTemplate.update("""
                        insert into sys_menu_permission (tenant_id, menu_id, permission_id, created_by, updated_by)
                        select ?, target_menu.id, target_permission.id, ?, ?
                          from sys_menu_permission source_link
                          join sys_menu source_menu on source_menu.id = source_link.menu_id
                          join sys_permission source_permission on source_permission.id = source_link.permission_id
                          join sys_menu target_menu
                            on target_menu.tenant_id = ?
                           and target_menu.menu_code = source_menu.menu_code
                          join sys_permission target_permission
                            on target_permission.tenant_id = ?
                           and target_permission.permission_code = source_permission.permission_code
                         where source_link.tenant_id = 1
                           and source_link.deleted = false
                        on conflict (tenant_id, menu_id, permission_id) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId, tenantId
        );
        jdbcTemplate.update("""
                        insert into sys_role_menu (tenant_id, role_id, menu_id, created_by, updated_by)
                        select ?, role.id, menu.id, ?, ?
                          from sys_role role
                          join sys_menu menu on menu.tenant_id = role.tenant_id
                         where role.tenant_id = ?
                           and role.role_code = 'platform_admin'
                        on conflict (tenant_id, role_id, menu_id) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId, tenantId
        );
    }

    private void initializeSystemConfig(long operatorUserId, long tenantId, String tenantName) {
        jdbcTemplate.update("""
                        insert into sys_theme_config (
                            tenant_id, config_key, theme_name, primary_color, sidebar_mode,
                            tokens_json, created_by, updated_by
                        )
                        values (?, 'current', 'light-industrial', '#1677ff', 'light', '{}'::jsonb, ?, ?)
                        on conflict (tenant_id, config_key) do nothing
                        """,
                tenantId, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_brand_config (
                            tenant_id, config_key, brand_name, login_hero_title,
                            login_hero_subtitle, login_ops_title, login_ops_description,
                            login_background_type, created_by, updated_by
                        )
                        values (?, 'current', ?, ?, 'Industrial Application Framework',
                                'Operations', 'Tenant operations workspace', 'preset', ?, ?)
                        on conflict (tenant_id, config_key) do nothing
                        """,
                tenantId, tenantName, tenantName, operatorUserId, operatorUserId
        );
        jdbcTemplate.update("""
                        insert into sys_i18n_resource (
                            tenant_id, locale, resource_key, resource_value, created_by, updated_by
                        )
                        values
                            (?, 'zh-CN', 'app.name', ?, ?, ?),
                            (?, 'en-US', 'app.name', ?, ?, ?)
                        on conflict (tenant_id, locale, resource_key) do nothing
                        """,
                tenantId, tenantName, operatorUserId, operatorUserId,
                tenantId, tenantName, operatorUserId, operatorUserId
        );
    }

    private Tenant mapTenant(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Tenant(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getString("tenant_code"),
                rs.getString("tenant_name"),
                TenantStatus.valueOf(rs.getString("status")),
                rs.getString("initialization_status"),
                rs.getString("initialization_error"),
                rs.getInt("version"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                toOffsetDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private TenantQuota mapQuota(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new TenantQuota(
                rs.getLong("tenant_id"),
                rs.getString("quota_key"),
                rs.getLong("quota_limit"),
                rs.getLong("quota_used")
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
