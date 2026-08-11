package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;
import com.company.iaf.platform.auth.domain.model.UserStatus;
import com.company.iaf.platform.auth.domain.repository.PlatformUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.platform.auth.interfaces.dto.UserCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrgAssignRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrganizationsResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserUpdateRequest;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Application service for platform user management. All public methods
 * are protected by {@link RequiresPermission}; the aspect enforces the
 * check before the use case runs.
 *
 * <p>Audit fields are sourced from {@link SecurityContext} on every
 * write and passed explicitly to the repository, so the actor recorded
 * in {@code created_by} / {@code updated_by} is always the
 * authenticated user rather than the target entity's id.
 */
@Service
public class UserApplicationService {

    private final PlatformUserRepository userRepository;
    private final UserOrgRepository userOrgRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;

    @Autowired
    public UserApplicationService(
            PlatformUserRepository userRepository,
            UserOrgRepository userOrgRepository,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository
    ) {
        this.userRepository = userRepository;
        this.userOrgRepository = userOrgRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
    }

    public UserApplicationService(
            PlatformUserRepository userRepository,
            UserOrgRepository userOrgRepository,
            PasswordEncoder passwordEncoder
    ) {
        this(userRepository, userOrgRepository, passwordEncoder, null);
    }

    @RequiresPermission("platform:user:view")
    @Transactional(readOnly = true)
    public PageResult<UserResponse> listUsers(long tenantId, String keyword, long pageNo, long pageSize) {
        long safePage = Math.max(1, pageNo);
        long safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
        UserDataScope dataScope = currentUserDataScope();
        if (dataScope.empty()) {
            return PageResult.empty(safePage, safeSize);
        }
        long total = userRepository.count(tenantId, keyword, dataScope);
        if (total == 0) {
            return PageResult.empty(safePage, safeSize);
        }
        List<PlatformUser> users = userRepository.findPage(tenantId, keyword, dataScope, (int) safePage, (int) safeSize);
        return new PageResult<>(
                users.stream().map(user -> UserResponse.from(user, userOrgRepository.findByUserId(tenantId, user.id()))).toList(),
                total,
                safePage,
                safeSize
        );
    }

    @RequiresPermission("platform:user:view")
    @Transactional(readOnly = true)
    public UserResponse getUser(long tenantId, long id) {
        return userRepository.findById(tenantId, id)
                .map(user -> UserResponse.from(user, userOrgRepository.findByUserId(tenantId, user.id())))
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
    }

    @RequiresPermission("platform:auth:me")
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(long tenantId, long userId) {
        return userRepository.findById(tenantId, userId)
                .map(user -> UserResponse.from(user, userOrgRepository.findByUserId(tenantId, user.id())))
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
    }

