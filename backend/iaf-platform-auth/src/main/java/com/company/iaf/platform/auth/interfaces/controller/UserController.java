package com.company.iaf.platform.auth.interfaces.controller;

import com.company.iaf.platform.auth.application.UserApplicationService;
import com.company.iaf.platform.auth.interfaces.dto.ResetPasswordRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrgAssignRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrgContextSwitchRequest;
import com.company.iaf.platform.auth.interfaces.dto.UserOrganizationsResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserResponse;
import com.company.iaf.platform.auth.interfaces.dto.UserUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping
    public Result<PageResult<UserResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize
    ) {
        long tenantId = currentTenantId();
        return Result.ok(userApplicationService.listUsers(tenantId, keyword, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<UserResponse> get(@PathVariable("id") long id) {
        return Result.ok(userApplicationService.getUser(currentTenantId(), id));
    }

    @GetMapping("/me")
    public Result<UserResponse> me() {
        long userId = com.company.iaf.shared.security.SecurityContext.getUserId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "User context is missing"));
        return Result.ok(userApplicationService.getCurrentUser(currentTenantId(), userId));
    }

    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userApplicationService.createUser(currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable("id") long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.ok(userApplicationService.updateUser(currentTenantId(), id, request));
    }

    @GetMapping("/{id}/orgs")
    public Result<UserOrganizationsResponse> getOrganizations(@PathVariable("id") long id) {
        return Result.ok(userApplicationService.getUserOrganizations(currentTenantId(), id));
    }

    @PutMapping("/{id}/orgs")
    public Result<UserOrganizationsResponse> replaceOrganizations(
            @PathVariable("id") long id,
            @Valid @RequestBody UserOrgAssignRequest request
    ) {
        return Result.ok(userApplicationService.replaceUserOrganizations(currentTenantId(), id, request));
    }

    @PatchMapping("/{id}/org-context")
    public Result<UserResponse> switchOrgContext(
            @PathVariable("id") long id,
            @Valid @RequestBody UserOrgContextSwitchRequest request
    ) {
        return Result.ok(userApplicationService.switchCurrentOrgContext(currentTenantId(), id, request.orgId()));
    }

    @PostMapping("/{id}/disable")
    public Result<UserResponse> disable(@PathVariable("id") long id) {
        return Result.ok(userApplicationService.disableUser(currentTenantId(), id));
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable("id") long id, @Valid @RequestBody ResetPasswordRequest request) {
        userApplicationService.resetPassword(currentTenantId(), id, request.newPassword());
        return Result.ok();
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}
