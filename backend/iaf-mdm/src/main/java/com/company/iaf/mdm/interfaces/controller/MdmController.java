package com.company.iaf.mdm.interfaces.controller;

import com.company.iaf.mdm.application.MdmApplicationService;
import com.company.iaf.mdm.application.MdmExcelImportService;
import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; import jakarta.validation.constraints.Max; import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List; import java.util.UUID;

@Tag(name="MDM",description="Metadata-driven master data") @Validated @RestController @RequestMapping("/api/mdm/models")
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class MdmController {
 private final MdmApplicationService service; private final MdmExcelImportService excel;
 public MdmController(MdmApplicationService service,MdmExcelImportService excel){this.service=service;this.excel=excel;}
 @Operation(summary="List published master data models") @GetMapping public Result<List<MdmModels.Model>> models(){return Result.ok(service.listModels(tenant()));}
 @Operation(summary="Get model schema and UI schema") @GetMapping("/{code}/schema") public Result<MdmModels.Model> schema(@PathVariable("code") String code){return Result.ok(service.schema(tenant(),code));}
 @Operation(summary="Create a draft master data model") @PostMapping public Result<MdmModels.Model> createModel(@Valid @RequestBody MdmDtos.CreateModelRequest request){return Result.ok(service.createModel(tenant(),actor(),request));}
 @Operation(summary="Save draft fields and UI schema") @PutMapping("/{code}/draft") public Result<MdmModels.Model> saveDraft(@PathVariable("code") String code,@Valid @RequestBody MdmDtos.SaveModelDraftRequest request){return Result.ok(service.saveDraft(tenant(),actor(),code,request));}
 @Operation(summary="Validate a model definition") @PostMapping("/{code}/validate") public Result<MdmDtos.ModelValidationResult> validateModel(@PathVariable("code") String code){return Result.ok(service.validateModel(tenant(),code));}
 @Operation(summary="Submit an immutable model version for publication approval") @PostMapping("/{code}/publish") public Result<MdmModels.Model> publish(@PathVariable("code") String code,@RequestBody(required=false) MdmDtos.RecordActionRequest request){return Result.ok(service.submitModelPublication(tenant(),org(),actor(),code,request==null?null:request.comment()));}
 @Operation(summary="Approve and publish a model version") @PostMapping("/{code}/publish/approve") public Result<MdmModels.Model> approveModelPublication(@PathVariable("code")String code,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.approveModelPublication(tenant(),actor(),code,request.comment()));}
 @Operation(summary="Reject a model publication request") @PostMapping("/{code}/publish/reject") public Result<MdmModels.Model> rejectModelPublication(@PathVariable("code")String code,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.rejectModelPublication(tenant(),actor(),code,request.comment()));}
 @Operation(summary="List configurable validation rules") @GetMapping("/{code}/validation-rules") public Result<List<MdmModels.ValidationRule>> validationRules(@PathVariable("code")String code){return Result.ok(service.validationRules(tenant(),code));}
 @Operation(summary="Replace configurable validation rules") @PutMapping("/{code}/validation-rules") public Result<List<MdmModels.ValidationRule>> saveValidationRules(@PathVariable("code")String code,@Valid @RequestBody MdmDtos.SaveValidationRulesRequest request){return Result.ok(service.saveValidationRules(tenant(),actor(),code,request));}
 @Operation(summary="Validate one field on blur without persistence") @PostMapping("/{code}/records/validate-field") public Result<MdmDtos.ValidationOutcome> validateField(@PathVariable("code")String code,@Valid @RequestBody MdmDtos.FieldValidationRequest request){return Result.ok(service.validateField(tenant(),code,request));}
 @Operation(summary="List records") @GetMapping("/{code}/records") public Result<PageResult<MdmModels.Record>> records(@PathVariable("code") String code,@RequestParam(name="keyword",required=false)String keyword,@RequestParam(name="pageNo",defaultValue="1")@Min(1)int pageNo,@RequestParam(name="pageSize",defaultValue="20")@Min(1)@Max(200)int pageSize){return Result.ok(service.records(tenant(),code,keyword,pageNo,pageSize));}
 @Operation(summary="List MDM approval tasks") @GetMapping("/approval-tasks") public Result<List<MdmModels.ApprovalTask>> approvalTasks(@RequestParam(name="scope",defaultValue="TODO")String scope){return Result.ok(service.approvalTasks(tenant(),actor(),scope));}
 @Operation(summary="List MDM model publication approval tasks") @GetMapping("/model-approval-tasks") public Result<List<MdmModels.ModelApprovalTask>> modelApprovalTasks(@RequestParam(name="scope",defaultValue="TODO")String scope){return Result.ok(service.modelApprovalTasks(tenant(),actor(),scope));}
 @Operation(summary="List immutable record versions") @GetMapping("/{code}/records/{id}/versions") public Result<List<MdmModels.RecordVersion>> recordVersions(@PathVariable("code") String code,@PathVariable("id") UUID id){return Result.ok(service.recordVersions(tenant(),code,id));}
 @Operation(summary="List record lifecycle actions") @GetMapping("/{code}/records/{id}/actions") public Result<List<MdmModels.RecordAction>> recordActions(@PathVariable("code")String code,@PathVariable("id")UUID id){return Result.ok(service.recordActions(tenant(),code,id));}
 @Operation(summary="Create record through the unified validation pipeline") @PostMapping("/{code}/records") public Result<MdmModels.Record> create(@PathVariable("code") String code,@Valid @RequestBody MdmDtos.SaveRecordRequest request){return Result.ok(service.create(tenant(),actor(),code,request));}
 @Operation(summary="Prevalidate pasted or imported records") @PostMapping("/{code}/records/batch-validate") public Result<MdmDtos.BatchValidationResult> validateBatch(@PathVariable("code") String code,@Valid @RequestBody MdmDtos.BatchRecordRequest request){return Result.ok(service.validateBatch(tenant(),code,request));}
 @Operation(summary="Create a validated record batch") @PostMapping("/{code}/records/batch") public Result<List<MdmModels.Record>> createBatch(@PathVariable("code") String code,@Valid @RequestBody MdmDtos.BatchRecordRequest request){return Result.ok(service.createBatch(tenant(),actor(),code,request));}
 @Operation(summary="Download a model-driven Excel import template") @GetMapping("/{code}/import-template") public ResponseEntity<byte[]> importTemplate(@PathVariable("code") String code){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+code+"-import-template.xlsx\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(excel.template(tenant(),code));}
 @Operation(summary="Upload and create an Excel import precheck task") @PostMapping(value="/{code}/imports",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Result<MdmDtos.ImportPreview> previewImport(@PathVariable("code") String code,@RequestPart("file") MultipartFile file){return Result.ok(excel.preview(tenant(),actor(),code,file));}
 @Operation(summary="Queue a workbook for asynchronous precheck") @PostMapping(value="/{code}/imports/async",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Result<MdmModels.ImportTask> queueImport(@PathVariable("code")String code,@RequestPart("file")MultipartFile file){return Result.ok(excel.queue(tenant(),actor(),code,file));}
 @Operation(summary="List Excel import tasks") @GetMapping("/{code}/imports") public Result<List<MdmModels.ImportTask>> importTasks(@PathVariable("code") String code){return Result.ok(excel.tasks(tenant(),code));}
 @Operation(summary="Get row validation results for an import task") @GetMapping("/{code}/imports/{taskId}/errors") public Result<MdmDtos.BatchValidationResult> importErrors(@PathVariable("code") String code,@PathVariable("taskId") UUID taskId){return Result.ok(excel.errors(tenant(),code,taskId));}
 @Operation(summary="Commit a ready import task") @PostMapping("/{code}/imports/{taskId}/commit") public Result<MdmModels.ImportTask> commitImport(@PathVariable("code") String code,@PathVariable("taskId") UUID taskId){return Result.ok(excel.commit(tenant(),actor(),code,taskId));}
 @Operation(summary="Download the archived source workbook") @GetMapping("/{code}/imports/{taskId}/source") public ResponseEntity<byte[]> importSource(@PathVariable("code")String code,@PathVariable("taskId")UUID taskId){return download(excel.sourceFile(tenant(),code,taskId));}
 @Operation(summary="Download the row validation workbook") @GetMapping("/{code}/imports/{taskId}/result") public ResponseEntity<byte[]> importResult(@PathVariable("code")String code,@PathVariable("taskId")UUID taskId){return download(excel.resultFile(tenant(),code,taskId));}
 @Operation(summary="Update record with optimistic locking") @PutMapping("/{code}/records/{id}") public Result<MdmModels.Record> update(@PathVariable("code") String code,@PathVariable("id") UUID id,@Valid @RequestBody MdmDtos.SaveRecordRequest request){return Result.ok(service.update(tenant(),actor(),code,id,request));}
 @Operation(summary="Atomically update an edited record batch") @PutMapping("/{code}/records/batch") public Result<List<MdmModels.Record>> updateBatch(@PathVariable("code")String code,@Valid @RequestBody MdmDtos.BatchUpdateRequest request){return Result.ok(service.updateBatch(tenant(),actor(),code,request));}
 @Operation(summary="Logically delete a draft record batch") @DeleteMapping("/{code}/records/batch") public Result<Void> deleteBatch(@PathVariable("code")String code,@Valid @RequestBody MdmDtos.BatchDeleteRequest request){service.deleteBatch(tenant(),actor(),code,request);return Result.ok(null);}
 @Operation(summary="Submit record for approval or activate directly") @PostMapping("/{code}/records/{id}/submit") public Result<MdmModels.Record> submit(@PathVariable("code")String code,@PathVariable("id")UUID id,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.submit(tenant(),actor(),code,id,request.comment()));}
 @Operation(summary="Approve record") @PostMapping("/{code}/records/{id}/approve") public Result<MdmModels.Record> approve(@PathVariable("code")String code,@PathVariable("id")UUID id,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.approve(tenant(),actor(),code,id,request.comment()));}
 @Operation(summary="Reject record") @PostMapping("/{code}/records/{id}/reject") public Result<MdmModels.Record> reject(@PathVariable("code")String code,@PathVariable("id")UUID id,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.reject(tenant(),actor(),code,id,request.comment()));}
 @Operation(summary="Deactivate active record") @PostMapping("/{code}/records/{id}/deactivate") public Result<MdmModels.Record> deactivate(@PathVariable("code")String code,@PathVariable("id")UUID id,@RequestBody MdmDtos.RecordActionRequest request){return Result.ok(service.deactivate(tenant(),actor(),code,id,request.comment()));}
 private long tenant(){return TenantContext.getTenantId().orElseThrow(()->new BusinessException(CommonErrorCode.UNAUTHORIZED,"Tenant context is missing"));}
 private long actor(){return SecurityContext.getUserId().orElseThrow(()->new BusinessException(CommonErrorCode.UNAUTHORIZED));}
 private long org(){return SecurityContext.getCurrentOrgId().orElseThrow(()->new BusinessException(CommonErrorCode.VALIDATION_FAILED,"Organization context is required"));}
 private ResponseEntity<byte[]> download(MdmDtos.ImportDownload file){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.fileName(),java.nio.charset.StandardCharsets.UTF_8).build().toString()).contentType(MediaType.parseMediaType(file.mediaType())).body(file.content());}
}
