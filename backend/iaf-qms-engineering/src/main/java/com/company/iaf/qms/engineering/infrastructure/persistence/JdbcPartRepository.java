package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.Part;
import com.company.iaf.qms.engineering.domain.model.PartStatus;
import com.company.iaf.qms.engineering.domain.repository.PartRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPartRepository implements PartRepository {

    private static final String SELECT_COLUMNS = """
            id, tenant_id, org_id, part_no, material_no, part_name, customer_id,
            vehicle_model, supplier_id, importance_level, status, version, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Part> findById(long tenantId, long orgId, long id) {
        List<Part> results = jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_part where tenant_id = ? and org_id = ? and id = ? and deleted = false",
                this::map, tenantId, orgId, id
        );
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByPartNo(long tenantId, long orgId, String partNo) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from qms_part where tenant_id = ? and org_id = ? and part_no = ? and deleted = false",
                Integer.class, tenantId, orgId, partNo
        );
        return count != null && count > 0;
    }

    @Override
    public List<Part> findPage(long tenantId, long orgId, String keyword, long offset, int pageSize) {
        if (keyword == null) {
            return jdbcTemplate.query(
                    "select " + SELECT_COLUMNS + " from qms_part where tenant_id = ? and org_id = ? and deleted = false order by created_at desc, id desc limit ? offset ?",
                    this::map, tenantId, orgId, pageSize, offset
            );
        }
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_part where tenant_id = ? and org_id = ? and deleted = false "
                        + "and (part_no ilike ? or part_name ilike ? or material_no ilike ?) "
                        + "order by created_at desc, id desc limit ? offset ?",
                this::map, tenantId, orgId, pattern, pattern, pattern, pageSize, offset
        );
    }

    @Override
    public long count(long tenantId, long orgId, String keyword) {
        Long count;
        if (keyword == null) {
            count = jdbcTemplate.queryForObject(
                    "select count(*) from qms_part where tenant_id = ? and org_id = ? and deleted = false",
                    Long.class, tenantId, orgId
            );
        } else {
            String pattern = "%" + keyword + "%";
            count = jdbcTemplate.queryForObject(
                    "select count(*) from qms_part where tenant_id = ? and org_id = ? and deleted = false "
                            + "and (part_no ilike ? or part_name ilike ? or material_no ilike ?)",
                    Long.class, tenantId, orgId, pattern, pattern, pattern
            );
        }
        return count == null ? 0 : count;
    }

    @Override
    public long insert(long operatorUserId, Part part) {
        return jdbcTemplate.queryForObject(
                """
                insert into qms_part
                    (tenant_id, org_id, part_no, material_no, part_name, customer_id,
                     vehicle_model, supplier_id, importance_level, status,
                     created_by, updated_by, deleted, version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, 0)
                returning id
                """,
                Long.class,
                part.tenantId(), part.orgId(), part.partNo(), part.materialNo(), part.partName(),
                part.customerId(), part.vehicleModel(), part.supplierId(), part.importanceLevel(),
                part.status().name(), operatorUserId, operatorUserId
        );
    }

    private Part map(ResultSet rs, int rowNum) throws SQLException {
        return new Part(
                rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getString("part_no"), rs.getString("material_no"), rs.getString("part_name"),
                nullableLong(rs, "customer_id"), rs.getString("vehicle_model"),
                nullableLong(rs, "supplier_id"), rs.getString("importance_level"),
                PartStatus.valueOf(rs.getString("status")), rs.getInt("version"),
                utc(rs.getTimestamp("created_at")), utc(rs.getTimestamp("updated_at"))
        );
    }

    static Long nullableLong(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).longValue();
    }

    static OffsetDateTime utc(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
