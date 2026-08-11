package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.RoleApplicationService;
import com.company.iaf.platform.permission.interfaces.dto.AssignRoleMenusRequest;
import com.company.iaf.platform.permission.interfaces.dto.AssignRolePermissionsRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleResponse;
import com.company.iaf.platform.permission.interfaces.dto.RoleUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/roles")
public class RoleController {

    private final RoleApplicationService roleApplicationService;

    public RoleController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @GetMapping
    public Result<PageResult<RoleResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize
    ) {
        return Result.ok(roleApplicationService.listRoles(currentTenantId(), keyword, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<RoleResponse> get(@PathVariable("id") long id) {
        return Result.ok(roleApplicationService.getRole(currentTenantId(), id));
    }

    @PostMapping
    public Result<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.ok(roleApplicationService.createRole(currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<RoleResponse> update(@PathVariable("id") long id, @Valid @RequestBody RoleUpdateRequest request) {
        return Result.ok(roleApplicationService.updateRole(currentTenantId(), id, request));
    }

    @PutMapping("/{id}/permissions")
    public Result<RoleResponse> assignPermissions(
            @PathVariable("id") long id,
            @Valid @RequestBody AssignRolePermissionsRequest request
    ) {
        return Result.ok(roleApplicationService.assignPermissions(currentTenantId(), id, request));
    }

    @PutMapping("/{id}/menus")
    public Result<RoleResponse> assignMenus(
            @PathVariable("id") long id,
            @Valid @RequestBody AssignRoleMenusRequest request
    ) {
        return Result.ok(roleApplicationService.assignMenus(currentTenantId(), id, request));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}
