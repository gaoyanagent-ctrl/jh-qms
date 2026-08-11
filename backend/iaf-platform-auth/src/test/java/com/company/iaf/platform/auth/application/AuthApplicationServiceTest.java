package com.company.iaf.platform.auth.application;

import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.LoginUser;
import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;
import com.company.iaf.platform.auth.domain.repository.AuthUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.platform.auth.infrastructure.security.InMemoryAuthTokenStore;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthApplicationServiceTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final AuthTokenStore tokenStore = new InMemoryAuthTokenStore();
    private final UserOrgRepository userOrgRepository = new EmptyUserOrgRepository();
    private final TenantRepository tenantRepository = tenantRepository(
            new TenantInfo(1L, "default", "ENABLED"),
            new TenantInfo(2L, "acme", "ENABLED")
    );

    @Test
    void loginReturnsTokenForEnabledUser() {
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository,
                repository(enabledUser("{noop}admin123")),
                userOrgRepository,
                passwordEncoder,
                tokenStore
        );

        AuthToken token = service.login("default", "admin", "admin123");

        assertThat(token.token()).isNotBlank();
        assertThat(token.user().username()).isEqualTo("admin");
        assertThat(token.user().permissions()).contains("platform:auth:me");
        assertThat(service.authenticate(token.token()).userId()).isEqualTo(1L);
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository,
                repository(enabledUser("{noop}admin123")),
                userOrgRepository,
                passwordEncoder,
                tokenStore
        );

        assertThatThrownBy(() -> service.login("default", "admin", "wrong"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }

    @Test
    void loginRejectsDisabledUserWithoutExposingReason() {
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository,
                repository(new LoginUser(
                        1L,
                        1L,
                        "admin",
                        "Platform Administrator",
                        "{noop}admin123",
                        "DISABLED",
                        Set.of()
                )),
                userOrgRepository,
                passwordEncoder,
                tokenStore
        );

        assertThatThrownBy(() -> service.login("default", "admin", "admin123"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }

    @Test
    void loginSeparatesSameUsernameByTenant() {
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository,
                repository(
                        enabledUser("{noop}admin123"),
                        new LoginUser(2L, 2L, "admin", "Acme Admin", "{noop}secret123", "ENABLED",
                                Set.of("platform:tenant2:only"))
                ),
                userOrgRepository,
                passwordEncoder,
                tokenStore
        );

        AuthToken defaultToken = service.login("default", "admin", "admin123");
        AuthToken acmeToken = service.login("acme", "admin", "secret123");

        assertThat(defaultToken.user().tenantId()).isEqualTo(1L);
        assertThat(defaultToken.user().permissions()).containsExactly("platform:auth:me");
        assertThat(acmeToken.user().tenantId()).isEqualTo(2L);
        assertThat(acmeToken.user().permissions()).containsExactly("platform:tenant2:only");
    }

    @Test
    void loginRejectsUnknownOrDisabledTenantWithoutExposingReason() {
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository(new TenantInfo(3L, "disabled", "DISABLED")),
                repository(enabledUser("{noop}admin123")),
                userOrgRepository,
                passwordEncoder,
                tokenStore
        );

        assertThatThrownBy(() -> service.login("missing", "admin", "admin123"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
        assertThatThrownBy(() -> service.login("disabled", "admin", "admin123"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }

    @Test
    void authenticateRefreshesCurrentOrgFromPrimaryAssignment() {
        MutableUserOrgRepository mutableUserOrgRepository = new MutableUserOrgRepository(List.of(
                new UserOrgAssignment(1L, true, 100, null, null),
                new UserOrgAssignment(2L, false, 0, null, null)
        ));
        AuthApplicationService service = new AuthApplicationService(
                tenantRepository,
                repository(enabledUser("{noop}admin123")),
                mutableUserOrgRepository,
                passwordEncoder,
                tokenStore
        );
        AuthToken token = service.login("default", "admin", "admin123");
        assertThat(token.user().currentOrgId()).isEqualTo(1L);

        mutableUserOrgRepository.updateUserPrimaryOrg(99L, 1L, 1L, 2L);

        assertThat(service.authenticate(token.token()).currentOrgId()).isEqualTo(2L);
    }

    @Test
    void tokenStoreRejectsMissingAndExpiredTokens() {
        InMemoryAuthTokenStore store = new InMemoryAuthTokenStore();
        AuthToken token = store.issue(enabledUser("{noop}admin123").toAuthenticatedUser(List.of()), Duration.ofMillis(1));

        assertThat(store.find("missing-token")).isEmpty();
        awaitExpiry();
        assertThat(store.find(token.token())).isEmpty();
    }

    private static AuthUserRepository repository(LoginUser... users) {
        return (tenantId, username) -> List.of(users).stream()
                .filter(user -> user.tenantId() == tenantId && user.username().equals(username))
                .findFirst();
    }

    private static TenantRepository tenantRepository(TenantInfo... tenants) {
        Map<String, TenantInfo> byCode = List.of(tenants).stream()
                .collect(java.util.stream.Collectors.toMap(TenantInfo::tenantCode, tenant -> tenant));
        return new StubTenantRepository(byCode);
    }

    private static void awaitExpiry() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static LoginUser enabledUser(String passwordHash) {
        return new LoginUser(
                1L,
                1L,
                "admin",
                "Platform Administrator",
                passwordHash,
                "ENABLED",
                Set.of("platform:auth:me")
        );
    }

    private static final class StubTenantRepository implements TenantRepository {

        private final Map<String, TenantInfo> byCode;

        private StubTenantRepository(Map<String, TenantInfo> byCode) {
            this.byCode = byCode;
        }

        @Override
        public Optional<TenantInfo> findByTenantCode(String tenantCode) {
            return Optional.ofNullable(byCode.get(tenantCode));
        }

        @Override
        public Optional<Tenant> findById(long tenantId) {
            return byCode.values().stream()
                    .filter(tenant -> tenant.tenantId() == tenantId)
                    .findFirst()
                    .map(tenant -> new Tenant(null, tenant.tenantId(), tenant.tenantCode(), tenant.tenantCode(),
                            TenantStatus.valueOf(tenant.status()), "COMPLETED", null, 0, null, null));
        }

        @Override
        public List<Tenant> findPage(String keyword, int pageNo, int pageSize) {
            return List.of();
        }

        @Override
        public long count(String keyword) {
            return byCode.size();
        }

        @Override
        public boolean existsByTenantCode(String tenantCode) {
            return byCode.containsKey(tenantCode);
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
            return List.of();
        }

        @Override
        public Optional<TenantQuota> findQuota(long tenantId, String quotaKey) {
            return Optional.empty();
        }

        @Override
        public void upsertQuota(long operatorUserId, long tenantId, String quotaKey, long quotaLimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countActiveUsers(long tenantId) {
            return 0;
        }
    }

    private static final class EmptyUserOrgRepository implements UserOrgRepository {

        @Override
        public List<UserOrg> findByUserId(long tenantId, long userId) {
            return List.of();
        }

        @Override
        public Optional<UserOrg> findByUserAndOrgId(long tenantId, long userId, long orgId) {
            return Optional.empty();
        }

        @Override
        public boolean allOrgsExist(long tenantId, java.util.Collection<Long> orgIds) {
            return true;
        }

        @Override
        public void replaceUserOrgs(long operatorUserId, long tenantId, long userId, List<UserOrgAssignment> assignments) {
        }

        @Override
        public void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId) {
        }
    }

    private static final class MutableUserOrgRepository implements UserOrgRepository {

        private List<UserOrgAssignment> assignments;

        private MutableUserOrgRepository(List<UserOrgAssignment> assignments) {
            this.assignments = List.copyOf(assignments);
        }

        @Override
        public List<UserOrg> findByUserId(long tenantId, long userId) {
            OffsetDateTime now = OffsetDateTime.now();
            return assignments.stream()
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
                            now,
                            now
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
            this.assignments = List.copyOf(assignments);
        }

        @Override
        public void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId) {
            assignments = assignments.stream()
                    .map(assignment -> new UserOrgAssignment(
                            assignment.orgId(),
                            primaryOrgId != null && assignment.orgId().equals(primaryOrgId),
                            assignment.scopeWeight(),
                            assignment.validFrom(),
                            assignment.validTo()
                    ))
                    .toList();
        }
    }
}
