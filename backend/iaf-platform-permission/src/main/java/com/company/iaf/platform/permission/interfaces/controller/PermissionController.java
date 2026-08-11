package com.company.iaf.platform.permission.interfaces.controller;

import com.company.iaf.platform.permission.application.PermissionApplicationService;
import com.company.iaf.platform.permission.interfaces.dto.PermissionResponse;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/permissions")
public class PermissionController {

    private final PermissionApplicationService permissionApplicationService;

    public PermissionController(PermissionApplicationService permissionApplicationService) {
        this.permissionApplicationService = permissionApplicationService;
    }

    @GetMapping
    public Result<List<PermissionResponse>> list() {
        return Result.ok(permissionApplicationService.listPermissions(currentTenantId()));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}
