package com.company.iaf.platform.integration.interfaces.controller;

import com.company.iaf.platform.integration.application.OutboxApplicationService;
import com.company.iaf.platform.integration.interfaces.dto.OutboxEventResponse;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(OutboxApplicationService.class)
@RequestMapping("/api/platform/outbox-events")
public class OutboxEventController {

    private final OutboxApplicationService service;

    public OutboxEventController(OutboxApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public Result<PageResult<OutboxEventResponse>> list(
            @RequestParam("tenantId") long tenantId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize
    ) {
        var page = service.list(tenantId, status, pageNo, pageSize);
        return Result.ok(new PageResult<>(
                page.records().stream().map(OutboxEventResponse::from).toList(),
                page.total(),
                page.pageNo(),
                page.pageSize()
        ));
    }

    @PostMapping("/{id}/retry")
    public Result<Void> retry(
            @PathVariable("id") long id,
            @RequestParam("tenantId") long tenantId
    ) {
        service.retry(tenantId, id);
        return Result.ok();
    }
}
