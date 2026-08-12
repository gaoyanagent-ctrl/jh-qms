package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import com.company.iaf.platform.auth.domain.model.UserStatus;
import com.company.iaf.platform.auth.domain.repository.PlatformUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JdbcPlatformUserRepository implements PlatformUserRepository {

    private static final String DELETED_FALSE = "deleted = false";

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlatformUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PlatformUser> findById(long tenantId, long id) {
        List<PlatformUser> results = jdbcTemplate.query("""
                        select id, tenant_id, username, display_name, mobile, email,
                               status, primary_org_id, version, created_at, updated_at
                          from sys_user
                         where tenant_id = ? and id = ? and deleted = false
                        """,
                this::mapUser,
                tenantId, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<PlatformUser> findByUsername(long tenantId, String username) {
        List<PlatformUser> results = jdbcTemplate.query("""
                        select id, tenant_id, username, display_name, mobile, email,
                               status, primary_org_id, version, created_at, updated_at
                          from sys_user
                         where tenant_id = ? and username = ? and deleted = false
                        """,
                this::mapUser,
                tenantId, username
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByUsername(long tenantId, String username) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sys_user where tenant_id = ? and username = ? and deleted = false",
                Integer.class,
                tenantId, username
        );
        return count != null && count > 0;
    }

    @Override
    public List<PlatformUser> findPage(long tenantId, String keyword, UserDataScope dataScope, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1)) * Math.max(1, pageSize);
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        String orgPlaceholders = placeholders(dataScope.orgIds().size());
        return jdbcTemplate.query(String.format("""
                        select id, tenant_id, username, display_name, mobile, email,
                               status, primary_org_id, version, created_at, updated_at
                          from sys_user
                         where tenant_id = ?
                           and ( cast(? as varchar) is null
                                 or username ilike ?
                                 or display_name ilike ?
                                 or coalesce(mobile, '') ilike ?
                                 or coalesce(email, '') ilike ? )
                           and deleted = false
                           and exists (
                                 select 1
                                   from sys_user_org uo
                                  where uo.tenant_id = sys_user.tenant_id
                                    and uo.user_id = sys_user.id
                                    and uo.org_id in (%s)
                                    and uo.deleted = false
                               )
                         order by id
                         limit ? offset ?
                        """, orgPlaceholders),
                this::mapUser,
                queryParams(tenantId, pattern, dataScope, Math.max(1, pageSize), offset)
        );
    }

    @Override
    public long count(long tenantId, String keyword, UserDataScope dataScope) {
        String pattern = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        String orgPlaceholders = placeholders(dataScope.orgIds().size());
        Long count = jdbcTemplate.queryForObject(
                String.format("""
                select count(*) from sys_user
                 where tenant_id = ?
                   and ( cast(? as varchar) is null
                         or username ilike ?
                         or display_name ilike ?
                         or coalesce(mobile, '') ilike ?
                         or coalesce(email, '') ilike ? )
                   and """ + DELETED_FALSE + """
                   and exists (
                         select 1
                           from sys_user_org uo
                          where uo.tenant_id = sys_user.tenant_id
                            and uo.user_id = sys_user.id
                            and uo.org_id in (%s)
                            and uo.deleted = false
                       )
                """, orgPlaceholders),
                Long.class,
                queryParams(tenantId, pattern, dataScope)
        );
        return count == null ? 0L : count;
    }

    @Override
    public long insert(long operatorUserId, PlatformUser user, String passwordHash) {
        return jdbcTemplate.queryForObject(
                """
                insert into sys_user
                    (tenant_id, username, display_name, mobile, email,
                     password_hash, status, primary_org_id,
                     created_by, created_at, updated_by, updated_at,
                     deleted, version)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                returning id
                """,
                Long.class,
                user.tenantId(),
                user.username(),
                user.displayName(),
                user.mobile(),
                user.email(),
                passwordHash,
                user.status().name(),
                user.primaryOrgId(),
                operatorUserId,
                operatorUserId
        );
    }

    @Override
    public boolean update(long operatorUserId, PlatformUser user) {
        int rows = jdbcTemplate.update(
                """
                update sys_user
                   set display_name = ?,
                       mobile = ?,
                       email = ?,
                       primary_org_id = ?,
                       status = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and version = ?
                   and """ + DELETED_FALSE,
                user.displayName(),
                user.mobile(),
                user.email(),
                user.primaryOrgId(),
                user.status().name(),
                operatorUserId,
                user.tenantId(),
                user.id(),
                user.version()
        );
        return rows > 0;
    }

    @Override
    public boolean updateStatus(long operatorUserId, long tenantId, long id, UserStatus status, int expectedVersion) {
        int rows = jdbcTemplate.update(
                """
                update sys_user
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
    public boolean updatePassword(long operatorUserId, long tenantId, long id, String passwordHash) {
        int rows = jdbcTemplate.update(
                """
                update sys_user
                   set password_hash = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and """ + DELETED_FALSE,
                passwordHash,
                operatorUserId,
                tenantId,
                id
        );
        return rows > 0;
    }

    private PlatformUser mapUser(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new PlatformUser(
                resultSet.getLong("id"),
                resultSet.getLong("tenant_id"),
                resultSet.getString("username"),
                resultSet.getString("display_name"),
                resultSet.getString("mobile"),
                resultSet.getString("email"),
                UserStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("primary_org_id") == null
                        ? null : resultSet.getLong("primary_org_id"),
                resultSet.getInt("version"),
                toOffsetDateTime(resultSet.getTimestamp("created_at")),
                toOffsetDateTime(resultSet.getTimestamp("updated_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    private static String placeholders(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Data scope orgIds must not be empty");
        }
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> "?")
                .collect(Collectors.joining(", "));
    }

    private static Object[] queryParams(long tenantId, String pattern, UserDataScope dataScope, Object... tail) {
        List<Object> params = new java.util.ArrayList<>();
        params.add(tenantId);
        params.add(pattern);
        params.add(pattern);
        params.add(pattern);
        params.add(pattern);
        params.add(pattern);
        params.addAll(dataScope.orgIds());
        params.addAll(List.of(tail));
        return params.toArray();
    }
}
