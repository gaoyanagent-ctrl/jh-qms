package com.company.iaf.platform.integration.application;

import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.core.event.DomainEventPublisher;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.integration.domain.model.OutboxEvent;
import com.company.iaf.platform.integration.domain.repository.OutboxEventRepository;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean(OutboxEventRepository.class)
public class OutboxApplicationService implements DomainEventPublisher {

    private static final long PLATFORM_TENANT_ID = 1L;

    private final OutboxEventRepository repository;
    private final List<OutboxEventHandler> handlers;

    public OutboxApplicationService(OutboxEventRepository repository, List<OutboxEventHandler> handlers) {
        this.repository = repository;
        this.handlers = handlers;
    }

    @RequiresPermission("platform:outbox:view")
    @Transactional(readOnly = true)
    public PageResult<OutboxEvent> list(long tenantId, String status, long pageNo, long pageSize) {
        assertPlatformOperator();
        long safePage = Math.max(1, pageNo);
        long safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
        long total = repository.count(tenantId, status);
        if (total == 0) {
            return PageResult.empty(safePage, safeSize);
        }
        return new PageResult<>(
                repository.findPage(tenantId, status, (int) safePage, (int) safeSize),
                total,
                safePage,
                safeSize
        );
    }

    @Override
    @Transactional
    public void publish(DomainEvent event) {
        repository.append(currentUserId(), UUID.randomUUID().toString(), event);
    }

    @Transactional
    public void dispatchPending(int limit) {
        for (OutboxEvent event : repository.findDispatchable(Math.max(1, Math.min(limit, 200)))) {
            try {
                handlers.stream()
                        .filter(handler -> handler.supports(event.eventType()))
                        .forEach(handler -> handler.handle(event));
                repository.markSent(currentUserId(), event.id());
            } catch (RuntimeException ex) {
                repository.markFailed(currentUserId(), event.id());
            }
        }
    }

    @Transactional
    @RequiresPermission("platform:outbox:retry")
    public void retry(long tenantId, long eventId) {
        assertPlatformOperator();
        repository.markPending(currentUserId(), tenantId, eventId);
    }

    private static long currentUserId() {
        return SecurityContext.getUserId().orElse(0L);
    }

    private static void assertPlatformOperator() {
        long tenantId = TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
        if (tenantId != PLATFORM_TENANT_ID) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Platform tenant context is required");
        }
    }
}
