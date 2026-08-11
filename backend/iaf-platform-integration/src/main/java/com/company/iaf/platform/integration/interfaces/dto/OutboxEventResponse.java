package com.company.iaf.platform.integration.interfaces.dto;

import com.company.iaf.platform.integration.domain.model.OutboxEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEventStatus;

import java.time.OffsetDateTime;

public record OutboxEventResponse(
        long id,
        long tenantId,
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payloadJson,
        OutboxEventStatus status,
        int retryCount,
        OffsetDateTime nextRetryAt
) {
    public static OutboxEventResponse from(OutboxEvent event) {
        return new OutboxEventResponse(
                event.id(),
                event.tenantId(),
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.payloadJson(),
                event.status(),
                event.retryCount(),
                event.nextRetryAt()
        );
    }
}
