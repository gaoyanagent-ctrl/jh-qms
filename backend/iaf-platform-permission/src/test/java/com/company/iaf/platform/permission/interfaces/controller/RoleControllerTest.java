package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.PlatformPermissionErrorCode;
import com.company.iaf.platform.permission.application.RoleApplicationService;
import com.company.iaf.platform.permission.domain.model.RoleStatus;
import com.company.iaf.platform.permission.interfaces.dto.AssignRoleMenusRequest;
import com.company.iaf.platform.permission.interfaces.dto.AssignRolePermissionsRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleResponse;
import com.company.iaf.platform.permission.interfaces.dto.RoleUpdateRequest;
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

@SpringJUnitConfig(classes = RoleControllerTest.TestConfig.class)
class RoleControllerTest {

    private final RoleApplicationService roleApplicationService = mock(RoleApplicationService.class);
    @Autowired
    private com.company.iaf.platform.core.security.PermissionChecker permissionChecker;
    @Autowired
    private com.company.iaf.platform.core.security.RequiresPermissionAspect requiresPermissionAspect;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RoleController controller = new RoleController(roleApplicationService);
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
                "platform:role:view", "platform:role:create",
                "platform:role:update", "platform:role:assign-permission",
                "platform:role:assign-menu"));
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
        TenantContext.clear();
    }

    @Test
    void listRolesReturnsPageResultEnvelope() throws Exception {
        when(roleApplicationService.listRoles(eq(1L), any(), anyLong(), anyLong()))
                .thenReturn(new PageResult<>(List.of(sampleRole(1L)), 1L, 1L, 20L));

        mockMvc.perform(get("/api/platform/roles").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("ops"));
    }

    @Test
    void getRoleReturnsEnvelope() throws Exception {
        when(roleApplicationService.getRole(eq(1L), eq(1L))).thenReturn(sampleRole(1L));

        mockMvc.perform(get("/api/platform/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ops"));
    }

    @Test
    void createRoleReturnsCreatedRole() throws Exception {
        when(roleApplicationService.createRole(eq(1L), any(RoleCreateRequest.class)))
                .thenReturn(sampleRole(2L));

        mockMvc.perform(post("/api/platform/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ops\",\"roleName\":\"Operations\","
                                + "\"roleType\":\"BUSINESS\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ops"));
    }

    @Test
    void updateRoleReturnsUpdatedRole() throws Exception {
        when(roleApplicationService.updateRole(eq(1L), eq(1L), any(RoleUpdateRequest.class)))
                .thenReturn(sampleRole(1L));

        mockMvc.perform(put("/api/platform/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ops\",\"roleName\":\"Operations\","
                                + "\"roleType\":\"BUSINESS\",\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ops"));
    }

    @Test
    void assignPermissionsReturnsUpdatedRole() throws Exception {
        when(roleApplicationService.assignPermissions(eq(1L), eq(1L), any(AssignRolePermissionsRequest.class)))
                .thenReturn(sampleRole(1L));

        mockMvc.perform(put("/api/platform/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionCodes\":[\"platform:user:view\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ops"));
    }

    @Test
    void assignMenusReturnsUpdatedRole() throws Exception {
        when(roleApplicationService.assignMenus(eq(1L), eq(1L), any(AssignRoleMenusRequest.class)))
                .thenReturn(sampleRole(1L));

        mockMvc.perform(put("/api/platform/roles/1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuCodes\":[\"platform.users\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ops"));
    }

    @Test
    void createRolePropagatesBusinessError() throws Exception {
        when(roleApplicationService.createRole(eq(1L), any(RoleCreateRequest.class)))
                .thenThrow(new BusinessException(PlatformPermissionErrorCode.ROLE_CODE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/platform/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"ops\",\"roleName\":\"Operations\","
                                + "\"roleType\":\"BUSINESS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLATFORM_PERMISSION_ROLE_CODE_ALREADY_EXISTS"));
    }

    private static RoleResponse sampleRole(long id) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new RoleResponse(id, 1L, "ops", "Operations", "BUSINESS",
                RoleStatus.ENABLED, 0, List.of("platform:user:view"), List.of("platform.users"), now, now);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        com.company.iaf.platform.core.security.PermissionChecker permissionChecker() {
            return new com.company.iaf.platform.core.security.PermissionChecker();
        }

        @Bean
        com.company.iaf.platform.core.security.RequiresPermissionAspect requiresPermissionAspect(
                com.company.iaf.platform.core.security.PermissionChecker permissionChecker
        ) {
            return new com.company.iaf.platform.core.security.RequiresPermissionAspect(permissionChecker);
        }
    }

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
