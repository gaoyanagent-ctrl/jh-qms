package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.DrawingParseResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDrawingParseResultRepository implements DrawingParseResultRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public JdbcDrawingParseResultRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public void save(long actorId, long tenantId, long orgId, long revisionId, long parseJobId,
                     long sourceFileId, DrawingParseResult result) {
        jdbc.update("""
            insert into qms_drawing_intermediate_model
              (tenant_id, org_id, revision_id, parse_job_id, schema_version, document_id,
               revision_code, model_json, created_by, updated_by)
            values (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
            """, tenantId, orgId, revisionId, parseJobId, result.schemaVersion(), result.documentId(),
                result.revisionCode(), json(result.modelJson()), actorId, actorId);
        for (DrawingEntity entity : result.entities()) {
            jdbc.update("""
                insert into qms_drawing_entity
                  (tenant_id, org_id, revision_id, parse_job_id, entity_id, source_entity_handle,
                   entity_type, layer_name, sheet_no, bbox_x, bbox_y, bbox_w, bbox_h,
                   geometry_json, raw_text, normalized_text, style_json, created_by, updated_by)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?::jsonb, ?, ?)
                """, tenantId, orgId, revisionId, parseJobId, entity.entityId(),
                    entity.sourceEntityHandle(), entity.entityType().name(), entity.layerName(),
                    entity.sheetNo(), entity.bboxX(), entity.bboxY(), entity.bboxW(), entity.bboxH(),
                    json(entity.geometry()), entity.rawText(), entity.normalizedText(), json(entity.style()),
                    actorId, actorId);
        }
        for (SourceEvidence item : result.evidence()) {
            jdbc.update("""
                insert into qms_source_evidence
                  (tenant_id, org_id, source_file_id, drawing_revision_id, parse_job_id, evidence_key, entity_id,
                   entity_handle, sheet_no, page_no, bbox_x, bbox_y, bbox_w, bbox_h, raw_text,
                   normalized_text, extractor_type, extractor_version, model_name, model_version,
                   confidence, created_by, updated_by)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, tenantId, orgId, sourceFileId, revisionId, parseJobId, item.evidenceKey(), item.entityId(),
                    item.entityHandle(), item.sheetNo(), item.pageNo(), item.bboxX(), item.bboxY(),
                    item.bboxW(), item.bboxH(), item.rawText(), item.normalizedText(),
                    item.extractorType().name(), item.extractorVersion(), item.modelName(),
                    item.modelVersion(), item.confidence(), actorId, actorId);
        }
    }

    @Override
    public Optional<DrawingIntermediateModel> findModel(long tenantId, long orgId, long revisionId) {
        return jdbc.query("""
            select id, tenant_id, org_id, revision_id, parse_job_id, schema_version, document_id,
                   revision_code, model_json, version, created_at, updated_at
              from qms_drawing_intermediate_model
             where tenant_id=? and org_id=? and revision_id=? and deleted=false
            """, this::mapModel, tenantId, orgId, revisionId).stream().findFirst();
    }

    @Override
    public List<DrawingEntity> findEntities(long tenantId, long orgId, long revisionId) {
        return jdbc.query("""
            select id, tenant_id, org_id, revision_id, parse_job_id, entity_id, source_entity_handle,
                   entity_type, layer_name, sheet_no, bbox_x, bbox_y, bbox_w, bbox_h, geometry_json,
                   raw_text, normalized_text, style_json, version, created_at, updated_at
              from qms_drawing_entity
             where tenant_id=? and org_id=? and revision_id=? and deleted=false
             order by sheet_no, id
            """, this::mapEntity, tenantId, orgId, revisionId);
    }

    @Override
    public List<SourceEvidence> findEvidence(long tenantId, long orgId, long revisionId) {
        return jdbc.query(evidenceSql(""), this::mapEvidence, tenantId, orgId, revisionId);
    }

    @Override
    public Optional<SourceEvidence> findEvidenceById(long tenantId, long orgId, long revisionId, long evidenceId) {
        return jdbc.query(evidenceSql(" and id=?"), this::mapEvidence,
                tenantId, orgId, revisionId, evidenceId).stream().findFirst();
    }

    private String evidenceSql(String suffix) {
        return """
            select id, tenant_id, org_id, source_file_id, drawing_revision_id, parse_job_id,
                   evidence_key, entity_id, entity_handle, sheet_no, page_no, bbox_x, bbox_y, bbox_w, bbox_h,
                   raw_text, normalized_text, extractor_type, extractor_version, model_name,
                   model_version, confidence, version, created_at, updated_at
              from qms_source_evidence
             where tenant_id=? and org_id=? and drawing_revision_id=? and deleted=false
            """ + suffix + " order by id";
    }

    private DrawingIntermediateModel mapModel(ResultSet rs, int row) throws SQLException {
        return new DrawingIntermediateModel(rs.getLong("id"), rs.getLong("tenant_id"),
                rs.getLong("org_id"), rs.getLong("revision_id"), rs.getLong("parse_job_id"),
                rs.getString("schema_version"), rs.getString("document_id"), rs.getString("revision_code"),
                read(rs.getString("model_json")), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at")));
    }

    private DrawingEntity mapEntity(ResultSet rs, int row) throws SQLException {
        return new DrawingEntity(rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getLong("revision_id"), rs.getLong("parse_job_id"), rs.getString("entity_id"),
                rs.getString("source_entity_handle"), DrawingEntityType.valueOf(rs.getString("entity_type")),
                rs.getString("layer_name"), rs.getString("sheet_no"), rs.getBigDecimal("bbox_x"),
                rs.getBigDecimal("bbox_y"), rs.getBigDecimal("bbox_w"), rs.getBigDecimal("bbox_h"),
                read(rs.getString("geometry_json")), rs.getString("raw_text"),
                rs.getString("normalized_text"), read(rs.getString("style_json")), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at")));
    }

    private SourceEvidence mapEvidence(ResultSet rs, int row) throws SQLException {
        return new SourceEvidence(rs.getLong("id"), rs.getLong("tenant_id"), rs.getLong("org_id"),
                rs.getLong("source_file_id"), rs.getLong("drawing_revision_id"),
                rs.getLong("parse_job_id"), rs.getString("evidence_key"), rs.getString("entity_id"), rs.getString("entity_handle"),
                rs.getString("sheet_no"), rs.getObject("page_no", Integer.class), rs.getBigDecimal("bbox_x"),
                rs.getBigDecimal("bbox_y"), rs.getBigDecimal("bbox_w"), rs.getBigDecimal("bbox_h"),
                rs.getString("raw_text"), rs.getString("normalized_text"),
                EvidenceExtractorType.valueOf(rs.getString("extractor_type")),
                rs.getString("extractor_version"), rs.getString("model_name"),
                rs.getString("model_version"), rs.getBigDecimal("confidence"), rs.getInt("version"),
                JdbcPartRepository.utc(rs.getTimestamp("created_at")),
                JdbcPartRepository.utc(rs.getTimestamp("updated_at")));
    }

    private String json(JsonNode node) { return node == null || node.isNull() ? null : node.toString(); }

    private JsonNode read(String value) throws SQLException {
        if (value == null) return null;
        try { return mapper.readTree(value); }
        catch (JsonProcessingException e) { throw new SQLException("Invalid stored JSON", e); }
    }
}
