package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.repository.QmsFileObjectRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JdbcQmsFileObjectRepository implements QmsFileObjectRepository {
    private final JdbcTemplate jdbc;
    public JdbcQmsFileObjectRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long insert(long actorId, QmsFileObject f) {
        return jdbc.queryForObject("""
            insert into qms_file_object
              (tenant_id, org_id, original_name, media_type, file_extension, size_bytes,
               checksum_sha256, storage_bucket, storage_object_key, created_by, updated_by)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id
            """, Long.class, f.tenantId(), f.orgId(), f.originalName(), f.mediaType(),
                f.fileExtension(), f.sizeBytes(), f.checksumSha256(), f.storageBucket(),
                f.storageObjectKey(), actorId, actorId);
    }

    public Optional<QmsFileObject> findById(long tenantId, long orgId, long id) {
        return jdbc.query("""
            select id, tenant_id, org_id, original_name, media_type, file_extension, size_bytes,
                   checksum_sha256, storage_bucket, storage_object_key, version, created_at
              from qms_file_object where tenant_id=? and org_id=? and id=? and deleted=false
            """, (rs, row) -> new QmsFileObject(rs.getLong("id"), rs.getLong("tenant_id"),
                rs.getLong("org_id"), rs.getString("original_name"), rs.getString("media_type"),
                rs.getString("file_extension"), rs.getLong("size_bytes"), rs.getString("checksum_sha256"),
                rs.getString("storage_bucket"), rs.getString("storage_object_key"), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at"))), tenantId, orgId, id).stream().findFirst();
    }
}

