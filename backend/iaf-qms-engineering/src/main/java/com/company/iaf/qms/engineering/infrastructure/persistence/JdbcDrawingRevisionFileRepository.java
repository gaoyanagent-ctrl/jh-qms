package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFile;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFileRole;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.repository.DrawingRevisionFileRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDrawingRevisionFileRepository implements DrawingRevisionFileRepository {
    private static final String SELECT = """
        select rf.revision_id, rf.file_role, f.id, f.tenant_id, f.org_id, f.original_name,
               f.media_type, f.file_extension, f.size_bytes, f.checksum_sha256,
               f.storage_bucket, f.storage_object_key, f.version, f.created_at
          from qms_drawing_revision_file rf
          join qms_file_object f on f.tenant_id=rf.tenant_id and f.id=rf.file_id and f.deleted=false
         where rf.tenant_id=? and rf.org_id=? and rf.revision_id=? and rf.deleted=false
        """;
    private final JdbcTemplate jdbc;
    public JdbcDrawingRevisionFileRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void attach(long actorId, long tenantId, long orgId, long revisionId, long fileId, DrawingRevisionFileRole role) {
        jdbc.update("""
            insert into qms_drawing_revision_file
                (tenant_id,org_id,revision_id,file_id,file_role,created_by,updated_by)
            values (?,?,?,?,?,?,?)
            """, tenantId, orgId, revisionId, fileId, role.name(), actorId, actorId);
    }

    @Override public Optional<DrawingRevisionFile> find(long tenantId, long orgId, long revisionId, DrawingRevisionFileRole role) {
        return jdbc.query(SELECT + " and rf.file_role=?", this::map, tenantId, orgId, revisionId, role.name()).stream().findFirst();
    }

    @Override public List<DrawingRevisionFile> findAll(long tenantId, long orgId, long revisionId) {
        return jdbc.query(SELECT + " order by rf.file_role", this::map, tenantId, orgId, revisionId);
    }

    private DrawingRevisionFile map(ResultSet rs, int row) throws SQLException {
        QmsFileObject file = new QmsFileObject(rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getString("original_name"), rs.getString("media_type"), rs.getString("file_extension"),
                rs.getLong("size_bytes"), rs.getString("checksum_sha256"), rs.getString("storage_bucket"),
                rs.getString("storage_object_key"), rs.getInt("version"), JdbcPartRepository.utc(rs.getTimestamp("created_at")));
        return new DrawingRevisionFile(rs.getLong("revision_id"), DrawingRevisionFileRole.valueOf(rs.getString("file_role")), file);
    }
}