    @RequiresPermission("platform:user:create")
    @Transactional
    public UserResponse createUser(long tenantId, UserCreateRequest request) {
        ensureTenantWritable(tenantId);
        ensureUserQuotaAvailable(tenantId);
        if (userRepository.existsByUsername(tenantId, request.username())) {
            throw new BusinessException(PlatformAuthErrorCode.USERNAME_ALREADY_EXISTS);
        }
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        PlatformUser draft = new PlatformUser(
                null,
                tenantId,
                request.username(),
                request.displayName(),
                request.mobile(),
                request.email(),
                UserStatus.ENABLED,
                null,
                0,
                null,
                null
        );
        String hash = passwordEncoder.encode(request.password());
        long id = userRepository.insert(operatorUserId, draft, hash);
        return userRepository.findById(tenantId, id)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "Inserted user could not be reloaded"));
    }

    @RequiresPermission("platform:user:update")
    @Transactional
    public UserResponse updateUser(long tenantId, long id, UserUpdateRequest request) {
        ensureTenantWritable(tenantId);
        PlatformUser existing = userRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
        PlatformUser updated = new PlatformUser(
                existing.id(),
                existing.tenantId(),
                existing.username(),
                request.displayName(),
                request.mobile(),
                request.email(),
                existing.status(),
                existing.primaryOrgId(),
                existing.version(),
                existing.createdAt(),
                existing.updatedAt()
        );
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        if (!userRepository.update(operatorUserId, updated)) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Platform user was modified concurrently");
        }
        return userRepository.findById(tenantId, id)
                .map(user -> UserResponse.from(user, userOrgRepository.findByUserId(tenantId, user.id())))
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
    }

    @RequiresPermission("platform:user:disable")
    @Transactional
    public UserResponse disableUser(long tenantId, long id) {
        ensureTenantWritable(tenantId);
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        if (operatorUserId == id) {
            throw new BusinessException(PlatformAuthErrorCode.CANNOT_DISABLE_SELF);
        }
        PlatformUser existing = userRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
        if (existing.status() == UserStatus.DISABLED) {
            return UserResponse.from(existing, userOrgRepository.findByUserId(tenantId, id));
        }
        if (!userRepository.updateStatus(operatorUserId, tenantId, id, UserStatus.DISABLED, existing.version())) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Platform user was modified concurrently");
        }
        return userRepository.findById(tenantId, id)
                .map(user -> UserResponse.from(user, userOrgRepository.findByUserId(tenantId, user.id())))
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND));
    }

    @RequiresPermission("platform:user:reset-password")
    @Transactional
    public void resetPassword(long tenantId, long id, String newPassword) {
        ensureTenantWritable(tenantId);
        if (userRepository.findById(tenantId, id).isEmpty()) {
            throw new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND);
        }
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        String hash = passwordEncoder.encode(newPassword);
        if (!userRepository.updatePassword(operatorUserId, tenantId, id, hash)) {
            throw new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND);
        }
    }

    @RequiresPermission("platform:user:view")
    @Transactional(readOnly = true)
    public UserOrganizationsResponse getUserOrganizations(long tenantId, long userId) {
        ensureUserExists(tenantId, userId);
        return UserOrganizationsResponse.from(userId, userOrgRepository.findByUserId(tenantId, userId));
    }

    @RequiresPermission("platform:user:update")
    @Transactional
    public UserOrganizationsResponse replaceUserOrganizations(long tenantId, long userId, UserOrgAssignRequest request) {
        ensureTenantWritable(tenantId);
        ensureUserExists(tenantId, userId);
        List<UserOrgAssignRequest.Item> items = request == null ? List.of() : request.safeOrganizations();
        validateOrganizationAssignments(tenantId, items);

        List<UserOrgAssignment> assignments = items.stream()
                .map(item -> new UserOrgAssignment(
                        item.orgId(),
                        item.primary(),
                        item.scopeWeight() == null ? 0 : item.scopeWeight(),
                        item.validFrom(),
                        item.validTo()
                ))
                .toList();
        Long primaryOrgId = assignments.stream()
                .filter(UserOrgAssignment::primary)
                .map(UserOrgAssignment::orgId)
                .findFirst()
                .orElse(null);
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        userOrgRepository.replaceUserOrgs(operatorUserId, tenantId, userId, assignments);
        userOrgRepository.updateUserPrimaryOrg(operatorUserId, tenantId, userId, primaryOrgId);
        return UserOrganizationsResponse.from(userId, userOrgRepository.findByUserId(tenantId, userId));
    }

    @RequiresPermission("platform:auth:me")
    @Transactional
    public UserResponse switchCurrentOrgContext(long tenantId, long userId, long orgId) {
        ensureTenantWritable(tenantId);
        long currentUserId = SecurityContext.getUserId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "User context is missing"));
        if (currentUserId != userId) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "Only the current user can switch organization context");
        }
        ensureUserExists(tenantId, userId);
        if (userOrgRepository.findByUserAndOrgId(tenantId, userId, orgId).isEmpty()) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "User is not assigned to the organization");
        }
        long operatorUserId = SecurityContext.getUserId().orElse(0L);
        userOrgRepository.updateUserPrimaryOrg(operatorUserId, tenantId, userId, orgId);
        SecurityContext.setCurrentOrgId(orgId);
        return getUser(tenantId, userId);
    }

    private void ensureUserExists(long tenantId, long userId) {
        if (userRepository.findById(tenantId, userId).isEmpty()) {
            throw new BusinessException(PlatformAuthErrorCode.USER_NOT_FOUND);
        }
    }

    private void ensureTenantWritable(long tenantId) {
        if (tenantRepository == null) {
            return;
        }
        tenantRepository.findById(tenantId)
                .filter(tenant -> tenant.status() == TenantStatus.ENABLED)
                .orElseThrow(() -> new BusinessException(PlatformAuthErrorCode.TENANT_DISABLED));
    }

    private void ensureUserQuotaAvailable(long tenantId) {
        if (tenantRepository == null) {
            return;
        }
        tenantRepository.findQuota(tenantId, TenantApplicationService.USER_COUNT_QUOTA)
                .ifPresent(quota -> {
                    long currentUsers = tenantRepository.countActiveUsers(tenantId);
                    if (quota.quotaLimit() >= 0 && currentUsers + 1 > quota.quotaLimit()) {
                        throw new BusinessException(PlatformAuthErrorCode.TENANT_QUOTA_EXCEEDED);
                    }
                });
    }

    private UserDataScope currentUserDataScope() {
        return SecurityContext.getCurrentOrgId()
                .map(UserDataScope::org)
                .orElseGet(UserDataScope::none);
    }

    private void validateOrganizationAssignments(long tenantId, List<UserOrgAssignRequest.Item> items) {
        Set<Long> orgIds = new LinkedHashSet<>();
        int primaryCount = 0;
        for (UserOrgAssignRequest.Item item : items) {
            if (!orgIds.add(item.orgId())) {
                throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "Duplicate organization assignment");
            }
            if (item.primary()) {
                primaryCount++;
            }
            if (item.validFrom() != null && item.validTo() != null && !item.validFrom().isBefore(item.validTo())) {
                throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "validFrom must be before validTo");
            }
        }
        if (!items.isEmpty() && primaryCount != 1) {
            throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "Exactly one primary organization is required");
        }
        if (!userOrgRepository.allOrgsExist(tenantId, orgIds)) {
            throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "Organization does not exist in current tenant");
        }
    }
}
