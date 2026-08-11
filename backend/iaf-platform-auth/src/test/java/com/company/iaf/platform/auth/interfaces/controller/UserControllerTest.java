package com.company.iaf.platform.auth.interfaces.controller;

import com.company.iaf.platform.auth.application.PlatformAuthErrorCode;
import com.company.iaf.platform.auth.application.UserApplicationService;
import com.company.iaf.platform.auth.domain.model.UserStatus;
import com.company.iaf.platform.auth.interfaces.dto.UserCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrganizationsResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserUpdateRequest;
import com.company.iaf.platform.core.security.PermissionChecker;
import com.company.iaf.platform.core.security.RequiresPermissionAspect;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = UserControllerTest.TestConfig.class)
class UserControllerTest {

    private final UserApplicationService userApplicationService = mock(UserApplicationService.class);
    @Autowired
    private PermissionChecker permissionChecker;
    @Autowired
    private RequiresPermissionAspect requiresPermissionAspect;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userApplicationService);
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper))
                .build();
        SecurityContext.setUserId(99L);
        TenantContext.setTenantId(1L);
        SecurityContext.setPermissions(Set.of(
                "platform:user:view", "platform:user:create",
                "platform:user:update", "platform:user:disable",
                "platform:user:reset-password"));
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
        TenantContext.clear();
    }

    @Test
    void listUsersReturnsPageResultEnvelope() throws Exception {
        when(userApplicationService.listUsers(eq(1L), any(), anyLong(), anyLong()))
                .thenReturn(new PageResult<>(List.of(sampleUser(1L)), 1L, 1L, 20L));

        mockMvc.perform(get("/api/platform/users").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
    }

    @Test
    void getUserReturnsUnifiedEnvelope() throws Exception {
        when(userApplicationService.getUser(eq(1L), eq(1L))).thenReturn(sampleUser(1L));

        mockMvc.perform(get("/api/platform/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        when(userApplicationService.createUser(eq(1L), any(UserCreateRequest.class)))
                .thenReturn(sampleUser(2L));

        mockMvc.perform(post("/api/platform/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\","
                                + "\"displayName\":\"Alice\",\"mobile\":\"13800000000\","
                                + "\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        when(userApplicationService.updateUser(eq(1L), eq(1L), any(UserUpdateRequest.class)))
                .thenReturn(sampleUser(1L));

        mockMvc.perform(put("/api/platform/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Updated\",\"mobile\":\"13900000000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUserOrganizationsReturnsAssignments() throws Exception {
        when(userApplicationService.getUserOrganizations(eq(1L), eq(1L)))
                .thenReturn(new UserOrganizationsResponse(1L, 1L, List.of()));

        mockMvc.perform(get("/api/platform/users/1/orgs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void replaceUserOrganizationsReturnsAssignments() throws Exception {
        when(userApplicationService.replaceUserOrganizations(eq(1L), eq(1L), any()))
                .thenReturn(new UserOrganizationsResponse(1L, 1L, List.of()));

        mockMvc.perform(put("/api/platform/users/1/orgs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizations\":[{\"orgId\":1,\"primary\":true,\"scopeWeight\":100}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryOrgId").value(1));
    }

    @Test
    void disableUserReturnsDisabledUser() throws Exception {
        UserResponse disabled = new UserResponse(1L, 1L, "admin", "Platform Administrator",
                null, null, UserStatus.DISABLED, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now());
        when(userApplicationService.disableUser(eq(1L), eq(1L))).thenReturn(disabled);

        mockMvc.perform(post("/api/platform/users/1/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    void resetPasswordReturnsOkEnvelope() throws Exception {
        mockMvc.perform(post("/api/platform/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createUserPropagatesBusinessError() throws Exception {
        when(userApplicationService.createUser(eq(1L), any(UserCreateRequest.class)))
                .thenThrow(new BusinessException(PlatformAuthErrorCode.USERNAME_ALREADY_EXISTS));

        mockMvc.perform(post("/api/platform/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\","
                                + "\"displayName\":\"Alice\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLATFORM_AUTH_USERNAME_ALREADY_EXISTS"));
    }

    private static UserResponse sampleUser(long id) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new UserResponse(id, 1L, "admin", "Platform Administrator",
                null, null, UserStatus.ENABLED, null, List.of(), now, now);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
     * Local copy of the production GlobalExceptionHandler behaviour for
     * {@link BusinessException} so the test does not need to depend on
     * {@code iaf-app}. This intentionally only covers the cases exercised
     * by the user controller test.
     */
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
            return ResponseEntity.status(400).body(Result.fail(
                    exception.errorCode().code(),
                    exception.errorCode().message()));
        }
    }
}
