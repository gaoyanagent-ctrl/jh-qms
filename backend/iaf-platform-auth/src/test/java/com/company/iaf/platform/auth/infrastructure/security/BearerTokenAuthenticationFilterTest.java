package com.company.iaf.platform.auth.infrastructure.security;

import com.company.iaf.platform.auth.application.AuthApplicationService;
import com.company.iaf.platform.auth.domain.model.AuthToken;
import com.company.iaf.platform.auth.domain.model.AuthenticatedUser;
import com.company.iaf.platform.auth.domain.model.Tenant;
import com.company.iaf.platform.auth.domain.model.TenantInfo;
import com.company.iaf.platform.auth.domain.model.TenantQuota;
import com.company.iaf.platform.auth.domain.model.TenantStatus;
import com.company.iaf.platform.auth.domain.repository.AuthUserRepository;
import com.company.iaf.platform.auth.domain.repository.TenantRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BearerTokenAuthenticationFilterTest {

    private final InMemoryAuthTokenStore tokenStore = new InMemoryAuthTokenStore();
    private final StubTenantRepository tenantRepository = new StubTenantRepository();
    private final BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter(authService());

    @AfterEach
    void clear() {
        TenantContext.clear();
        SecurityContext.clear();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void noTokenClearsResidualThreadContextBeforeChain() throws ServletException, IOException {
        TenantContext.setTenantId(99L);
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("stale"));
        FilterChain chain = (request, response) -> {
            assertThat(TenantContext.getTenantId()).isEmpty();
            assertThat(SecurityContext.getUserId()).isEmpty();
            assertThat(SecurityContext.getPermissions()).isEmpty();
            assertThat(MDC.get("traceId")).isNotBlank();
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(TenantContext.getTenantId()).isEmpty();
        assertThat(SecurityContext.getUserId()).isEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void invalidTokenDoesNotLeaveAuthenticationContext() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer missing");

        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) -> {
        });

        assertThat(TenantContext.getTenantId()).isEmpty();
        assertThat(SecurityContext.getUserId()).isEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validTokenSetsContextForChainAndClearsAfterwards() throws ServletException, IOException {
        AuthenticatedUser user = new AuthenticatedUser(7L, 2L, "admin", "Admin", 5L, List.of(), Set.of("platform:auth:me"));
        AuthToken token = tokenStore.issue(user, Duration.ofHours(1));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token.token());
        FilterChain chain = (servletRequest, servletResponse) -> {
            assertThat(TenantContext.getTenantId()).contains(2L);
            assertThat(SecurityContext.getUserId()).contains(7L);
            assertThat(SecurityContext.getCurrentOrgId()).contains(5L);
            assertThat(SecurityContext.getPermissions()).containsExactly("platform:auth:me");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(MDC.get("tenantId")).isEqualTo("2");
            assertThat(MDC.get("userId")).isEqualTo("7");
            assertThat(MDC.get("currentOrgId")).isEqualTo("5");
        };

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(TenantContext.getTenantId()).isEmpty();
        assertThat(SecurityContext.getUserId()).isEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("tenantId")).isNull();
    }

    @Test
    void tokenForDisabledTenantDoesNotSetAuthenticationContext() throws ServletException, IOException {
        tenantRepository.status = TenantStatus.DISABLED;
        AuthenticatedUser user = new AuthenticatedUser(7L, 2L, "admin", "Admin", 5L, List.of(), Set.of("platform:auth:me"));
        AuthToken token = tokenStore.issue(user, Duration.ofHours(1));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token.token());

        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) -> {
            assertThat(TenantContext.getTenantId()).isEmpty();
            assertThat(SecurityContext.getUserId()).isEmpty();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        });

        assertThat(TenantContext.getTenantId()).isEmpty();
        assertThat(SecurityContext.getUserId()).isEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private AuthApplicationService authService() {
        AuthUserRepository authUserRepository = (tenantId, username) -> Optional.empty();
        UserOrgRepository userOrgRepository = new EmptyUserOrgRepository();
        return new AuthApplicationService(
                tenantRepository,
                authUserRepository,
                userOrgRepository,
                PasswordEncoderFactories.createDelegatingPasswordEncoder(),
                tokenStore
        );
    }

    private static final class StubTenantRepository implements TenantRepository {

        private TenantStatus status = TenantStatus.ENABLED;

        @Override
        public Optional<TenantInfo> findByTenantCode(String tenantCode) {
            return Optional.of(new TenantInfo(2L, "acme", status.name()));
        }

        @Override
        public Optional<Tenant> findById(long tenantId) {
            return Optional.of(new Tenant(null, tenantId, "acme", "Acme", status,
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
            return false;
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
        public List<com.company.iaf.platform.auth.domain.model.UserOrg> findByUserId(long tenantId, long userId) {
            OffsetDateTime now = OffsetDateTime.now();
            return List.of(new com.company.iaf.platform.auth.domain.model.UserOrg(
                    1L,
                    tenantId,
                    userId,
                    5L,
                    "ORG",
                    "Org",
                    "DEPARTMENT",
                    true,
                    100,
                    null,
                    null,
                    now,
                    now
            ));
        }

        @Override
        public Optional<com.company.iaf.platform.auth.domain.model.UserOrg> findByUserAndOrgId(long tenantId, long userId, long orgId) {
            return Optional.empty();
        }

        @Override
        public boolean allOrgsExist(long tenantId, java.util.Collection<Long> orgIds) {
            return true;
        }

        @Override
        public void replaceUserOrgs(long operatorUserId, long tenantId, long userId, List<com.company.iaf.platform.auth.domain.model.UserOrgAssignment> assignments) {
        }

        @Override
        public void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId) {
        }
    }
}
