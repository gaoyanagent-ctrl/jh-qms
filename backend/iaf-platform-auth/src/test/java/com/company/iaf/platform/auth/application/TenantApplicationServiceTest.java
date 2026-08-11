package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.interfaces.dto.TenantCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.TenantUpdateRequest;
import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.core.event.DomainEventPublisher;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantApplicationServiceTest {

    private final InMemoryTenantRepository repository = new InMemoryTenantRepository();
    private final RecordingPublisher publisher = new RecordingPublisher();
    private TenantApplicationService service;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        SecurityContext.setUserId(99L);
        service = new TenantApplicationService(
                repository,
                PasswordEncoderFactories.createDelegatingPasswordEncoder(),
                publisher
        );
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
        SecurityContext.clear();
    }

    @Test
    void createTenantInitializesDefaultsAndPublishesOutboxEvent() {
        var response = service.createTenant(new TenantCreateRequest("acme", "Acme", "admin", "password123"));

        assertThat(response.tenantId()).isPositive();
        assertThat(response.status()).isEqualTo(TenantStatus.ENABLED);
        assertThat(repository.initializedTenantIds).containsExactly(response.tenantId());
        assertThat(repository.findQuota(response.tenantId(), TenantApplicationService.USER_COUNT_QUOTA)).isPresent();
        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst().eventType()).isEqualTo("TenantCreatedEvent");
        assertThat(publisher.events.getFirst().tenantId()).isEqualTo(response.tenantId());
    }

    @Test
    void createTenantRejectsDuplicateCode() {
        service.createTenant(new TenantCreateRequest("acme", "Acme", "admin", "password123"));

        assertThatThrownBy(() -> service.createTenant(new TenantCreateRequest("acme", "Acme 2", "admin", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_TENANT_CODE_ALREADY_EXISTS");
    }

    @Test
    void enableDisableAndUpdateTenant() {
        var created = service.createTenant(new TenantCreateRequest("acme", "Acme", "admin", "password123"));

        assertThat(service.disableTenant(created.tenantId()).status()).isEqualTo(TenantStatus.DISABLED);
        assertThat(service.enableTenant(created.tenantId()).status()).isEqualTo(TenantStatus.ENABLED);
        assertThat(service.updateTenant(created.tenantId(), new TenantUpdateRequest("Acme Updated")).tenantName())
                .isEqualTo("Acme Updated");
    }

    @Test
    void updateQuotaReloadsUsage() {
        var created = service.createTenant(new TenantCreateRequest("acme", "Acme", "admin", "password123"));

        var quota = service.updateQuota(created.tenantId(), TenantApplicationService.USER_COUNT_QUOTA, 10);

        assertThat(quota.quotaLimit()).isEqualTo(10);
        assertThat(quota.quotaUsed()).isEqualTo(1);
    }

    @Test
    void createTenantRejectsNonPlatformTenantContext() {
        TenantContext.setTenantId(2L);

        assertThatThrownBy(() -> service.createTenant(new TenantCreateRequest("acme", "Acme", "admin", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("COMMON_FORBIDDEN");
    }

    private static final class RecordingPublisher implements DomainEventPublisher {

        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }

    private static final class InMemoryTenantRepository implements TenantRepository {

        private final AtomicLong nextTenantId = new AtomicLong(1);
        private final Map<Long, Tenant> tenants = new ConcurrentHashMap<>();
        private final Map<Long, TenantQuota> quotas = new ConcurrentHashMap<>();
        private final List<Long> initializedTenantIds = new ArrayList<>();

        @Override
        public Optional<TenantInfo> findByTenantCode(String tenantCode) {
            return tenants.values().stream()
                    .filter(tenant -> tenant.tenantCode().equals(tenantCode))
                    .findFirst()
                    .map(tenant -> new TenantInfo(tenant.tenantId(), tenant.tenantCode(), tenant.status().name()));
        }

        @Override
        public Optional<Tenant> findById(long tenantId) {
            return Optional.ofNullable(tenants.get(tenantId));
        }

        @Override
        public List<Tenant> findPage(String keyword, int pageNo, int pageSize) {
            return tenants.values().stream().sorted(java.util.Comparator.comparingLong(Tenant::tenantId)).toList();
        }

        @Override
        public long count(String keyword) {
            return tenants.size();
        }

        @Override
        public boolean existsByTenantCode(String tenantCode) {
            return findByTenantCode(tenantCode).isPresent();
        }

        @Override
        public long createTenant(long operatorUserId, String tenantCode, String tenantName) {
            long tenantId = nextTenantId.getAndIncrement();
            tenants.put(tenantId, new Tenant(null, tenantId, tenantCode, tenantName, TenantStatus.ENABLED,
                    "PENDING", null, 0, null, null));
            return tenantId;
        }

        @Override
        public boolean updateTenant(long operatorUserId, long tenantId, String tenantName, int expectedVersion) {
            Tenant tenant = tenants.get(tenantId);
            if (tenant == null || tenant.version() != expectedVersion) {
                return false;
            }
            tenants.put(tenantId, new Tenant(tenant.id(), tenantId, tenant.tenantCode(), tenantName, tenant.status(),
                    tenant.initializationStatus(), tenant.initializationError(), tenant.version() + 1, null, null));
            return true;
        }

        @Override
        public boolean updateStatus(long operatorUserId, long tenantId, TenantStatus status, int expectedVersion) {
            Tenant tenant = tenants.get(tenantId);
            if (tenant == null || tenant.version() != expectedVersion) {
                return false;
            }
            tenants.put(tenantId, new Tenant(tenant.id(), tenantId, tenant.tenantCode(), tenant.tenantName(), status,
                    tenant.initializationStatus(), tenant.initializationError(), tenant.version() + 1, null, null));
            return true;
        }

        @Override
        public void initializeDefaults(long operatorUserId, long tenantId, String tenantCode, String tenantName, String adminUsername, String adminPasswordHash) {
            initializedTenantIds.add(tenantId);
            quotas.put(tenantId, new TenantQuota(tenantId, TenantApplicationService.USER_COUNT_QUOTA, 100, 1));
            Tenant tenant = tenants.get(tenantId);
            tenants.put(tenantId, new Tenant(tenant.id(), tenantId, tenantCode, tenantName, tenant.status(),
                    "COMPLETED", null, tenant.version() + 1, null, null));
        }

        @Override
        public List<TenantQuota> listQuotas(long tenantId) {
            return findQuota(tenantId, TenantApplicationService.USER_COUNT_QUOTA).stream().toList();
        }

        @Override
        public Optional<TenantQuota> findQuota(long tenantId, String quotaKey) {
            return Optional.ofNullable(quotas.get(tenantId)).filter(quota -> quota.quotaKey().equals(quotaKey));
        }

        @Override
        public void upsertQuota(long operatorUserId, long tenantId, String quotaKey, long quotaLimit) {
            quotas.put(tenantId, new TenantQuota(tenantId, quotaKey, quotaLimit, countActiveUsers(tenantId)));
        }

        @Override
        public long countActiveUsers(long tenantId) {
            return initializedTenantIds.contains(tenantId) ? 1 : 0;
        }
    }
}
