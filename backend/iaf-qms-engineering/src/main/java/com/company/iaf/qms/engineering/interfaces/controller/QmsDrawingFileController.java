package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingFileApplicationService;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFileRole;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingRevisionFileResponse;
import com.company.iaf.qms.engineering.interfaces.dto.QmsFileResponse;
import com.company.iaf.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Tag(name="QMS Drawing Files", description="Controlled drawing revision source files")
@RestController @RequestMapping("/api/qms/drawing-revisions/{revisionId}")
public class QmsDrawingFileController {
    private final DrawingFileApplicationService service;
    public QmsDrawingFileController(DrawingFileApplicationService service) { this.service=service; }
    @Operation(summary="Upload PDF or DWG source file")
    @PostMapping(value="/file", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<QmsFileResponse> upload(@PathVariable("revisionId") long id, @RequestPart("file") MultipartFile file) {
        return Result.ok(service.upload(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id, file));
    }
    @GetMapping("/file") public Result<QmsFileResponse> metadata(@PathVariable("revisionId") long id) {
        return Result.ok(QmsFileResponse.from(service.metadata(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id)));
    }
    @GetMapping("/file/content") public ResponseEntity<InputStreamResource> content(@PathVariable("revisionId") long id) {
        long tenant=QmsRequestContext.tenantId(), org=QmsRequestContext.orgId();
        QmsFileObject file=service.metadata(tenant, org, id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.mediaType())).contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.originalName()).build().toString())
                .body(new InputStreamResource(service.content(tenant, org, id)));
    }
    @GetMapping("/files") public Result<List<DrawingRevisionFileResponse>> attachments(@PathVariable("revisionId") long id) {
        return Result.ok(service.attachments(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id).stream()
                .map(DrawingRevisionFileResponse::from).toList());
    }
    @PostMapping(value="/files/{role}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<QmsFileResponse> uploadRole(@PathVariable("revisionId") long id,
            @PathVariable("role") DrawingRevisionFileRole role, @RequestPart("file") MultipartFile file) {
        return Result.ok(service.upload(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id, role, file));
    }
    @GetMapping("/files/{role}/content")
    public ResponseEntity<InputStreamResource> roleContent(@PathVariable("revisionId") long id,
            @PathVariable("role") DrawingRevisionFileRole role) {
        long tenant=QmsRequestContext.tenantId(), org=QmsRequestContext.orgId();
        QmsFileObject file=service.metadata(tenant, org, id, role);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.mediaType())).contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.originalName()).build().toString())
                .body(new InputStreamResource(service.content(tenant, org, id, role)));
    }
}
