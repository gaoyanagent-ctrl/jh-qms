package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingParseJobApplicationService;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingParseJobResponse;
import com.company.iaf.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name="QMS Drawing Parse Jobs", description="Drawing source parse orchestration")
@RestController
@RequestMapping("/api/qms")
public class QmsDrawingParseJobController {
    private final DrawingParseJobApplicationService service;
    public QmsDrawingParseJobController(DrawingParseJobApplicationService service) { this.service=service; }

    @Operation(summary="Get latest parse job")
    @GetMapping("/drawing-revisions/{revisionId}/parse-job")
    public Result<DrawingParseJobResponse> latest(@PathVariable("revisionId") long revisionId) {
        return Result.ok(service.latest(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), revisionId));
    }
    @Operation(summary="Retry latest failed parse job")
    @PostMapping("/drawing-revisions/{revisionId}/parse-job/retry")
    public Result<DrawingParseJobResponse> retry(@PathVariable("revisionId") long revisionId) {
        return Result.ok(service.retry(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), revisionId));
    }


    @Operation(summary="List the latest parse job for each revision in a drawing")
    @GetMapping("/drawings/{drawingId}/parse-jobs")
    public Result<List<DrawingParseJobResponse>> latestByDrawing(@PathVariable("drawingId") long drawingId) {
        return Result.ok(service.latestByDrawing(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), drawingId));
    }
}
