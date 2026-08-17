package com.company.iaf.mdm.interfaces.controller;

import com.company.iaf.mdm.application.MdmApplicationService;
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
import java.util.List; import java.util.UUID;

@Tag(name="MDM",description="Metadata-driven master data") @Validated @RestController @RequestMapping("/api/mdm/models")
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class MdmController {
 private final MdmApplicationService service; public MdmController(MdmApplicationService service){this.service=service;}
 @Operation(summary="List published master data models") @GetMapping public Result<List<MdmModels.Model>> models(){return Result.ok(service.listModels(tenant()));}
 @Operation(summary="Get model schema and UI schema") @GetMapping("/{code}/schema") public Result<MdmModels.Model> schema(@PathVariable String code){return Result.ok(service.schema(tenant(),code));}
 @Operation(summary="Create a draft master data model") @PostMapping public Result<MdmModels.Model> createModel(@Valid @RequestBody MdmDtos.CreateModelRequest request){return Result.ok(service.createModel(tenant(),actor(),request));}
 @Operation(summary="Save draft fields and UI schema") @PutMapping("/{code}/draft") public Result<MdmModels.Model> saveDraft(@PathVariable String code,@Valid @RequestBody MdmDtos.SaveModelDraftRequest request){return Result.ok(service.saveDraft(tenant(),actor(),code,request));}
 @Operation(summary="Validate a model definition") @PostMapping("/{code}/validate") public Result<MdmDtos.ModelValidationResult> validateModel(@PathVariable String code){return Result.ok(service.validateModel(tenant(),code));}
 @Operation(summary="Publish an immutable model version") @PostMapping("/{code}/publish") public Result<MdmModels.Model> publish(@PathVariable String code){return Result.ok(service.publish(tenant(),actor(),code));}
 @Operation(summary="List records") @GetMapping("/{code}/records") public Result<PageResult<MdmModels.Record>> records(@PathVariable String code,@RequestParam(required=false)String keyword,@RequestParam(defaultValue="1")@Min(1)int pageNo,@RequestParam(defaultValue="20")@Min(1)@Max(200)int pageSize){return Result.ok(service.records(tenant(),code,keyword,pageNo,pageSize));}
 @Operation(summary="Create record through the unified validation pipeline") @PostMapping("/{code}/records") public Result<MdmModels.Record> create(@PathVariable String code,@Valid @RequestBody MdmDtos.SaveRecordRequest request){return Result.ok(service.create(tenant(),actor(),code,request));}
 @Operation(summary="Update record with optimistic locking") @PutMapping("/{code}/records/{id}") public Result<MdmModels.Record> update(@PathVariable String code,@PathVariable UUID id,@Valid @RequestBody MdmDtos.SaveRecordRequest request){return Result.ok(service.update(tenant(),actor(),code,id,request));}
 private long tenant(){return TenantContext.getTenantId().orElseThrow(()->new BusinessException(CommonErrorCode.UNAUTHORIZED,"Tenant context is missing"));}
 private long actor(){return SecurityContext.getUserId().orElseThrow(()->new BusinessException(CommonErrorCode.UNAUTHORIZED));}
}
