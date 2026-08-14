package com.company.iaf.platform.org.infrastructure.persistence;

import com.company.iaf.platform.org.domain.model.Org;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;
import com.company.iaf.platform.org.domain.repository.OrgRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOrgRepository implements OrgRepository {

    private static final String DELETED_FALSE = "deleted = false";

    private static final String SELECT_COLUMNS = """
            id, tenant_id, parent_id, org_code, org_name, org_type,
            status, sort_no, version, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcOrgRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Org> findById(long tenantId, long id) {
        List<Org> results = jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from sys_org where tenant_id = ? and id = ? and " + DELETED_FALSE,
                this::mapOrg,
                tenantId, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByOrgCode(long tenantId, String orgCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sys_org where tenant_id = ? and org_code = ? and " + DELETED_FALSE,
                Integer.class,
                tenantId, orgCode
        );
        return count != null && count > 0;
    }

    @Override
    public List<Org> findAll(long tenantId) {
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + """
                          from sys_org
                         where tenant_id = ? and deleted = false
                         order by coalesce(parent_id, 0), sort_no, id
                        """,
                this::mapOrg,
                tenantId
        );
    }

    @Override
    public long insert(long operatorUserId, Org org) {
        return jdbcTemplate.queryForObject(
                """
                insert into sys_org
                    (tenant_id, parent_id, org_code, org_name, org_type,
                     status, sort_no,
                     created_by, created_at, updated_by, updated_at,
                     deleted, version)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, current_timestamp, false, 0)
                returning id
                """,
                Long.class,
                org.tenantId(),
                org.parentId(),
                org.orgCode(),
                org.orgName(),
                org.orgType().name(),
                org.status().name(),
                org.sortNo(),
                operatorUserId,
                operatorUserId
        );
    }

    @Override
    public boolean update(long operatorUserId, Org org) {
        int rows = jdbcTemplate.update(
                """
                update sys_org
                   set parent_id = ?,
                       org_code = ?,
                       org_name = ?,
                       org_type = ?,
                       status = ?,
                       sort_no = ?,
                       version = version + 1,
                       updated_by = ?,
                       updated_at = current_timestamp
                 where tenant_id = ?
                   and id = ?
                   and version = ?
                   and deleted = false
                """,
                org.parentId(),
                org.orgCode(),
                org.orgName(),
                org.orgType().name(),
                org.status().name(),
                org.sortNo(),
                operatorUserId,
                org.tenantId(),
                org.id(),
                org.version()
        );
        return rows > 0;
    }

    private Org mapOrg(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Org(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getObject("parent_id") == null ? null : rs.getLong("parent_id"),
                rs.getString("org_code"),
                rs.getString("org_name"),
                OrgType.valueOf(rs.getString("org_type")),
                OrgStatus.valueOf(rs.getString("status")),
                rs.getInt("sort_no"),
                rs.getInt("version"),
                toOffsetDateTime(rs.getTimestamp("created_at")),
                toOffsetDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
