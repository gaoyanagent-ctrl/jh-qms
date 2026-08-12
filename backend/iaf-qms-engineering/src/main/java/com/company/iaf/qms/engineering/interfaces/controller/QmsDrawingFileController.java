package com.company.iaf.qms.engineering.interfaces.controller;

import com.company.iaf.qms.engineering.application.DrawingFileApplicationService;
import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
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

@Tag(name="QMS Drawing Files", description="Controlled drawing revision source files")
@RestController @RequestMapping("/api/qms/drawing-revisions/{revisionId}/file")
public class QmsDrawingFileController {
    private final DrawingFileApplicationService service;
    public QmsDrawingFileController(DrawingFileApplicationService service) { this.service=service; }
    @Operation(summary="Upload PDF or DWG source file")
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<QmsFileResponse> upload(@PathVariable("revisionId") long id, @RequestPart("file") MultipartFile file) {
        return Result.ok(service.upload(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id, file));
    }
    @GetMapping public Result<QmsFileResponse> metadata(@PathVariable("revisionId") long id) {
        return Result.ok(QmsFileResponse.from(service.metadata(QmsRequestContext.tenantId(), QmsRequestContext.orgId(), id)));
    }
    @GetMapping("/content") public ResponseEntity<InputStreamResource> content(@PathVariable("revisionId") long id) {
        long tenant=QmsRequestContext.tenantId(), org=QmsRequestContext.orgId();
        QmsFileObject file=service.metadata(tenant, org, id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.mediaType())).contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.originalName()).build().toString())
                .body(new InputStreamResource(service.content(tenant, org, id)));
    }
}
