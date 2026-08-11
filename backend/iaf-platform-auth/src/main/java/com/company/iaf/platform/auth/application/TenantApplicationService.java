package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.interfaces.dto.TenantCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.TenantQuotaResponse;
import com.company.iaf.platform.auth.interfaces.dto.TenantResponse;
import com.company.iaf.platform.auth.interfaces.dto.TenantUpdateRequest;
import com.company.iaf.platform.core.event.DomainEvent;
import com.company.iaf.platform.core.event.DomainEventPublisher;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantApplicationService {

    private static final long PLATFORM_TENANT_ID = 1L;

    public static final String USER_COUNT_QUOTA = "USER_COUNT";

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;

    public TenantApplicationService(
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            DomainEventPublisher eventPublisher
    ) {
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @RequiresPermission("platform:tenant:view")
    @Transactional(readOnly = true)
    public PageResult<TenantResponse> listTenants(String keyword, long pageNo, long pageSize) {
        assertPlatformOperator();
        long safePage = Math.max(1, pageNo);
        long safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
        long total = tenantRepository.count(keyword);
        if (total == 0) {
            return PageResult.empty(safePage, safeSize);
        }
        return new PageResult<>(
                tenantRepository.findPage(keyword, (int) safePage, (int) safeSize)
                        .stream()
                        .map(TenantResponse::from)
                        .toList(),
                total,
                safePage,
                safeSize
        );
    }

    @RequiresPermission("platform:tenant:view")
    @Transactional(readOnly = true)
    public TenantResponse getTenant(long tenantId) {
        assertPlatformOperator();
        return TenantResponse.from(findTenant(tenantId));
    }

    @RequiresPermission("platform:tenant:create")
    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {
        assertPlatformOperator();
        if (tenantRepository.existsByTenantCode(request.tenantCode())) {
            throw new BusinessException(PlatformAuthErrorCode.TENANT_CODE_ALREADY_EXISTS);
        }
        long operatorUserId = currentUserId();
        String adminHash = passwordEncoder.encode(request.adminPassword());
        long tenantId = tenantRepository.createTenant(operatorUserId, request.tenantCode(), request.tenantName());
        tenantRepository.initializeDefaults(
                operatorUserId,
                tenantId,
                request.tenantCode(),
                request.tenantName(),
                request.adminUsername(),
                adminHash
        );
        eventPublisher.publish(new DomainEvent(
                tenantId,
                "Tenant",
                String.valueOf(tenantId),
                "TenantCreatedEvent",
                "{\"tenantId\":" + tenantId + ",\"tenantCode\":\"" + request.tenantCode() + "\"}"
        ));
        return getTenant(tenantId);
    }

    @RequiresPermission("platform:tenant:update")
    @Transactional
    public TenantResponse updateTenant(long tenantId, TenantUpdateRequest request) {
        assertPlatformOperator();
        Tenant existing = findTenant(tenantId);
        if (!tenantRepository.updateTenant(currentUserId(), tenantId, request.tenantName(), existing.version())) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Tenant was modified concurrently");
        }
        return getTenant(tenantId);
    }

    @RequiresPermission("platform:tenant:disable")
    @Transactional
    public TenantResponse disableTenant(long tenantId) {
        assertPlatformOperator();
        return updateStatus(tenantId, TenantStatus.DISABLED);
    }

    @RequiresPermission("platform:tenant:enable")
    @Transactional
    public TenantResponse enableTenant(long tenantId) {
        assertPlatformOperator();
        return updateStatus(tenantId, TenantStatus.ENABLED);
    }

    @RequiresPermission("platform:tenant-quota:view")
    @Transactional(readOnly = true)
    public List<TenantQuotaResponse> listQuotas(long tenantId) {
        assertPlatformOperator();
        findTenant(tenantId);
        return tenantRepository.listQuotas(tenantId).stream().map(TenantQuotaResponse::from).toList();
    }

    @RequiresPermission("platform:tenant-quota:update")
    @Transactional
    public TenantQuotaResponse updateQuota(long tenantId, String quotaKey, long quotaLimit) {
        assertPlatformOperator();
        findTenant(tenantId);
        tenantRepository.upsertQuota(currentUserId(), tenantId, quotaKey, quotaLimit);
        TenantQuota quota = tenantRepository.findQuota(tenantId, quotaKey)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "Tenant quota could not be reloaded"));
        return TenantQuotaResponse.from(quota);
    }

    private TenantResponse updateStatus(long tenantId, TenantStatus status) {
        Tenant existing = findTenant(tenantId);
        if (!tenantRepository.updateStatus(currentUserId(), tenantId, status, existing.version())) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Tenant was modified concurrently");
        }
        return getTenant(tenantId);
    }

    private Tenant findTenant(long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.TENANT_NOT_FOUND));
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
