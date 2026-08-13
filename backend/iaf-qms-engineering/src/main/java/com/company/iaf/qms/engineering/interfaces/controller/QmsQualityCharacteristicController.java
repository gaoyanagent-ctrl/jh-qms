package com.company.iaf.qms.engineering.interfaces.controller;
import com.company.iaf.qms.engineering.application.QualityCharacteristicService;
import com.company.iaf.qms.engineering.interfaces.dto.*;
import com.company.iaf.shared.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/qms/drawing-revisions/{revisionId}/characteristics")
public class QmsQualityCharacteristicController {
 private final QualityCharacteristicService service; public QmsQualityCharacteristicController(QualityCharacteristicService service){this.service=service;}
 @GetMapping public Result<List<QualityCharacteristicResponse>> list(@PathVariable("revisionId") long revisionId){return Result.ok(service.list(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),revisionId));}
 @PostMapping public Result<QualityCharacteristicResponse> create(@PathVariable("revisionId") long revisionId,@Valid @RequestBody QualityCharacteristicCreateRequest request){return Result.ok(service.create(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),revisionId,request));}
 @PostMapping("/bulk-review") public Result<List<QualityCharacteristicResponse>> bulkReview(@PathVariable("revisionId") long revisionId,@Valid @RequestBody QualityCharacteristicBulkReviewRequest request){return Result.ok(service.bulkReview(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),revisionId,request));}
 @PostMapping("/{id}/confirm") public Result<QualityCharacteristicResponse> confirm(@PathVariable("revisionId") long revisionId,@PathVariable("id") long id,@Valid @RequestBody QualityCharacteristicReviewRequest request){return Result.ok(service.review(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),revisionId,id,"CONFIRMED",request));}
 @PostMapping("/{id}/reject") public Result<QualityCharacteristicResponse> reject(@PathVariable("revisionId") long revisionId,@PathVariable("id") long id,@Valid @RequestBody QualityCharacteristicReviewRequest request){return Result.ok(service.review(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),revisionId,id,"REJECTED",request));}
}
