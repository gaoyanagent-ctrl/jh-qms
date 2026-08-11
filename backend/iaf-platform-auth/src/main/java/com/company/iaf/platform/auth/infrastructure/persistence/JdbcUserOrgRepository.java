package com.company.iaf.platform.auth.infrastructure.persistence;

import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserOrgRepository implements UserOrgRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserOrgRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserOrg> findByUserId(long tenantId, long userId) {
        return jdbcTemplate.query("""
                        select uo.id, uo.tenant_id, uo.user_id, uo.org_id,
                               o.org_code, o.org_name, o.org_type,
                               uo.is_primary, uo.scope_weight, uo.valid_from, uo.valid_to,
                               uo.created_at, uo.updated_at
                          from sys_user_org uo
                          join sys_org o
                            on o.tenant_id = uo.tenant_id
                           and o.id = uo.org_id
                           and o.deleted = false
                         where uo.tenant_id = ?
                           and uo.user_id = ?
                           and uo.deleted = false
                           and (uo.valid_from is null or uo.valid_from <= current_timestamp)
                           and (uo.valid_to is null or uo.valid_to > current_timestamp)
                         order by uo.is_primary desc, uo.scope_weight desc, o.sort_no, o.id
                        """,
                this::mapUserOrg,
                tenantId,
                userId
        );
    }

    @Override
    public Optional<UserOrg> findByUserAndOrgId(long tenantId, long userId, long orgId) {
        List<UserOrg> results = jdbcTemplate.query("""
                        select uo.id, uo.tenant_id, uo.user_id, uo.org_id,
                               o.org_code, o.org_name, o.org_type,
                               uo.is_primary, uo.scope_weight, uo.valid_from, uo.valid_to,
                               uo.created_at, uo.updated_at
                          from sys_user_org uo
                          join sys_org o
                            on o.tenant_id = uo.tenant_id
                           and o.id = uo.org_id
                           and o.deleted = false
                         where uo.tenant_id = ?
                           and uo.user_id = ?
                           and uo.org_id = ?
                           and uo.deleted = false
                           and (uo.valid_from is null or uo.valid_from <= current_timestamp)
                           and (uo.valid_to is null or uo.valid_to > current_timestamp)
                        """,
                this::mapUserOrg,
                tenantId,
                userId,
                orgId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean allOrgsExist(long tenantId, Collection<Long> orgIds) {
        if (orgIds.isEmpty()) {
            return true;
        }
        String placeholders = String.join(",", orgIds.stream().map(ignored -> "?").toList());
        Object[] args = new Object[orgIds.size() + 1];
        args[0] = tenantId;
        int index = 1;
        for (Long orgId : orgIds) {
            args[index++] = orgId;
        }
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from sys_org where tenant_id = ? and id in (" + placeholders + ") and deleted = false",
                Long.class,
                args
        );
        return count != null && count == orgIds.size();
    }

    @Override
    public void replaceUserOrgs(long operatorUserId, long tenantId, long userId, List<UserOrgAssignment> assignments) {
        jdbcTemplate.update("""
                        update sys_user_org
                           set deleted = true,
                               version = version + 1,
                               updated_by = ?,
                               updated_at = current_timestamp
                         where tenant_id = ?
                           and user_id = ?
                           and deleted = false
                        """,
                operatorUserId,
                tenantId,
                userId
        );

        for (UserOrgAssignment assignment : assignments) {
            jdbcTemplate.update("""
                            insert into sys_user_org
                                (tenant_id, user_id, org_id, is_primary, scope_weight, valid_from, valid_to,
                                 created_by, created_at, updated_by, updated_at, deleted, version)
                            values
                                (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                            """,
                    tenantId,
                    userId,
                    assignment.orgId(),
                    assignment.primary(),
                    assignment.scopeWeight(),
                    toTimestamp(assignment.validFrom()),
                    toTimestamp(assignment.validTo()),
                    operatorUserId,
                    operatorUserId
            );
        }
    }

    @Override
    public void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId) {
        jdbcTemplate.update("""
                        update sys_user_org
                           set is_primary = false,
                               version = version + 1,
                               updated_by = ?,
                               updated_at = current_timestamp
                         where tenant_id = ?
                           and user_id = ?
                           and deleted = false
                           and is_primary = true
                        """,
                operatorUserId,
                tenantId,
                userId
        );

        if (primaryOrgId != null) {
            jdbcTemplate.update("""
                            update sys_user_org
                               set is_primary = true,
                                   version = version + 1,
                                   updated_by = ?,
                                   updated_at = current_timestamp
                             where tenant_id = ?
                               and user_id = ?
                               and org_id = ?
                               and deleted = false
                            """,
                    operatorUserId,
                    tenantId,
                    userId,
                    primaryOrgId
            );
        }

        jdbcTemplate.update("""
                        update sys_user
                           set primary_org_id = ?,
                               version = version + 1,
                               updated_by = ?,
                               updated_at = current_timestamp
                         where tenant_id = ?
                           and id = ?
                           and deleted = false
                        """,
                primaryOrgId,
                operatorUserId,
                tenantId,
                userId
        );
    }

    private UserOrg mapUserOrg(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new UserOrg(
                resultSet.getLong("id"),
                resultSet.getLong("tenant_id"),
                resultSet.getLong("user_id"),
                resultSet.getLong("org_id"),
                resultSet.getString("org_code"),
                resultSet.getString("org_name"),
                resultSet.getString("org_type"),
                resultSet.getBoolean("is_primary"),
                resultSet.getInt("scope_weight"),
                toOffsetDateTime(resultSet.getTimestamp("valid_from")),
                toOffsetDateTime(resultSet.getTimestamp("valid_to")),
                toOffsetDateTime(resultSet.getTimestamp("created_at")),
                toOffsetDateTime(resultSet.getTimestamp("updated_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    private static Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }
}
