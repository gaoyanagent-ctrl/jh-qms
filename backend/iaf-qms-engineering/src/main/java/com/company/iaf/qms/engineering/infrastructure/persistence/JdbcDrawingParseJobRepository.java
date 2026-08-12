package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.DrawingParseJob;
import com.company.iaf.qms.engineering.domain.model.ParseJobStatus;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseJobRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDrawingParseJobRepository implements DrawingParseJobRepository {
    private final JdbcTemplate jdbc;
    public JdbcDrawingParseJobRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public long enqueue(long actorId, long tenantId, long orgId, long revisionId, long fileId,
                        String parserType, int attemptNo) {
        try {
            return jdbc.queryForObject("""
                insert into qms_drawing_parse_job
                  (tenant_id, org_id, revision_id, file_id, attempt_no, status, parser_type, created_by, updated_by)
                values (?, ?, ?, ?, ?, 'QUEUED', ?, ?, ?) returning id
                """, Long.class, tenantId, orgId, revisionId, fileId, attemptNo, parserType, actorId, actorId);
        } catch (DuplicateKeyException e) {
            return findLatest(tenantId, orgId, revisionId).orElseThrow().id();
        }
    }

    @Override
    public Optional<DrawingParseJob> findLatest(long tenantId, long orgId, long revisionId) {
        return jdbc.query("""
            select id, tenant_id, org_id, revision_id, file_id, attempt_no, status, parser_type,
                   error_code, error_message, version, created_at, updated_at
              from qms_drawing_parse_job
             where tenant_id=? and org_id=? and revision_id=? and deleted=false
             order by attempt_no desc limit 1
            """, this::map, tenantId, orgId, revisionId)
                .stream().findFirst();
    }

    @Override
    public Optional<DrawingParseJob> findById(long tenantId, long orgId, long id) {
        return jdbc.query("""
            select id, tenant_id, org_id, revision_id, file_id, attempt_no, status, parser_type,
                   error_code, error_message, version, created_at, updated_at
              from qms_drawing_parse_job
             where tenant_id=? and org_id=? and id=? and deleted=false
            """, this::map, tenantId, orgId, id).stream().findFirst();
    }

    @Override
    public List<DrawingParseJob> findLatestByDrawingId(long tenantId, long orgId, long drawingId) {
        return jdbc.query("""
            select distinct on (j.revision_id)
                   j.id, j.tenant_id, j.org_id, j.revision_id, j.file_id, j.attempt_no, j.status,
                   j.parser_type, j.error_code, j.error_message, j.version, j.created_at, j.updated_at
              from qms_drawing_parse_job j
              join qms_drawing_revision r on r.tenant_id=j.tenant_id and r.id=j.revision_id and r.deleted=false
             where j.tenant_id=? and j.org_id=? and r.drawing_id=? and j.deleted=false
             order by j.revision_id, j.attempt_no desc
            """, this::map, tenantId, orgId, drawingId);
    }

    @Override
    public List<DrawingParseJob> findQueued(int limit) {
        return jdbc.query("""
            select id, tenant_id, org_id, revision_id, file_id, attempt_no, status, parser_type,
                   error_code, error_message, version, created_at, updated_at
              from qms_drawing_parse_job
             where status='QUEUED' and deleted=false
             order by created_at, id limit ?
            """, this::map, Math.max(1, Math.min(limit, 20)));
    }

    @Override
    public boolean transition(long actorId, long tenantId, long orgId, long id, String fromStatus,
                              String targetStatus, String errorCode, String errorMessage, int expectedVersion) {
        return jdbc.update("""
            update qms_drawing_parse_job
               set status=?, error_code=?, error_message=?,
                   started_at=case when ?='RUNNING' then current_timestamp else started_at end,
                   completed_at=case when ? in ('SUCCEEDED','FAILED','CANCELLED') then current_timestamp else completed_at end,
                   updated_by=?, updated_at=current_timestamp, version=version+1
             where tenant_id=? and org_id=? and id=? and status=? and version=? and deleted=false
            """, targetStatus, errorCode, errorMessage, targetStatus, targetStatus, actorId,
                tenantId, orgId, id, fromStatus, expectedVersion) == 1;
    }

    private DrawingParseJob map(ResultSet rs, int row) throws SQLException {
        return new DrawingParseJob(rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getLong("revision_id"), rs.getLong("file_id"), rs.getInt("attempt_no"),
                ParseJobStatus.valueOf(rs.getString("status")), rs.getString("parser_type"),
                rs.getString("error_code"), rs.getString("error_message"), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at")));
    }
}
