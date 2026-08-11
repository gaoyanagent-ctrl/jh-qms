package com.company.iaf.platform.integration.application;

import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEvent;
import com.company.iaf.platform.integration.domain.model.OutboxEventStatus;
import com.company.iaf.platform.integration.domain.repository.OutboxEventRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxApplicationServiceTest {

    private final InMemoryOutboxEventRepository repository = new InMemoryOutboxEventRepository();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        SecurityContext.setUserId(99L);
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void publishAppendsPendingEvent() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of());

        service.publish(new DomainEvent(1L, "Tenant", "1", "TenantCreatedEvent", "{\"tenantId\":1}"));

        assertThat(repository.events).hasSize(1);
        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(repository.events.getFirst().tenantId()).isEqualTo(1L);
    }

    @Test
    void dispatcherMarksSentWhenHandlerSucceeds() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of(new RecordingHandler(false)));
        service.publish(new DomainEvent(1L, "Tenant", "1", "TenantCreatedEvent", "{}"));

        service.dispatchPending(10);

        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.SENT);
    }

    @Test
    void dispatcherMarksFailedAndRetryMarksPending() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of(new RecordingHandler(true)));
        service.publish(new DomainEvent(1L, "Tenant", "1", "TenantCreatedEvent", "{}"));

        service.dispatchPending(10);
        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(repository.events.getFirst().retryCount()).isEqualTo(1);

        service.retry(1L, repository.events.getFirst().id());
        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.PENDING);
    }

    @Test
    void platformOperatorCanListAndRetryTargetTenantEvent() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of(new RecordingHandler(true)));
        service.publish(new DomainEvent(2L, "Tenant", "2", "TenantCreatedEvent", "{}"));

        assertThat(service.list(2L, null, 1, 20).records()).hasSize(1);

        service.dispatchPending(10);
        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.FAILED);

        service.retry(2L, repository.events.getFirst().id());

        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.PENDING);
    }

    @Test
    void retryRejectsNonPlatformTenantContext() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of());
        service.publish(new DomainEvent(2L, "Tenant", "2", "TenantCreatedEvent", "{}"));
        TenantContext.setTenantId(2L);

        assertThatThrownBy(() -> service.retry(2L, repository.events.getFirst().id()))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("COMMON_FORBIDDEN");
    }

    @Test
    void retryDoesNotUpdateEventFromAnotherTenant() {
        OutboxApplicationService service = new OutboxApplicationService(repository, List.of(new RecordingHandler(true)));
        service.publish(new DomainEvent(2L, "Tenant", "2", "TenantCreatedEvent", "{}"));
        service.dispatchPending(10);
        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.FAILED);

        service.retry(1L, repository.events.getFirst().id());

        assertThat(repository.events.getFirst().status()).isEqualTo(OutboxEventStatus.FAILED);
    }

    private static final class RecordingHandler implements OutboxEventHandler {

        private final boolean fail;

        private RecordingHandler(boolean fail) {
            this.fail = fail;
        }

        @Override
        public boolean supports(String eventType) {
            return "TenantCreatedEvent".equals(eventType);
        }

        @Override
        public void handle(OutboxEvent event) {
            if (fail) {
                throw new IllegalStateException("boom");
            }
        }
    }

    private static final class InMemoryOutboxEventRepository implements OutboxEventRepository {

        private final AtomicLong nextId = new AtomicLong(1);
        private final List<OutboxEvent> events = new ArrayList<>();

        @Override
        public OutboxEvent append(long operatorUserId, String eventId, DomainEvent event) {
            OutboxEvent outboxEvent = new OutboxEvent(nextId.getAndIncrement(), event.tenantId(), eventId,
                    event.aggregateType(), event.aggregateId(), event.eventType(), event.payloadJson(),
                    OutboxEventStatus.PENDING, 0, null);
            events.add(outboxEvent);
            return outboxEvent;
        }

        @Override
        public List<OutboxEvent> findPage(long tenantId, String status, int pageNo, int pageSize) {
            return events.stream()
                    .filter(event -> event.tenantId() == tenantId)
                    .filter(event -> status == null || event.status().name().equals(status))
                    .toList();
        }

        @Override
        public long count(long tenantId, String status) {
            return findPage(tenantId, status, 1, 200).size();
        }

        @Override
        public List<OutboxEvent> findDispatchable(int limit) {
            return events.stream()
                    .filter(event -> event.status() == OutboxEventStatus.PENDING || event.status() == OutboxEventStatus.FAILED)
                    .limit(limit)
                    .toList();
        }

        @Override
        public void markSent(long operatorUserId, long id) {
            replace(id, OutboxEventStatus.SENT, 0);
        }

        @Override
        public void markFailed(long operatorUserId, long id) {
            OutboxEvent event = find(id);
            replace(id, OutboxEventStatus.FAILED, event.retryCount() + 1);
        }

        @Override
        public void markPending(long operatorUserId, long tenantId, long id) {
            OutboxEvent event = find(tenantId, id);
            if (event == null) {
                return;
            }
            replace(id, OutboxEventStatus.PENDING, event.retryCount());
        }

        private OutboxEvent find(long id) {
            return events.stream().filter(event -> event.id() == id).findFirst().orElseThrow();
        }

        private OutboxEvent find(long tenantId, long id) {
            return events.stream()
                    .filter(event -> event.tenantId() == tenantId && event.id() == id)
                    .findFirst()
                    .orElse(null);
        }

        private void replace(long id, OutboxEventStatus status, int retryCount) {
            for (int index = 0; index < events.size(); index++) {
                OutboxEvent event = events.get(index);
                if (event.id() == id) {
                    events.set(index, new OutboxEvent(event.id(), event.tenantId(), event.eventId(),
                            event.aggregateType(), event.aggregateId(), event.eventType(), event.payloadJson(),
                            status, retryCount, event.nextRetryAt()));
                    return;
                }
            }
        }
    }
}
