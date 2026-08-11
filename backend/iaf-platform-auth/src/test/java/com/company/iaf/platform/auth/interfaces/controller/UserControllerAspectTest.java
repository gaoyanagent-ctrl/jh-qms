package com.company.iaf.platform.auth.interfaces.controller;

import com.company.iaf.platform.auth.application.UserApplicationService;
import com.company.iaf.platform.auth.domain.model.PlatformUser;
import com.company.iaf.platform.auth.domain.model.UserDataScope;
import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;
import com.company.iaf.platform.auth.domain.model.UserStatus;
import com.company.iaf.platform.auth.domain.repository.PlatformUserRepository;
import com.company.iaf.platform.auth.domain.repository.UserOrgRepository;
import com.company.iaf.platform.core.security.PermissionChecker;
import com.company.iaf.platform.core.security.RequiresPermissionAspect;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the {@code @RequiresPermission} aspect actually gates
 * controller traffic end-to-end. Unlike {@link UserControllerTest}
 * (which mocks the application service and therefore bypasses the
 * aspect), this test wires the real {@link UserApplicationService}
 * against an in-memory repository so the aspect can intercept every
 * call before the use case runs.
 *
 * <p>The tests cover the three relevant outcomes:
 * <ul>
 *   <li>200 / success envelope when both authentication and the
 *       required permission are present;</li>
 *   <li>403 / {@code COMMON_FORBIDDEN} when the user is authenticated
 *       but the required permission is missing;</li>
 *   <li>401 / {@code COMMON_UNAUTHORIZED} when no user is bound to the
 *       current thread.</li>
 * </ul>
 */
@SpringJUnitConfig(classes = UserControllerAspectTest.TestConfig.class)
class UserControllerAspectTest {

    @Autowired
    private UserApplicationService userApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userApplicationService);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
        TenantContext.clear();
    }

    @Test
    void listUsersReturns200WhenPermissionGranted() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        mockMvc.perform(get("/api/platform/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listUsersReturns403WhenPermissionMissing() throws Exception {
        // Authenticated but the required permission is absent.
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:role:view"));

        mockMvc.perform(get("/api/platform/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    void listUsersReturns401WhenUserUnauthenticated() throws Exception {
        // No user id means the aspect rejects before the permission check.
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        mockMvc.perform(get("/api/platform/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.UNAUTHORIZED.code()));
    }

    @Test
    void createUserReturns403WhenPermissionMissing() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));

        mockMvc.perform(post("/api/platform/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\",\"password\":\"password123\","
                                + "\"displayName\":\"Bob\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    void createUserReturns200WhenCreatePermissionGranted() throws Exception {
        SecurityContext.setUserId(99L);
        SecurityContext.setPermissions(Set.of("platform:user:create"));

        mockMvc.perform(post("/api/platform/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carol\",\"password\":\"password123\","
                                + "\"displayName\":\"Carol\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Local re-implementation of the production
     * {@code GlobalExceptionHandler} behaviour for {@link BusinessException}
     * (specifically the 401/403/400 mapping) so the test does not need
     * to depend on {@code iaf-app}.
     */
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
            String code = exception.errorCode().code();
            int status = switch (code) {
                case "COMMON_UNAUTHORIZED" -> 401;
                case "COMMON_FORBIDDEN" -> 403;
                default -> 400;
            };
            return ResponseEntity.status(status).body(Result.fail(code, exception.getMessage()));
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        PlatformUserRepository platformUserRepository() {
            return new InMemoryPlatformUserRepository();
        }

        @Bean
        UserApplicationService userApplicationService(
                PlatformUserRepository repository,
                UserOrgRepository userOrgRepository,
                PasswordEncoder passwordEncoder
        ) {
            return new UserApplicationService(repository, userOrgRepository, passwordEncoder);
        }

        @Bean
        UserOrgRepository userOrgRepository() {
            return new InMemoryUserOrgRepository();
        }

        @Bean
        PermissionChecker permissionChecker() {
            return new PermissionChecker();
        }

        @Bean
        RequiresPermissionAspect requiresPermissionAspect(PermissionChecker permissionChecker) {
            return new RequiresPermissionAspect(permissionChecker);
        }
    }

    /**
     * Test double for {@link PlatformUserRepository}, kept private so it
     * does not leak into other test classes. Tracks users plus password
     * hashes in side maps so the service can verify encoding actually ran.
     */
    private static final class InMemoryPlatformUserRepository implements PlatformUserRepository {

        private final AtomicLong nextId = new AtomicLong(1);
        private final Map<Long, PlatformUser> users = new ConcurrentHashMap<>();
        private final Map<Long, String> hashes = new ConcurrentHashMap<>();

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
            long total = 0;
            for (PlatformUser user : users.values()) {
                if (user.tenantId() != tenantId) continue;
                if (needle == null
                        || user.username().toLowerCase().contains(needle)
                        || user.displayName().toLowerCase().contains(needle)
                        || (user.mobile() != null && user.mobile().toLowerCase().contains(needle))
                        || (user.email() != null && user.email().toLowerCase().contains(needle))) {
                    total++;
                }
            }
            return total;
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
            hashes.put(id, passwordHash);
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            users.put(id, new PlatformUser(current.id(), current.tenantId(), current.username(), current.displayName(),
                    current.mobile(), current.email(), current.status(), current.primaryOrgId(),
                    current.version() + 1, current.createdAt(), now));
            return true;
        }
    }

    private static final class InMemoryUserOrgRepository implements UserOrgRepository {

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
}
