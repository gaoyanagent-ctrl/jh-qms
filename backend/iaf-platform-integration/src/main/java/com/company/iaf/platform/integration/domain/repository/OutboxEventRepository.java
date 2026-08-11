package com.company.iaf.platform.integration.domain.repository;

import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {

    OutboxEvent append(long operatorUserId, String eventId, DomainEvent event);

    List<OutboxEvent> findPage(long tenantId, String status, int pageNo, int pageSize);

    long count(long tenantId, String status);

    List<OutboxEvent> findDispatchable(int limit);

    void markSent(long operatorUserId, long id);

    void markFailed(long operatorUserId, long id);

    void markPending(long operatorUserId, long tenantId, long id);
}
