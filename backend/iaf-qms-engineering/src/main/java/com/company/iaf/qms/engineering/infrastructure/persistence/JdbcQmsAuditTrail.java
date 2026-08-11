package com.company.iaf.qms.engineering.infrastructure.persistence;

import com.company.iaf.qms.engineering.application.QmsEngineeringErrorCode;
import com.company.iaf.qms.engineering.domain.repository.QmsAuditTrail;
import com.company.iaf.shared.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcQmsAuditTrail implements QmsAuditTrail {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcQmsAuditTrail(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(long tenantId, long actorId, String action, String objectType, long objectId, Object afterState) {
        try {
            String afterJson = objectMapper.writeValueAsString(afterState);
            jdbcTemplate.update(
                    """
                    insert into qms_audit_log
                        (tenant_id, actor_id, action, object_type, object_id, after_json,
                         source, trace_id, created_by, updated_by, deleted, version)
                    values (?, ?, ?, ?, ?, cast(? as jsonb), 'USER', ?, ?, ?, false, 0)
                    """,
                    tenantId, actorId, action, objectType, objectId, afterJson,
                    UUID.randomUUID().toString(), actorId, actorId
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(QmsEngineeringErrorCode.AUDIT_WRITE_FAILED);
        }
    }
}
