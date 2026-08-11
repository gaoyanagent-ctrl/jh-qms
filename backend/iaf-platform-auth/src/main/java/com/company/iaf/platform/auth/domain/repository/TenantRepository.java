package com.company.iaf.platform.auth.domain.repository;

import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<TenantInfo> findByTenantCode(String tenantCode);

    Optional<Tenant> findById(long tenantId);

    List<Tenant> findPage(String keyword, int pageNo, int pageSize);

    long count(String keyword);

    boolean existsByTenantCode(String tenantCode);

    long createTenant(long operatorUserId, String tenantCode, String tenantName);

    boolean updateTenant(long operatorUserId, long tenantId, String tenantName, int expectedVersion);

    boolean updateStatus(long operatorUserId, long tenantId, TenantStatus status, int expectedVersion);

    void initializeDefaults(long operatorUserId, long tenantId, String tenantCode, String tenantName, String adminUsername, String adminPasswordHash);

    List<TenantQuota> listQuotas(long tenantId);

    Optional<TenantQuota> findQuota(long tenantId, String quotaKey);

    void upsertQuota(long operatorUserId, long tenantId, String quotaKey, long quotaLimit);

    long countActiveUsers(long tenantId);
}
