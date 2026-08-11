package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;
import com.company.iaf.platform.auth.domain.model.UserStatus;
import com.company.iaf.platform.auth.domain.repository.PlatformUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.platform.auth.interfaces.dto.UserCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrgAssignRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserApplicationServiceTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final InMemoryUserOrgRepository userOrgRepository = new InMemoryUserOrgRepository();
    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(repository, userOrgRepository, passwordEncoder);
        SecurityContext.setUserId(99L);
    }

    @AfterEach
    void clear() {
        SecurityContext.clear();
    }

    @Test
    void createUserSucceedsAndPersistsEncodedPassword() {
        UserCreateRequest request = new UserCreateRequest(
                "alice", "password123", "Alice", "13800000000", "alice@example.com"
        );

        UserResponse response = service.createUser(1L, request);

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.status()).isEqualTo(UserStatus.ENABLED);
        Optional<PlatformUser> stored = repository.findById(1L, response.id());
        assertThat(stored).isPresent();
        assertThat(stored.get().displayName()).isEqualTo("Alice");
        String hash = repository.passwordHash(response.id()).orElseThrow();
        assertThat(passwordEncoder.matches("password123", hash)).isTrue();
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        service.createUser(1L, new UserCreateRequest("dup", "password123", "Dup", null, null));

        assertThatThrownBy(() -> service.createUser(1L, new UserCreateRequest("dup", "password123", "Dup 2", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_USERNAME_ALREADY_EXISTS");
    }

    @Test
    void createUserIsScopedToTenant() {
        service.createUser(1L, new UserCreateRequest("tenant1", "password123", "T1", null, null));

        // Different tenant must not see the same username as duplicate.
        UserResponse response = service.createUser(2L, new UserCreateRequest("tenant1", "password123", "T1", null, null));
        assertThat(response.id()).isNotNull();
        assertThat(response.tenantId()).isEqualTo(2L);
    }

    @Test
    void createUserRejectsDisabledTenant() {
        service = new UserApplicationService(repository, userOrgRepository, passwordEncoder,
                new StubTenantRepository(TenantStatus.DISABLED, 100));

        assertThatThrownBy(() -> service.createUser(1L, new UserCreateRequest("blocked", "password123", "Blocked", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_TENANT_DISABLED");
    }

    @Test
    void createUserRejectsWhenUserQuotaReached() {
        service = new UserApplicationService(repository, userOrgRepository, passwordEncoder,
                new StubTenantRepository(TenantStatus.ENABLED, 1));
        service.createUser(1L, new UserCreateRequest("first", "password123", "First", null, null));

        assertThatThrownBy(() -> service.createUser(1L, new UserCreateRequest("second", "password123", "Second", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_TENANT_QUOTA_EXCEEDED");
    }

    @Test
    void disableUserFlipsStatus() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("bob", "password123", "Bob", null, null));

        UserResponse disabled = service.disableUser(1L, created.id());

        assertThat(disabled.status()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void disableUserRejectsDisablingSelf() {
        long selfId = SecurityContext.getUserId().orElseThrow();
        repository.seedUser(selfId, 1L, "self", "self user");

        assertThatThrownBy(() -> service.disableUser(1L, selfId))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_CANNOT_DISABLE_SELF");
    }

    @Test
    void disableUserReturnsCurrentWhenAlreadyDisabled() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("carol", "password123", "Carol", null, null));
        service.disableUser(1L, created.id());

        UserResponse response = service.disableUser(1L, created.id());

        assertThat(response.status()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void updateUserChangesProfileFields() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("dave", "password123", "Dave", null, null));

        UserResponse updated = service.updateUser(1L, created.id(),
                new UserUpdateRequest("Dave Updated", "13900000000", "dave@example.com"));

        assertThat(updated.displayName()).isEqualTo("Dave Updated");
        assertThat(updated.mobile()).isEqualTo("13900000000");
        assertThat(updated.email()).isEqualTo("dave@example.com");
    }

    @Test
    void updateUserThrowsWhenUserMissing() {
        assertThatThrownBy(() -> service.updateUser(1L, 9_999L,
                new UserUpdateRequest("Ghost", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_USER_NOT_FOUND");
    }

    @Test
    void resetPasswordReplacesHash() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("erin", "password123", "Erin", null, null));

        service.resetPassword(1L, created.id(), "newSecret123");

        String newHash = repository.passwordHash(created.id()).orElseThrow();
        assertThat(passwordEncoder.matches("newSecret123", newHash)).isTrue();
        assertThat(passwordEncoder.matches("password123", newHash)).isFalse();
    }

    @Test
    void resetPasswordThrowsWhenUserMissing() {
        assertThatThrownBy(() -> service.resetPassword(1L, 9_999L, "newSecret123"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_USER_NOT_FOUND");
    }

    @Test
    void listUsersPagesAndFilters() {
        SecurityContext.setCurrentOrgId(10L);
        UserResponse frank = service.createUser(1L, new UserCreateRequest("frank", "password123", "Frank", null, null));
        UserResponse frank2 = service.createUser(1L, new UserCreateRequest("frank2", "password123", "Frank Two", null, null));
        UserResponse gina = service.createUser(1L, new UserCreateRequest("gina", "password123", "Gina", null, null));
        repository.assignOrg(frank.id(), 10L);
        repository.assignOrg(frank2.id(), 10L);
        repository.assignOrg(gina.id(), 10L);

        var page = service.listUsers(1L, "frank", 1L, 10L);

        assertThat(page.total()).isEqualTo(2L);
        assertThat(page.records()).hasSize(2)
                .allSatisfy(user -> assertThat(user.username()).contains("frank"));
    }

    @Test
    void listUsersReturnsEmptyWhenNoMatch() {
        SecurityContext.setCurrentOrgId(10L);
        UserResponse frank = service.createUser(1L, new UserCreateRequest("frank", "password123", "Frank", null, null));
        repository.assignOrg(frank.id(), 10L);

        var page = service.listUsers(1L, "no-such-user", 1L, 10L);

        assertThat(page.total()).isZero();
        assertThat(page.records()).isEmpty();
    }

    @Test
    void listUsersReturnsEmptyWhenUserHasNoDataScope() {
        UserResponse frank = service.createUser(1L, new UserCreateRequest("frank", "password123", "Frank", null, null));
        repository.assignOrg(frank.id(), 10L);

        var page = service.listUsers(1L, null, 1L, 10L);

        assertThat(page.total()).isZero();
        assertThat(page.records()).isEmpty();
    }

    @Test
    void listUsersFiltersByCurrentOrgDataScope() {
        SecurityContext.setCurrentOrgId(10L);
        UserResponse visible = service.createUser(1L, new UserCreateRequest("visible", "password123", "Visible", null, null));
        UserResponse hidden = service.createUser(1L, new UserCreateRequest("hidden", "password123", "Hidden", null, null));
        repository.assignOrg(visible.id(), 10L);
        repository.assignOrg(hidden.id(), 20L);

        var page = service.listUsers(1L, null, 1L, 10L);

        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.records()).extracting(UserResponse::username).containsExactly("visible");
    }

    @Test
    void getUserThrowsWhenMissing() {
        assertThatThrownBy(() -> service.getUser(1L, 9_999L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_AUTH_USER_NOT_FOUND");
    }

    @Test
    void replaceUserOrganizationsRequiresExactlyOnePrimaryWhenAssignmentsExist() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("harry", "password123", "Harry", null, null));

        assertThatThrownBy(() -> service.replaceUserOrganizations(1L, created.id(), new UserOrgAssignRequest(List.of(
                new UserOrgAssignRequest.Item(1L, false, 0, null, null),
                new UserOrgAssignRequest.Item(2L, false, 0, null, null)
        ))))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("COMMON_VALIDATION_FAILED");
    }

    @Test
    void replaceUserOrganizationsReturnsAssignedOrganizationsAndPrimaryOrg() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("ivy", "password123", "Ivy", null, null));

        var response = service.replaceUserOrganizations(1L, created.id(), new UserOrgAssignRequest(List.of(
                new UserOrgAssignRequest.Item(1L, true, 100, null, null),
                new UserOrgAssignRequest.Item(2L, false, 0, null, null)
        )));

        assertThat(response.primaryOrgId()).isEqualTo(1L);
        assertThat(response.organizations()).extracting("orgId").containsExactly(1L, 2L);
    }

    @Test
    void switchCurrentOrgContextUpdatesPrimaryAssignmentAndReturnedUser() {
        UserResponse created = service.createUser(1L, new UserCreateRequest("jane", "password123", "Jane", null, null));
        service.replaceUserOrganizations(1L, created.id(), new UserOrgAssignRequest(List.of(
                new UserOrgAssignRequest.Item(1L, true, 100, null, null),
                new UserOrgAssignRequest.Item(2L, false, 0, null, null)
        )));
        SecurityContext.setUserId(created.id());

        UserResponse response = service.switchCurrentOrgContext(1L, created.id(), 2L);

        assertThat(response.primaryOrgId()).isEqualTo(2L);
        assertThat(response.organizations())
                .filteredOn(item -> item.primary())
                .extracting("orgId")
                .containsExactly(2L);
        assertThat(SecurityContext.getCurrentOrgId()).contains(2L);
    }

    /**
     * Test double for the persistence contract. Tracks user state plus
     * password hashes in a side map so the service can verify the
     * encoder actually ran.
     */
    private static final class InMemoryUserRepository implements PlatformUserRepository {

        private final AtomicLong nextId = new AtomicLong(1);
        private final Map<Long, PlatformUser> users = new ConcurrentHashMap<>();
        private final Map<Long, String> hashes = new ConcurrentHashMap<>();
        private final Map<Long, List<Long>> userOrgIds = new ConcurrentHashMap<>();

        void seedUser(long id, long tenantId, String username, String displayName) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            users.put(id, new PlatformUser(id, tenantId, username, displayName, null, null,
                    UserStatus.ENABLED, null, 0, now, now));
        }

        Optional<String> passwordHash(long id) {
            return Optional.ofNullable(hashes.get(id));
        }

        void assignOrg(long userId, long orgId) {
            userOrgIds.compute(userId, (ignored, current) -> {
                List<Long> next = new ArrayList<>(current == null ? List.of() : current);
                next.add(orgId);
                return List.copyOf(next);
            });
        }

        @Override
        public Optional<PlatformUser> findById(long tenantId, long id) {
            return Optional.ofNullable(users.get(id)).filter(user -> user.tenantId() == tenantId);
        }

        @Override
        public Optional<PlatformUser> findByUsername(long tenantId, String username) {
            return users.values().stream()
                    .filter(user -> user.tenantId() == tenantId && user.username().equals(username))
                    .findFirst();
        }

        @Override
        public boolean existsByUsername(long tenantId, String username) {
            return users.values().stream()
                    .anyMatch(user -> user.tenantId() == tenantId && user.username().equals(username));
        }

        @Override
        public List<PlatformUser> findPage(long tenantId, String keyword, UserDataScope dataScope, int pageNo, int pageSize) {
            String needle = keyword == null ? null : keyword.trim().toLowerCase();
            List<PlatformUser> filtered = new ArrayList<>();
            for (PlatformUser user : users.values()) {
                if (user.tenantId() != tenantId) continue;
                if (!inScope(user.id(), dataScope)) continue;
                if (needle == null
                        || user.username().toLowerCase().contains(needle)
                        || user.displayName().toLowerCase().contains(needle)
                        || (user.mobile() != null && user.mobile().toLowerCase().contains(needle))
                        || (user.email() != null && user.email().toLowerCase().contains(needle))) {
                    filtered.add(user);
                }
            }
            filtered.sort((a, b) -> Long.compare(a.id(), b.id()));
            int from = Math.max(0, (pageNo - 1) * pageSize);
            int to = Math.min(filtered.size(), from + pageSize);
            return filtered.subList(from, to);
        }

        @Override
        public long count(long tenantId, String keyword, UserDataScope dataScope) {
            String needle = keyword == null ? null : keyword.trim().toLowerCase();
            return users.values().stream()
                    .filter(user -> user.tenantId() == tenantId)
                    .filter(user -> inScope(user.id(), dataScope))
                    .filter(user -> needle == null
                            || user.username().toLowerCase().contains(needle)
                            || user.displayName().toLowerCase().contains(needle)
                            || (user.mobile() != null && user.mobile().toLowerCase().contains(needle))
                            || (user.email() != null && user.email().toLowerCase().contains(needle)))
                    .count();
        }

        private boolean inScope(long userId, UserDataScope dataScope) {
            return userOrgIds.getOrDefault(userId, List.of()).stream().anyMatch(dataScope.orgIds()::contains);
        }

        @Override
        public long insert(long operatorUserId, PlatformUser user, String passwordHash) {
            long id = nextId.getAndIncrement();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            users.put(id, new PlatformUser(id, user.tenantId(), user.username(), user.displayName(),
                    user.mobile(), user.email(), user.status(), user.primaryOrgId(), 0, now, now));
            hashes.put(id, passwordHash);
            return id;
        }

        @Override
        public boolean update(long operatorUserId, PlatformUser user) {
            PlatformUser current = users.get(user.id());
            if (current == null || current.version() != user.version()) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            users.put(user.id(), new PlatformUser(user.id(), user.tenantId(), user.username(), user.displayName(),
                    user.mobile(), user.email(), user.status(), user.primaryOrgId(),
                    user.version() + 1, current.createdAt(), now));
            return true;
        }

        @Override
        public boolean updateStatus(long operatorUserId, long tenantId, long id, UserStatus status, int expectedVersion) {
            PlatformUser current = users.get(id);
            if (current == null || current.tenantId() != tenantId || current.version() != expectedVersion) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            users.put(id, new PlatformUser(current.id(), current.tenantId(), current.username(), current.displayName(),
                    current.mobile(), current.email(), status, current.primaryOrgId(),
                    current.version() + 1, current.createdAt(), now));
            return true;
        }

        @Override
        public boolean updatePassword(long operatorUserId, long tenantId, long id, String passwordHash) {
            PlatformUser current = users.get(id);
            if (current == null || current.tenantId() != tenantId) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            hashes.put(id, passwordHash);
            users.put(id, new PlatformUser(current.id(), current.tenantId(), current.username(), current.displayName(),
                    current.mobile(), current.email(), current.status(), current.primaryOrgId(),
                    current.version() + 1, current.createdAt(), now));
            return true;
        }
    }

    private final class StubTenantRepository implements TenantRepository {

        private final TenantStatus status;
        private final long userLimit;

        private StubTenantRepository(TenantStatus status, long userLimit) {
            this.status = status;
            this.userLimit = userLimit;
        }

        @Override
        public Optional<TenantInfo> findByTenantCode(String tenantCode) {
            return Optional.of(new TenantInfo(1L, tenantCode, status.name()));
        }

        @Override
        public Optional<Tenant> findById(long tenantId) {
            return Optional.of(new Tenant(null, tenantId, "default", "Default", status,
                    "COMPLETED", null, 0, null, null));
        }

        @Override
        public List<Tenant> findPage(String keyword, int pageNo, int pageSize) {
            return List.of();
        }

        @Override
        public long count(String keyword) {
            return 1;
        }

        @Override
        public boolean existsByTenantCode(String tenantCode) {
            return true;
        }

        @Override
        public long createTenant(long operatorUserId, String tenantCode, String tenantName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean updateTenant(long operatorUserId, long tenantId, String tenantName, int expectedVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean updateStatus(long operatorUserId, long tenantId, TenantStatus status, int expectedVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void initializeDefaults(long operatorUserId, long tenantId, String tenantCode, String tenantName, String adminUsername, String adminPasswordHash) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<TenantQuota> listQuotas(long tenantId) {
            return List.of(new TenantQuota(tenantId, TenantApplicationService.USER_COUNT_QUOTA, userLimit, countActiveUsers(tenantId)));
        }

        @Override
        public Optional<TenantQuota> findQuota(long tenantId, String quotaKey) {
            return Optional.of(new TenantQuota(tenantId, quotaKey, userLimit, countActiveUsers(tenantId)));
        }

        @Override
        public void upsertQuota(long operatorUserId, long tenantId, String quotaKey, long quotaLimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countActiveUsers(long tenantId) {
            return repository.users.values().stream()
                    .filter(user -> user.tenantId() == tenantId && user.status() == UserStatus.ENABLED)
                    .count();
        }
    }

    private static final class InMemoryUserOrgRepository implements UserOrgRepository {

        private final Map<Long, List<UserOrgAssignment>> assignments = new ConcurrentHashMap<>();

        @Override
        public List<UserOrg> findByUserId(long tenantId, long userId) {
            return assignments.getOrDefault(userId, List.of()).stream()
                    .map(assignment -> new UserOrg(
                            assignment.orgId(),
                            tenantId,
                            userId,
                            assignment.orgId(),
                            "ORG_" + assignment.orgId(),
                            "Org " + assignment.orgId(),
                            "DEPARTMENT",
                            assignment.primary(),
                            assignment.scopeWeight(),
                            assignment.validFrom(),
                            assignment.validTo(),
                            OffsetDateTime.now(ZoneOffset.UTC),
                            OffsetDateTime.now(ZoneOffset.UTC)
                    ))
                    .toList();
        }

        @Override
        public Optional<UserOrg> findByUserAndOrgId(long tenantId, long userId, long orgId) {
            return findByUserId(tenantId, userId).stream()
                    .filter(item -> item.orgId().equals(orgId))
                    .findFirst();
        }

        @Override
        public boolean allOrgsExist(long tenantId, java.util.Collection<Long> orgIds) {
            return true;
        }

        @Override
        public void replaceUserOrgs(long operatorUserId, long tenantId, long userId, List<UserOrgAssignment> assignments) {
            this.assignments.put(userId, List.copyOf(assignments));
        }

        @Override
        public void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId) {
            List<UserOrgAssignment> current = assignments.getOrDefault(userId, List.of());
            assignments.put(userId, current.stream()
                    .map(assignment -> new UserOrgAssignment(
                            assignment.orgId(),
                            primaryOrgId != null && assignment.orgId().equals(primaryOrgId),
                            assignment.scopeWeight(),
                            assignment.validFrom(),
                            assignment.validTo()
                    ))
                    .toList());
        }
    }
}
