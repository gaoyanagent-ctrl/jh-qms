package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingParseResultQueryService;
import com.company.iaf.qms.engineering.interfaces.dto.*;
import com.company.iaf.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "QMS Drawing Parse Results", description = "Drawing intermediate model and source evidence")
@RestController
@RequestMapping("/api/qms/drawing-revisions/{revisionId}")
public class QmsDrawingParseResultController {
    private final DrawingParseResultQueryService service;
    public QmsDrawingParseResultController(DrawingParseResultQueryService service) { this.service = service; }

    @Operation(summary = "Get the drawing intermediate model")
    @GetMapping("/intermediate-model")
    public Result<DrawingIntermediateModelResponse> model(@PathVariable("revisionId") long revisionId) {
        return Result.ok(service.model(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), revisionId));
    }

    @Operation(summary = "List normalized drawing entities")
    @GetMapping("/entities")
    public Result<List<DrawingEntityResponse>> entities(@PathVariable("revisionId") long revisionId) {
        return Result.ok(service.entities(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), revisionId));
    }

    @Operation(summary = "List source evidence")
    @GetMapping("/evidence")
    public Result<List<SourceEvidenceResponse>> evidence(@PathVariable("revisionId") long revisionId) {
        return Result.ok(service.evidence(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), revisionId));
    }

    @Operation(summary = "Get source evidence")
    @GetMapping("/evidence/{evidenceId}")
    public Result<SourceEvidenceResponse> evidence(@PathVariable("revisionId") long revisionId,
                                                   @PathVariable("evidenceId") long evidenceId) {
        return Result.ok(service.evidence(QmsRequestContext.tenantId(), QmsRequestContext.orgId(),
                revisionId, evidenceId));
    }
}
