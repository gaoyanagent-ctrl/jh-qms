package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.PartApplicationService;
import com.company.iaf.qms.engineering.interfaces.dto.PartCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.PartResponse;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "QMS Parts", description = "QMS engineering part master data")
@Validated
@RestController
@RequestMapping("/api/qms/parts")
public class QmsPartController {

    private final PartApplicationService partApplicationService;

    public QmsPartController(PartApplicationService partApplicationService) {
        this.partApplicationService = partApplicationService;
    }

    @Operation(summary = "List parts in the current organization")
    @GetMapping
    public Result<PageResult<PartResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int pageNo,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int pageSize
    ) {
        return Result.ok(partApplicationService.list(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), keyword, pageNo, pageSize
        ));
    }

    @Operation(summary = "Get a part")
    @GetMapping("/{id}")
    public Result<PartResponse> get(@PathVariable long id) {
        return Result.ok(partApplicationService.get(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id));
    }

    @Operation(summary = "Create a part in the current organization")
    @PostMapping
    public Result<PartResponse> create(@Valid @RequestBody PartCreateRequest request) {
        return Result.ok(partApplicationService.create(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), request
        ));
    }
}
