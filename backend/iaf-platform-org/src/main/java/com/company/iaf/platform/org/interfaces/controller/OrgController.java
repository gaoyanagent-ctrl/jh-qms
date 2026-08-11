package com.company.iaf.platform.org.interfaces.controller;

import com.company.iaf.platform.org.application.OrgApplicationService;
import com.company.iaf.platform.org.interfaces.dto.OrgCreateRequest;
import com.company.iaf.platform.org.interfaces.dto.OrgResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgTreeNodeResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgUpdateRequest;
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
@RequestMapping("/api/platform/orgs")
public class OrgController {

    private final OrgApplicationService orgApplicationService;

    public OrgController(OrgApplicationService orgApplicationService) {
        this.orgApplicationService = orgApplicationService;
    }

    @GetMapping("/tree")
    public Result<List<OrgTreeNodeResponse>> tree() {
        return Result.ok(orgApplicationService.getTree(currentTenantId()));
    }

    @GetMapping("/{id}")
    public Result<OrgResponse> get(@PathVariable("id") long id) {
        return Result.ok(orgApplicationService.getOrg(currentTenantId(), id));
    }

    @PostMapping
    public Result<OrgResponse> create(@Valid @RequestBody OrgCreateRequest request) {
        return Result.ok(orgApplicationService.createOrg(currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<OrgResponse> update(@PathVariable("id") long id, @Valid @RequestBody OrgUpdateRequest request) {
        return Result.ok(orgApplicationService.updateOrg(currentTenantId(), id, request));
    }

    private static long currentTenantId() {
        return TenantContext.getTenantId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "Tenant context is missing"));
    }
}