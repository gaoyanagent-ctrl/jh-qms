package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.DrawingRevision;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionStatus;
import com.company.iaf.qms.engineering.domain.model.ParseStatus;
import com.company.iaf.qms.engineering.domain.model.ReviewStatus;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDrawingRevisionRepository implements DrawingRevisionRepository {

    private static final String SELECT_COLUMNS = """
            id, tenant_id, org_id, drawing_id, revision_code, revision_seq, file_id,
            file_type, effective_date, release_date, supersedes_revision_id, parse_status,
            review_status, status, checksum, released_by, released_at, version, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcDrawingRevisionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<DrawingRevision> findById(long tenantId, long orgId, long id) {
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_drawing_revision where tenant_id = ? and org_id = ? and id = ? and deleted = false",
                this::map, tenantId, orgId, id
        ).stream().findFirst();
    }

    @Override
    public boolean existsByRevisionCode(long tenantId, long drawingId, String revisionCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from qms_drawing_revision where tenant_id = ? and drawing_id = ? and revision_code = ? and deleted = false",
                Integer.class, tenantId, drawingId, revisionCode
        );
        return count != null && count > 0;
    }

    @Override
    public int reserveNextSequence(long tenantId, long drawingId) {
        jdbcTemplate.query("select pg_advisory_xact_lock(?)", rs -> null, drawingId);
        Integer next = jdbcTemplate.queryForObject(
                "select coalesce(max(revision_seq), 0) + 1 from qms_drawing_revision where tenant_id = ? and drawing_id = ?",
                Integer.class, tenantId, drawingId
        );
        return next == null ? 1 : next;
    }

    @Override
    public List<DrawingRevision> findByDrawingId(long tenantId, long orgId, long drawingId) {
        return jdbcTemplate.query(
                "select " + SELECT_COLUMNS + " from qms_drawing_revision where tenant_id = ? and org_id = ? and drawing_id = ? and deleted = false order by revision_seq desc",
                this::map, tenantId, orgId, drawingId
        );
    }

    @Override
    public long insert(long operatorUserId, DrawingRevision revision) {
        return jdbcTemplate.queryForObject(
                """
                insert into qms_drawing_revision
                    (tenant_id, org_id, drawing_id, revision_code, revision_seq,
                     effective_date, supersedes_revision_id, parse_status, review_status, status,
                     created_by, updated_by, deleted, version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, 0)
                returning id
                """,
                Long.class,
                revision.tenantId(), revision.orgId(), revision.drawingId(), revision.revisionCode(),
                revision.revisionSeq(), revision.effectiveDate(), revision.supersedesRevisionId(),
                revision.parseStatus().name(), revision.reviewStatus().name(), revision.status().name(),
                operatorUserId, operatorUserId
        );
    }

    @Override
    public boolean attachFile(long actorId, long tenantId, long orgId, long revisionId,
                              long fileId, String fileType, String checksum, int expectedVersion) {
        return jdbcTemplate.update("""
            update qms_drawing_revision set file_id=?, file_type=?, checksum=?,
                   updated_by=?, updated_at=current_timestamp, version=version+1
             where tenant_id=? and org_id=? and id=? and version=? and file_id is null and deleted=false
            """, fileId, fileType, checksum, actorId, tenantId, orgId, revisionId, expectedVersion) == 1;
    }

    private DrawingRevision map(ResultSet rs, int rowNum) throws SQLException {
        Date effectiveDate = rs.getDate("effective_date");
        Date releaseDate = rs.getDate("release_date");
        return new DrawingRevision(
                rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getLong("drawing_id"), rs.getString("revision_code"), rs.getInt("revision_seq"),
                JdbcPartRepository.nullableLong(rs, "file_id"), rs.getString("file_type"),
                effectiveDate == null ? null : effectiveDate.toLocalDate(),
                releaseDate == null ? null : releaseDate.toLocalDate(),
                JdbcPartRepository.nullableLong(rs, "supersedes_revision_id"),
                ParseStatus.valueOf(rs.getString("parse_status")),
                ReviewStatus.valueOf(rs.getString("review_status")),
                DrawingRevisionStatus.valueOf(rs.getString("status")), rs.getString("checksum"),
                JdbcPartRepository.nullableLong(rs, "released_by"),
                JdbcPartRepository.utc(rs.getTimestamp("released_at")), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at"))
        );
    }
}
