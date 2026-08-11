package com.company.iaf.platform.auth.interfaces.controller;

import com.company.iaf.platform.auth.application.TenantApplicationService;
import com.company.iaf.platform.auth.interfaces.dto.TenantCreateRequest;
import com.company.iaf.platform.auth.interfaces.dto.TenantQuotaResponse;
import com.company.iaf.platform.auth.interfaces.dto.TenantQuotaUpdateRequest;
import com.company.iaf.platform.auth.interfaces.dto.TenantResponse;
import com.company.iaf.platform.auth.interfaces.dto.TenantUpdateRequest;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/tenants")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;

    public TenantController(TenantApplicationService tenantApplicationService) {
        this.tenantApplicationService = tenantApplicationService;
    }

    @GetMapping
    public Result<PageResult<TenantResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize
    ) {
        return Result.ok(tenantApplicationService.listTenants(keyword, pageNo, pageSize));
    }

    @PostMapping
    public Result<TenantResponse> create(@Valid @RequestBody TenantCreateRequest request) {
        return Result.ok(tenantApplicationService.createTenant(request));
    }

    @GetMapping("/{id}")
    public Result<TenantResponse> get(@PathVariable("id") long id) {
        return Result.ok(tenantApplicationService.getTenant(id));
    }

    @PutMapping("/{id}")
    public Result<TenantResponse> update(@PathVariable("id") long id, @Valid @RequestBody TenantUpdateRequest request) {
        return Result.ok(tenantApplicationService.updateTenant(id, request));
    }

    @PostMapping("/{id}/enable")
    public Result<TenantResponse> enable(@PathVariable("id") long id) {
        return Result.ok(tenantApplicationService.enableTenant(id));
    }

    @PostMapping("/{id}/disable")
    public Result<TenantResponse> disable(@PathVariable("id") long id) {
        return Result.ok(tenantApplicationService.disableTenant(id));
    }

    @GetMapping("/{id}/quotas")
    public Result<List<TenantQuotaResponse>> listQuotas(@PathVariable("id") long id) {
        return Result.ok(tenantApplicationService.listQuotas(id));
    }

    @PutMapping("/{id}/quotas")
    public Result<TenantQuotaResponse> updateQuota(
            @PathVariable("id") long id,
            @Valid @RequestBody TenantQuotaUpdateRequest request
    ) {
        return Result.ok(tenantApplicationService.updateQuota(id, request.quotaKey(), request.quotaLimit()));
    }
}
