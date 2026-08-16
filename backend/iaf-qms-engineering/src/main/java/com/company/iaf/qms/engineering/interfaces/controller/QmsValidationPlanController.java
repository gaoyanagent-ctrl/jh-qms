package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.infrastructure.persistence.ValidationPlanService;
import com.company.iaf.qms.engineering.interfaces.dto.ValidationPlanActionRequest;
import com.company.iaf.qms.engineering.interfaces.dto.ValidationPlanResponse;
import com.company.iaf.qms.engineering.interfaces.dto.ValidationPlanUpdateRequest;
import com.company.iaf.shared.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qms/inspection-standards/{inspectionStandardId}/validation-plan")
public class QmsValidationPlanController {
    private final ValidationPlanService service;

    public QmsValidationPlanController(ValidationPlanService service) {
        this.service = service;
    }

    @GetMapping
    public Result<ValidationPlanResponse> get(
            @PathVariable("inspectionStandardId") long inspectionStandardId) {
        return Result.ok(service.get(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId));
    }

    @PostMapping("/generate")
    public Result<ValidationPlanResponse> generate(
            @PathVariable("inspectionStandardId") long inspectionStandardId) {
        return Result.ok(service.generate(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId));
    }

    @PutMapping("/{id}")
    public Result<ValidationPlanResponse> update(
            @PathVariable("inspectionStandardId") long inspectionStandardId,
            @PathVariable("id") long id,
            @Valid @RequestBody ValidationPlanUpdateRequest request) {
        return Result.ok(service.update(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId, id, request));
    }

    @PostMapping("/{id}/submit-approval")
    public Result<ValidationPlanResponse> submit(
            @PathVariable("inspectionStandardId") long inspectionStandardId,
            @PathVariable("id") long id,
            @Valid @RequestBody ValidationPlanActionRequest request) {
        return Result.ok(service.submit(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId, id, request.comment()));
    }

    @PostMapping("/{id}/approve")
    public Result<ValidationPlanResponse> approve(
            @PathVariable("inspectionStandardId") long inspectionStandardId,
            @PathVariable("id") long id,
            @Valid @RequestBody ValidationPlanActionRequest request) {
        return Result.ok(service.approve(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId, id, request.comment()));
    }

    @PostMapping("/{id}/reject")
    public Result<ValidationPlanResponse> reject(
            @PathVariable("inspectionStandardId") long inspectionStandardId,
            @PathVariable("id") long id,
            @Valid @RequestBody ValidationPlanActionRequest request) {
        return Result.ok(service.reject(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId, id, request.comment()));
    }

    @PostMapping("/{id}/release")
    public Result<ValidationPlanResponse> release(
            @PathVariable("inspectionStandardId") long inspectionStandardId,
            @PathVariable("id") long id,
            @Valid @RequestBody ValidationPlanActionRequest request) {
        return Result.ok(service.release(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), inspectionStandardId, id, request.comment()));
    }
}
