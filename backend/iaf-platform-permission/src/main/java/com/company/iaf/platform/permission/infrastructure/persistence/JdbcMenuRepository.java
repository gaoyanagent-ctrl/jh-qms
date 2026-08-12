package com.company.iaf.platform.permission.infrastructure.persistence;

import com.company.iaf.platform.permission.domain.model.Menu;
import com.company.iaf.platform.permission.domain.repository.MenuRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcMenuRepository implements MenuRepository {

    private static final String DELETED_FALSE = "deleted = false";

    private static final String SELECT_COLUMNS = """
            id, tenant_id, parent_id, menu_code, menu_type, title_key,
            route_path, component_key, icon, sort_no, visible, enabled,
            version, created_at, updated_at
            """;

    private static final String MENU_SELECT_COLUMNS = """
            m.id, m.tenant_id, m.parent_id, m.menu_code, m.menu_type, m.title_key,
            m.route_path, m.component_key, m.icon, m.sort_no, m.visible, m.enabled,
            m.version, m.created_at, m.updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcMenuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Menu> findById(long tenantId, long id) {
        List<Menu> results = jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from sys_menu where tenant_id = ? and id = ? and " + DELETED_FALSE,
                this::mapMenu,
                tenantId,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByMenuCode(long tenantId, String menuCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sys_menu where tenant_id = ? and menu_code = ? and " + DELETED_FALSE,
                Integer.class,
                tenantId,
                menuCode
        );
        return count != null && count > 0;
    }

    @Override
    public List<Menu> findAll(long tenantId) {
        return jdbcTemplate.query(("""
                        select %s
                          from sys_menu
                         where tenant_id = ?
                           and %s
                         order by sort_no, id
                        """).formatted(SELECT_COLUMNS, DELETED_FALSE),
                this::mapMenu,
                tenantId
        );
    }

    @Override
    public List<Menu> findAllByCodes(long tenantId, Collection<String> menuCodes) {
        if (menuCodes == null || menuCodes.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("select ").append(SELECT_COLUMNS)
                .append(" from sys_menu where tenant_id = ? and ").append(DELETED_FALSE)
                .append(" and menu_code in (");
        for (int i = 0; i < menuCodes.size(); i++) {
            if (i > 0) sql.append(',');
            sql.append('?');
        }
        sql.append(") order by sort_no, id");
        List<Object> params = new ArrayList<>(menuCodes.size() + 1);
        params.add(tenantId);
        params.addAll(menuCodes);
        return jdbcTemplate.query(sql.toString(), this::mapMenu, params.toArray());
    }

    @Override
    public List<Menu> findVisibleByUserId(long tenantId, long userId, Collection<String> permissionCodes) {
        List<Object> params = new ArrayList<>();
        params.add(tenantId);
        params.add(userId);
        String permissionFilter = "";
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            permissionFilter = """
                       and not exists (
                           select 1
                             from sys_menu_permission mp
                            where mp.tenant_id = m.tenant_id
                              and mp.menu_id = m.id
                              and mp.deleted = false
                       )
                    """;
        } else {
            permissionFilter = """
                       and (
                           not exists (
                               select 1
                                 from sys_menu_permission mp
                                where mp.tenant_id = m.tenant_id
                                  and mp.menu_id = m.id
                                  and mp.deleted = false
                           )
                           or exists (
                               select 1
                                 from sys_menu_permission mp
                                 join sys_permission p
                                   on p.id = mp.permission_id
                                  and p.tenant_id = mp.tenant_id
                                  and p.deleted = false
                                where mp.tenant_id = m.tenant_id
                                  and mp.menu_id = m.id
                                  and mp.deleted = false
                                  and p.permission_code in (
                    """;
            StringBuilder placeholders = new StringBuilder();
            int index = 0;
            for (String ignored : permissionCodes) {
                if (index++ > 0) placeholders.append(',');
                placeholders.append('?');
            }
            permissionFilter += placeholders + """
                                  )
                           )
                       )
                    """;
            params.addAll(permissionCodes);
        }
        return jdbcTemplate.query(("""
                        select distinct %s
                          from sys_menu m
                          join sys_role_menu rm
                            on rm.menu_id = m.id
                           and rm.tenant_id = m.tenant_id
                           and rm.deleted = false
                          join sys_user_role ur
                            on ur.role_id = rm.role_id
                           and ur.tenant_id = rm.tenant_id
                           and ur.deleted = false
                         where m.tenant_id = ?
                           and ur.user_id = ?
                           and m.visible = true
                           and m.enabled = true
                           and m.deleted = false
                    %s
                         order by m.sort_no, m.id
                        """).formatted(MENU_SELECT_COLUMNS, permissionFilter),
                this::mapMenu,
                params.toArray()
        );
    }

    @Override
    public Map<Long, List<String>> permissionCodesByMenuIds(long tenantId, Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Map.of();
        }
        StringBuilder sql = new StringBuilder("""
                select mp.menu_id, p.permission_code
                  from sys_menu_permission mp
                  join sys_permission p
                    on p.id = mp.permission_id
                   and p.tenant_id = mp.tenant_id
                   and p.deleted = false
                 where mp.tenant_id = ?
                   and mp.deleted = false
                   and mp.menu_id in (
                """);
        for (int i = 0; i < menuIds.size(); i++) {
            if (i > 0) sql.append(',');
            sql.append('?');
        }
        sql.append(") order by mp.menu_id, p.permission_code");
        List<Object> params = new ArrayList<>(menuIds.size() + 1);
        params.add(tenantId);
        params.addAll(menuIds);
        Map<Long, List<String>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql.toString(), rs -> {
            result.computeIfAbsent(rs.getLong("menu_id"), ignored -> new ArrayList<>())
                    .add(rs.getString("permission_code"));
        }, params.toArray());
        return result;
    }

    @Override
    public List<String> findMenuCodesByRoleId(long tenantId, long roleId) {
        return jdbcTemplate.queryForList("""
                        select m.menu_code
                          from sys_menu m
                          join sys_role_menu rm
                            on rm.menu_id = m.id
                           and rm.tenant_id = m.tenant_id
                           and rm.deleted = false
                         where m.tenant_id = ?
                           and rm.role_id = ?
                           and m.deleted = false
                         order by m.sort_no, m.id
                        """,
                String.class,
                tenantId,
                roleId
        );
    }

    @Override
    public long insert(long operatorUserId, Menu menu) {
        return jdbcTemplate.queryForObject(
                """
                insert into sys_menu
                    (tenant_id, parent_id, menu_code, menu_type, title_key,
                     route_path, component_key, icon, sort_no, visible, enabled,
                     created_by, created_at, updated_by, updated_at, deleted, version)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                returning id
                """,
                Long.class,
                menu.tenantId(),
                menu.parentId(),
                menu.menuCode(),
                menu.menuType(),
                menu.titleKey(),
                menu.routePath(),
                menu.componentKey(),
                menu.icon(),
                menu.sortNo(),
                menu.visible(),
                menu.enabled(),
                operatorUserId,
                operatorUserId
        );
    }

    @Override
    public boolean update(long operatorUserId, Menu menu) {
        int rows = jdbcTemplate.update(
                """
                update sys_menu
                   set parent_id = ?,
                       menu_code = ?,
                       menu_type = ?,
                       title_key = ?,
                       route_path = ?,
                       component_key = ?,
                       icon = ?,
                       sort_no = ?,
                       visible = ?,
                       enabled = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and version = ?
                   and %s
                """.formatted(DELETED_FALSE),
                menu.parentId(),
                menu.menuCode(),
                menu.menuType(),
                menu.titleKey(),
                menu.routePath(),
                menu.componentKey(),
                menu.icon(),
                menu.sortNo(),
                menu.visible(),
                menu.enabled(),
                operatorUserId,
                menu.tenantId(),
                menu.id(),
                menu.version()
        );
        return rows > 0;
    }

    @Override
    public boolean replaceRoleMenus(long operatorUserId, long tenantId, long roleId, List<Long> menuIds) {
        Integer roleCount = jdbcTemplate.queryForObject(
                "select count(*) from sys_role where tenant_id = ? and id = ? and " + DELETED_FALSE,
                Integer.class,
                tenantId,
                roleId
        );
        if (roleCount == null || roleCount == 0) {
            return false;
        }
        jdbcTemplate.update("delete from sys_role_menu where tenant_id = ? and role_id = ?", tenantId, roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            List<Object[]> params = new ArrayList<>(menuIds.size());
            for (Long menuId : menuIds) {
                params.add(new Object[]{tenantId, roleId, menuId, operatorUserId, operatorUserId});
            }
            jdbcTemplate.batchUpdate(
                    """
                    insert into sys_role_menu
                        (tenant_id, role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version)
                    values
                        (?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                    """,
                    params
            );
        }
        return true;
    }

    private Menu mapMenu(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Long parentId = rs.getObject("parent_id", Long.class);
        return new Menu(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                parentId,
                rs.getString("menu_code"),
                rs.getString("menu_type"),
                rs.getString("title_key"),
                rs.getString("route_path"),
                rs.getString("component_key"),
                rs.getString("icon"),
                rs.getInt("sort_no"),
                rs.getBoolean("visible"),
                rs.getBoolean("enabled"),
                rs.getInt("version"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                toOffsetDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
