package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.MenuApplicationService;
import com.company.iaf.platform.permission.interfaces.dto.MenuResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/auth/menus")
public class AuthMenuController {

    private final MenuApplicationService menuApplicationService;

    public AuthMenuController(MenuApplicationService menuApplicationService) {
        this.menuApplicationService = menuApplicationService;
    }

    @GetMapping
    public Result<List<MenuResponse>> currentUserMenus() {
        return Result.ok(menuApplicationService.listCurrentUserMenus(currentTenantId()));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}
