package com.company.iaf.platform.permission.infrastructure.persistence;

import com.company.iaf.platform.permission.domain.model.Permission;
import com.company.iaf.platform.permission.domain.repository.PermissionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class JdbcPermissionRepository implements PermissionRepository {

    private static final String DELETED_FALSE = "deleted = false";

    private static final String SELECT_COLUMNS = """
            id, tenant_id, permission_code, permission_name,
            resource_type, module_code, action_code
            """;

    private static final String QUALIFIED_SELECT_COLUMNS = """
            p.id, p.tenant_id, p.permission_code, p.permission_name,
            p.resource_type, p.module_code, p.action_code
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Permission> findAll(long tenantId) {
        return jdbcTemplate.query(("""
                        select %s
                          from sys_permission
                         where tenant_id = ?
                           and %s
                         order by module_code, permission_code
                        """).formatted(SELECT_COLUMNS, DELETED_FALSE),
                this::mapPermission,
                tenantId
        );
    }

    @Override
    public List<Permission> findAllByCodes(long tenantId, Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        // Build a placeholder list for the IN clause; codes themselves are
        // bound through parameters below.
        StringBuilder sql = new StringBuilder("select ").append(SELECT_COLUMNS)
                .append(" from sys_permission where tenant_id = ? and ").append(DELETED_FALSE)
                .append(" and permission_code in (");
        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) sql.append(',');
            sql.append('?');
        }
        sql.append(')');
        List<Object> params = new ArrayList<>(codes.size() + 1);
        params.add(tenantId);
        params.addAll(codes);
        return jdbcTemplate.query(sql.toString(), this::mapPermission, params.toArray());
    }

    @Override
    public List<Permission> findAllByRoleId(long tenantId, long roleId) {
        return jdbcTemplate.query(("""
                        select %s
                          from sys_permission p
                          join sys_role_permission rp on rp.permission_id = p.id and rp.tenant_id = p.tenant_id
                         where p.tenant_id = ?
                           and rp.role_id = ?
                           and p.deleted = false
                           and rp.deleted = false
                         order by p.permission_code
                        """).formatted(QUALIFIED_SELECT_COLUMNS),
                this::mapPermission,
                tenantId, roleId
        );
    }

    private Permission mapPermission(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Permission(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getString("permission_code"),
                rs.getString("permission_name"),
                rs.getString("resource_type"),
                rs.getString("module_code"),
                rs.getString("action_code")
        );
    }
}
