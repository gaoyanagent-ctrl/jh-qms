package com.company.iaf.platform.permission.infrastructure.persistence;

import com.company.iaf.platform.permission.domain.model.Role;
import com.company.iaf.platform.permission.domain.model.RoleStatus;
import com.company.iaf.platform.permission.domain.repository.RoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRoleRepository implements RoleRepository {

    private static final String DELETED_FALSE = "deleted = false";

    private static final String SELECT_COLUMNS = """
            id, tenant_id, role_code, role_name, role_type,
            status, version, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Role> findById(long tenantId, long id) {
        List<Role> results = jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from sys_role where tenant_id = ? and id = ? and " + DELETED_FALSE,
                this::mapRole,
                tenantId, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByRoleCode(long tenantId, String roleCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sys_role where tenant_id = ? and role_code = ? and " + DELETED_FALSE,
                Integer.class,
                tenantId, roleCode
        );
        return count != null && count > 0;
    }

    @Override
    public List<Role> findPage(long tenantId, String keyword, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1)) * Math.max(1, pageSize);
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        return jdbcTemplate.query("""
                        select """ + SELECT_COLUMNS + """
                          from sys_role
                         where tenant_id = ?
                           and ( ? is null
                                 or role_code ilike ?
                                 or role_name ilike ? )
                           and """ + DELETED_FALSE + """
                         order by id
                         limit ? offset ?
                        """,
                this::mapRole,
                tenantId, pattern, pattern, pattern,
                Math.max(1, pageSize), offset
        );
    }

    @Override
    public long count(long tenantId, String keyword) {
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        Long count = jdbcTemplate.queryForObject(
                """
                select count(*) from sys_role
                 where tenant_id = ?
                   and ( ? is null
                         or role_code ilike ?
                         or role_name ilike ? )
                   and """ + DELETED_FALSE,
                Long.class,
                tenantId, pattern, pattern, pattern
        );
        return count == null ? 0L : count;
    }

    @Override
    public long insert(long operatorUserId, Role role) {
        return jdbcTemplate.queryForObject(
                """
                insert into sys_role
                    (tenant_id, role_code, role_name, role_type, status,
                     created_by, created_at, updated_by, updated_at,
                     deleted, version)
                values
                    (?, ?, ?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                returning id
                """,
                Long.class,
                role.tenantId(),
                role.roleCode(),
                role.roleName(),
                role.roleType(),
                role.status().name(),
                operatorUserId,
                operatorUserId
        );
    }

    @Override
    public boolean update(long operatorUserId, Role role) {
        int rows = jdbcTemplate.update(
                """
                update sys_role
                   set role_code = ?,
                       role_name = ?,
                       role_type = ?,
                       status = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and version = ?
                   and """ + DELETED_FALSE,
                role.roleCode(),
                role.roleName(),
                role.roleType(),
                role.status().name(),
                operatorUserId,
                role.tenantId(),
                role.id(),
                role.version()
        );
        return rows > 0;
    }

    @Override
    public boolean updateStatus(long operatorUserId, long tenantId, long id, RoleStatus status, int expectedVersion) {
        int rows = jdbcTemplate.update(
                """
                update sys_role
                   set status = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and version = ?
                   and """ + DELETED_FALSE,
                status.name(),
                operatorUserId,
                tenantId,
                id,
                expectedVersion
        );
        return rows > 0;
    }

    @Override
    public boolean replacePermissions(long operatorUserId, long tenantId, long roleId, List<Long> permissionIds) {
        // Verify the role still exists for this tenant. Returning false
        // here lets the service layer surface ROLE_NOT_FOUND without
        // needing to inspect row counts from the deletes below.
        Integer roleCount = jdbcTemplate.queryForObject(
                "select count(*) from sys_role where tenant_id = ? and id = ? and " + DELETED_FALSE,
                Integer.class,
                tenantId, roleId
        );
        if (roleCount == null || roleCount == 0) {
            return false;
        }
        jdbcTemplate.update(
                "delete from sys_role_permission where tenant_id = ? and role_id = ?",
                tenantId, roleId
        );
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Object[]> params = new ArrayList<>(permissionIds.size());
            for (Long permissionId : permissionIds) {
                params.add(new Object[]{tenantId, roleId, permissionId, operatorUserId, operatorUserId});
            }
            jdbcTemplate.batchUpdate(
                    """
                    insert into sys_role_permission
                        (tenant_id, role_id, permission_id,
                         created_by, created_at, updated_by, updated_at,
                         deleted, version)
                    values
                        (?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                    """,
                    params
            );
        }
        return true;
    }

    private Role mapRole(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Role(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getString("role_code"),
                rs.getString("role_name"),
                rs.getString("role_type"),
                RoleStatus.valueOf(rs.getString("status")),
                rs.getInt("version"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                toOffsetDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
