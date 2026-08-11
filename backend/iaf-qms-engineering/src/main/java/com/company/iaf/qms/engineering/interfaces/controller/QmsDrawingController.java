package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingApplicationService;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingResponse;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionCreateRequest;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionResponse;
import com.company.iaf.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "QMS Drawings", description = "QMS drawings and revision history")
@RestController
@RequestMapping("/api/qms")
public class QmsDrawingController {

    private final DrawingApplicationService drawingApplicationService;

    public QmsDrawingController(DrawingApplicationService drawingApplicationService) {
        this.drawingApplicationService = drawingApplicationService;
    }

    @Operation(summary = "List drawings under a part")
    @GetMapping("/parts/{partId}/drawings")
    public Result<List<DrawingResponse>> listDrawings(@PathVariable long partId) {
        return Result.ok(drawingApplicationService.listDrawings(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), partId
        ));
    }

    @Operation(summary = "Create a drawing under a part")
    @PostMapping("/parts/{partId}/drawings")
    public Result<DrawingResponse> createDrawing(
            @PathVariable long partId,
            @Valid @RequestBody DrawingCreateRequest request
    ) {
        return Result.ok(drawingApplicationService.createDrawing(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), partId, request
        ));
    }

    @Operation(summary = "Get a drawing")
    @GetMapping("/drawings/{id}")
    public Result<DrawingResponse> getDrawing(@PathVariable long id) {
        return Result.ok(drawingApplicationService.getDrawing(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id
        ));
    }

    @Operation(summary = "List drawing revisions")
    @GetMapping("/drawings/{drawingId}/revisions")
    public Result<List<DrawingRevisionResponse>> listRevisions(@PathVariable long drawingId) {
        return Result.ok(drawingApplicationService.listRevisions(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), drawingId
        ));
    }

    @Operation(summary = "Create a metadata-only drawing revision draft")
    @PostMapping("/drawings/{drawingId}/revisions")
    public Result<DrawingRevisionResponse> createRevision(
            @PathVariable long drawingId,
            @Valid @RequestBody DrawingRevisionCreateRequest request
    ) {
        return Result.ok(drawingApplicationService.createRevision(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), drawingId, request
        ));
    }

    @Operation(summary = "Get a drawing revision")
    @GetMapping("/drawing-revisions/{id}")
    public Result<DrawingRevisionResponse> getRevision(@PathVariable long id) {
        return Result.ok(drawingApplicationService.getRevision(
                QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id
        ));
    }
}
