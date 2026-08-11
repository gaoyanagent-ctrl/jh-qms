package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.MenuApplicationService;
import com.company.iaf.platform.permission.interfaces.dto.MenuCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.MenuResponse;
import com.company.iaf.platform.permission.interfaces.dto.MenuUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/menus")
public class MenuController {

    private final MenuApplicationService menuApplicationService;

    public MenuController(MenuApplicationService menuApplicationService) {
        this.menuApplicationService = menuApplicationService;
    }

    @GetMapping("/tree")
    public Result<List<MenuResponse>> tree() {
        return Result.ok(menuApplicationService.listMenuTree(currentTenantId()));
    }

    @PostMapping
    public Result<MenuResponse> create(@Valid @RequestBody MenuCreateRequest request) {
        return Result.ok(menuApplicationService.createMenu(currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<MenuResponse> update(@PathVariable("id") long id, @Valid @RequestBody MenuUpdateRequest request) {
        return Result.ok(menuApplicationService.updateMenu(currentTenantId(), id, request));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}
