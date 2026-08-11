package com.company.iaf.platform.core.event;

public record DomainEvent(
        long tenantId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payloadJson
) {
}
