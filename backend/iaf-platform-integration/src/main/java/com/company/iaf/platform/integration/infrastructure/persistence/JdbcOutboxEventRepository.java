package com.company.iaf.platform.integration.infrastructure.persistence;

import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEventStatus;
import com.company.iaf.platform.integration.domain.repository.OutboxEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOutboxEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OutboxEvent append(long operatorUserId, String eventId, DomainEvent event) {
        return jdbcTemplate.queryForObject("""
                        insert into platform_outbox_event (
                            tenant_id, event_id, aggregate_type, aggregate_id, event_type,
                            payload_json, status, created_by, updated_by
                        )
                        values (?, ?, ?, ?, ?, cast(? as jsonb), 'PENDING', ?, ?)
                        returning id, tenant_id, event_id, aggregate_type, aggregate_id, event_type,
                                  payload_json::text, status, retry_count, next_retry_at
                        """,
                this::mapEvent,
                event.tenantId(),
                eventId,
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.payloadJson(),
                operatorUserId,
                operatorUserId
        );
    }

    @Override
    public List<OutboxEvent> findPage(long tenantId, String status, int pageNo, int pageSize) {
        int offset = Math.max(0, pageNo - 1) * Math.max(1, pageSize);
        return jdbcTemplate.query("""
                        select id, tenant_id, event_id, aggregate_type, aggregate_id, event_type,
                               payload_json::text, status, retry_count, next_retry_at
                          from platform_outbox_event
                         where tenant_id = ?
                           and (? is null or status = ?)
                           and deleted = false
                         order by id desc
                         limit ? offset ?
                        """,
                this::mapEvent,
                tenantId, normalizeStatus(status), normalizeStatus(status), Math.max(1, pageSize), offset
        );
    }

    @Override
    public long count(long tenantId, String status) {
        Long count = jdbcTemplate.queryForObject("""
                        select count(*)
                          from platform_outbox_event
                         where tenant_id = ?
                           and (? is null or status = ?)
                           and deleted = false
                        """,
                Long.class,
                tenantId, normalizeStatus(status), normalizeStatus(status)
        );
        return count == null ? 0 : count;
    }

    @Override
    public List<OutboxEvent> findDispatchable(int limit) {
        return jdbcTemplate.query("""
                        select id, tenant_id, event_id, aggregate_type, aggregate_id, event_type,
                               payload_json::text, status, retry_count, next_retry_at
                          from platform_outbox_event
                         where status in ('PENDING', 'FAILED')
                           and (next_retry_at is null or next_retry_at <= current_timestamp)
                           and deleted = false
                         order by id
                         limit ?
                        """,
                this::mapEvent,
                Math.max(1, limit)
        );
    }

    @Override
    public void markSent(long operatorUserId, long id) {
        jdbcTemplate.update("""
                        update platform_outbox_event
                           set status = 'SENT',
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where id = ? and deleted = false
                        """,
                operatorUserId, id
        );
    }

    @Override
    public void markFailed(long operatorUserId, long id) {
        jdbcTemplate.update("""
                        update platform_outbox_event
                           set status = 'FAILED',
                               retry_count = retry_count + 1,
                               next_retry_at = current_timestamp + interval '1 minute',
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where id = ? and deleted = false
                        """,
                operatorUserId, id
        );
    }

    @Override
    public void markPending(long operatorUserId, long tenantId, long id) {
        jdbcTemplate.update("""
                        update platform_outbox_event
                           set status = 'PENDING',
                               next_retry_at = null,
                               updated_by = ?,
                               updated_at = current_timestamp,
                               version = version + 1
                         where tenant_id = ? and id = ? and deleted = false
                        """,
                operatorUserId, tenantId, id
        );
    }

    private OutboxEvent mapEvent(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new OutboxEvent(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getString("event_id"),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("payload_json"),
                OutboxEventStatus.valueOf(rs.getString("status")),
                rs.getInt("retry_count"),
                toOffsetDateTime(rs.getTimestamp("next_retry_at"))
        );
    }

    private static String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim().toUpperCase();
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
