package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.Drawing;
import com.company.iaf.qms.engineering.domain.model.DrawingSourceSystem;
import com.company.iaf.qms.engineering.domain.model.DrawingStatus;
import com.company.iaf.qms.engineering.domain.model.DrawingType;
import com.company.iaf.qms.engineering.domain.repository.DrawingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDrawingRepository implements DrawingRepository {

    private static final String SELECT_COLUMNS = """
            id, tenant_id, org_id, part_id, drawing_no, drawing_name, drawing_type,
            source_system, status, version, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcDrawingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Drawing> findById(long tenantId, long orgId, long id) {
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_drawing where tenant_id = ? and org_id = ? and id = ? and deleted = false",
                this::map, tenantId, orgId, id
        ).stream().findFirst();
    }

    @Override
    public boolean existsByDrawingNo(long tenantId, long partId, String drawingNo) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from qms_drawing where tenant_id = ? and part_id = ? and drawing_no = ? and deleted = false",
                Integer.class, tenantId, partId, drawingNo
        );
        return count != null && count > 0;
    }

    @Override
    public List<Drawing> findByPartId(long tenantId, long orgId, long partId) {
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_drawing where tenant_id = ? and org_id = ? and part_id = ? and deleted = false order by drawing_no, id",
                this::map, tenantId, orgId, partId
        );
    }

    @Override
    public long insert(long operatorUserId, Drawing drawing) {
        return jdbcTemplate.queryForObject(
                """
                insert into qms_drawing
                    (tenant_id, org_id, part_id, drawing_no, drawing_name, drawing_type,
                     source_system, status, created_by, updated_by, deleted, version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, 0)
                returning id
                """,
                Long.class,
                drawing.tenantId(), drawing.orgId(), drawing.partId(), drawing.drawingNo(),
                drawing.drawingName(), drawing.drawingType().name(), drawing.sourceSystem().name(),
                drawing.status().name(), operatorUserId, operatorUserId
        );
    }

    private Drawing map(ResultSet rs, int rowNum) throws SQLException {
        return new Drawing(
                rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getLong("part_id"), rs.getString("drawing_no"), rs.getString("drawing_name"),
                DrawingType.valueOf(rs.getString("drawing_type")),
                DrawingSourceSystem.valueOf(rs.getString("source_system")),
                DrawingStatus.valueOf(rs.getString("status")), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at"))
        );
    }
}
