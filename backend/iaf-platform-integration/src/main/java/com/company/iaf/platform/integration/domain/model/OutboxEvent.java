package com.company.iaf.platform.integration.domain.model;

import java.time.OffsetDateTime;

public record OutboxEvent(
        Long id,
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
}
