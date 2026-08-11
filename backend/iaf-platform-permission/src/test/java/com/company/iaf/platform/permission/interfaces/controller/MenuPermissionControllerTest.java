package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.MenuApplicationService;
import com.company.iaf.platform.permission.application.PermissionApplicationService;
import com.company.iaf.platform.permission.interfaces.dto.MenuCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.MenuResponse;
import com.company.iaf.platform.permission.interfaces.dto.MenuUpdateRequest;
import com.company.iaf.platform.permission.interfaces.dto.PermissionResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuPermissionControllerTest {

    private final MenuApplicationService menuApplicationService = mock(MenuApplicationService.class);
    private final PermissionApplicationService permissionApplicationService = mock(PermissionApplicationService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MenuController(menuApplicationService),
                        new AuthMenuController(menuApplicationService),
                        new PermissionController(permissionApplicationService))
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper))
                .build();
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @Test
    void listMenuTreeReturnsEnvelope() throws Exception {
        when(menuApplicationService.listMenuTree(eq(1L)))
                .thenReturn(List.of(sampleMenu()));

        mockMvc.perform(get("/api/platform/menus/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menuCode").value("platform.users"));
    }

    @Test
    void listCurrentUserMenusReturnsEnvelope() throws Exception {
        when(menuApplicationService.listCurrentUserMenus(eq(1L)))
                .thenReturn(List.of(sampleMenu()));

        mockMvc.perform(get("/api/platform/auth/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].routePath").value("/platform/users"));
    }

    @Test
    void createMenuReturnsCreatedMenu() throws Exception {
        when(menuApplicationService.createMenu(eq(1L), any(MenuCreateRequest.class)))
                .thenReturn(sampleMenu());

        mockMvc.perform(post("/api/platform/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuCode\":\"platform.users\",\"menuType\":\"MENU\","
                                + "\"titleKey\":\"menu.users\",\"routePath\":\"/platform/users\","
                                + "\"sortNo\":100,\"visible\":true,\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuCode").value("platform.users"));
    }

    @Test
    void updateMenuReturnsUpdatedMenu() throws Exception {
        when(menuApplicationService.updateMenu(eq(1L), eq(10L), any(MenuUpdateRequest.class)))
                .thenReturn(sampleMenu());

        mockMvc.perform(put("/api/platform/menus/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuCode\":\"platform.users\",\"menuType\":\"MENU\","
                                + "\"titleKey\":\"menu.users\",\"routePath\":\"/platform/users\","
                                + "\"sortNo\":100,\"visible\":true,\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void listPermissionsReturnsEnvelope() throws Exception {
        when(permissionApplicationService.listPermissions(eq(1L)))
                .thenReturn(List.of(new PermissionResponse(
                        1L, 1L, "platform:menu:view", "Menu view", "API", "platform", "view")));

        mockMvc.perform(get("/api/platform/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permissionCode").value("platform:menu:view"));
    }

    private static MenuResponse sampleMenu() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new MenuResponse(
                10L,
                1L,
                null,
                "platform.users",
                "MENU",
                "menu.users",
                "/platform/users",
                "platform.users",
                "UserOutlined",
                100,
                true,
                true,
                0,
                List.of("platform:user:view"),
                List.of(),
                now,
                now
        );
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
