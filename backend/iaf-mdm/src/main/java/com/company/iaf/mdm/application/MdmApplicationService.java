package com.company.iaf.mdm.application;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.domain.repository.MdmRepository;
import com.company.iaf.mdm.domain.service.DynamicRecordValidator;
import com.company.iaf.mdm.domain.service.ModelDefinitionValidator;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import com.company.iaf.platform.workflow.application.ApprovalApplicationService;
import com.company.iaf.platform.workflow.application.ApprovalStatus;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.ArrayList;

@Service
@ConditionalOnProperty(name = "iaf.mdm.enabled", havingValue = "true", matchIfMissing = true)
public class MdmApplicationService {
    private static final String MODEL_PUBLISH_BUSINESS_TYPE="MDM_MODEL_PUBLISH";
    private final MdmRepository repository;
    private final ConfiguredRuleValidator configuredRuleValidator;
    private final ApprovalApplicationService approvals;
    private final DynamicRecordValidator validator = new DynamicRecordValidator();
    private final ModelDefinitionValidator modelValidator = new ModelDefinitionValidator();
    public MdmApplicationService(MdmRepository repository, ConfiguredRuleValidator configuredRuleValidator, ApprovalApplicationService approvals) { this.repository = repository; this.configuredRuleValidator = configuredRuleValidator; this.approvals=approvals; }

    @RequiresPermission("mdm:model:view") @Transactional(readOnly = true)
    public List<MdmModels.Model> listModels(long tenantId) { return repository.findModels(tenantId); }
    @RequiresPermission("mdm:model:view") @Transactional(readOnly = true)
    public MdmModels.Model schema(long tenantId, String code) { return requireModel(tenantId, code); }
    @RequiresPermission("mdm:model:create") @Transactional
    public MdmModels.Model createModel(long tenantId, long actorId, MdmDtos.CreateModelRequest request) {
        if (repository.findModel(tenantId, request.code().trim()).isPresent()) throw new BusinessException(MdmErrorCode.MODEL_CODE_EXISTS);
        return repository.insertModel(tenantId, actorId, request);
    }
    @RequiresPermission("mdm:model:update") @Transactional
    public MdmModels.Model saveDraft(long tenantId, long actorId, String code, MdmDtos.SaveModelDraftRequest request) {
        MdmModels.Model model = requireModel(tenantId, code);
        if (!"DRAFT".equals(model.status()) && !"PUBLISHED".equals(model.status())) throw new BusinessException(MdmErrorCode.MODEL_NOT_EDITABLE);
        var result = modelValidator.validate(request.fields());
        var referenceErrors=validateReferenceTargets(tenantId,request.fields());
        if(request.approvalRequired()){Long roleId=approvalRoleId(request.uiSchema());if(roleId==null||!repository.roleExists(tenantId,roleId))referenceErrors.add("approval: a valid approver role is required");}
        if(!repository.roleExists(tenantId,request.modelApprovalRoleId()))referenceErrors.add("modelApproval: a valid approver role is required");
        if (!result.valid()||!referenceErrors.isEmpty()) {var errors=new ArrayList<>(result.errors());errors.addAll(referenceErrors);throw new BusinessException(MdmErrorCode.VALIDATION_FAILED,String.join("; ",errors));}
        repository.replaceDraft(tenantId, actorId, model.id(), request.approvalRequired(), request.modelApprovalRoleId(), request.fields(), request.uiSchema());
        return requireModel(tenantId, code);
    }
    @RequiresPermission("mdm:model:update") @Transactional(readOnly = true)
    public MdmDtos.ModelValidationResult validateModel(long tenantId, String code) {
        MdmModels.Model model = requireModel(tenantId, code);
        var fields=model.fields().stream().map(f -> new MdmDtos.FieldDraft(f.code(), f.name(), f.dataType(), f.required(), f.unique(), f.readonly(), f.searchable(), f.sortable(), f.listVisible(), f.length(), f.enumOptions(), f.helpText(), f.sortNo(), f.referenceConfig())).toList();
        var base=modelValidator.validate(fields);var errors=new ArrayList<>(base.errors());errors.addAll(validateReferenceTargets(tenantId,fields));return new MdmDtos.ModelValidationResult(errors.isEmpty(),errors,base.warnings());
    }
    @RequiresPermission("mdm:model:publish") @Transactional
    public MdmModels.Model submitModelPublication(long tenantId,long orgId,long actorId,String code,String comment) {
        MdmModels.Model model = requireModel(tenantId, code); var result = validateModel(tenantId, code);
        if (!"DRAFT".equals(model.status())) throw new BusinessException(MdmErrorCode.MODEL_NOT_EDITABLE);
        if (!result.valid()) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, String.join("; ", result.errors()));
        if(model.modelApprovalRoleId()==null||!repository.roleExists(tenantId,model.modelApprovalRoleId()))throw new BusinessException(MdmErrorCode.APPROVAL_ROLE_REQUIRED);
        if(!repository.submitModelApproval(tenantId,actorId,model.id(),orgId))throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE);
        try{approvals.submit(tenantId,orgId,MODEL_PUBLISH_BUSINESS_TYPE,model.id(),actorId,comment);}catch(IllegalStateException failure){throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE);}
        return requireModel(tenantId,code);
    }
    @RequiresPermission("mdm:model:approve") @Transactional
    public MdmModels.Model approveModelPublication(long tenantId,long actorId,String code,String comment){var model=requireModel(tenantId,code);requireModelApprover(tenantId,actorId,model);if(!"PENDING_APPROVAL".equals(model.status())||model.publishApprovalOrgId()==null)throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE);try{approvals.approve(tenantId,model.publishApprovalOrgId(),MODEL_PUBLISH_BUSINESS_TYPE,model.id(),actorId,comment);}catch(IllegalStateException failure){throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE,failure.getMessage());}repository.publishModel(tenantId,actorId,model);return requireModel(tenantId,code);}
    @RequiresPermission("mdm:model:approve") @Transactional
    public MdmModels.Model rejectModelPublication(long tenantId,long actorId,String code,String comment){var model=requireModel(tenantId,code);requireModelApprover(tenantId,actorId,model);if(!"PENDING_APPROVAL".equals(model.status())||model.publishApprovalOrgId()==null)throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE);try{approvals.reject(tenantId,model.publishApprovalOrgId(),MODEL_PUBLISH_BUSINESS_TYPE,model.id(),actorId,comment);}catch(IllegalStateException failure){throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE,failure.getMessage());}if(!repository.rejectModelApproval(tenantId,actorId,model.id()))throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_INVALID_STATE);return requireModel(tenantId,code);}
    @RequiresPermission("mdm:model:view") @Transactional(readOnly=true)
    public List<MdmModels.ModelApprovalTask> modelApprovalTasks(long tenantId,long actorId,String scope){var models=repository.findModels(tenantId).stream().collect(java.util.stream.Collectors.toMap(MdmModels.Model::id,item->item));return approvals.findLatestByBusinessType(tenantId,MODEL_PUBLISH_BUSINESS_TYPE).stream().filter(item->{var model=models.get(item.businessId());if(model==null)return false;return switch(scope==null?"TODO":scope.toUpperCase()){case "STARTED"->item.submittedBy()==actorId;case "DONE"->item.decidedBy()!=null&&item.decidedBy()==actorId;default->item.status()==ApprovalStatus.PENDING&&model.modelApprovalRoleId()!=null&&repository.userHasRole(tenantId,actorId,model.modelApprovalRoleId());};}).map(item->{var model=models.get(item.businessId());return new MdmModels.ModelApprovalTask(model.id(),model.code(),model.name(),model.currentModelVersion()+1,item.status().name(),item.submittedBy(),repository.userDisplayName(tenantId,item.submittedBy()),item.submittedAt().atOffset(ZoneOffset.UTC));}).sorted(java.util.Comparator.comparing(MdmModels.ModelApprovalTask::submittedAt).reversed()).toList();}
    @RequiresPermission("mdm:model:view") @Transactional(readOnly = true)
    public List<MdmModels.ValidationRule> validationRules(long tenantId, String code) {
        MdmModels.Model model=requireModel(tenantId,code); return repository.findValidationRules(tenantId,model.id(),null);
    }
    @RequiresPermission("mdm:model:update") @Transactional
    public List<MdmModels.ValidationRule> saveValidationRules(long tenantId,long actorId,String code,MdmDtos.SaveValidationRulesRequest request){
        MdmModels.Model model=requireModel(tenantId,code); repository.replaceValidationRules(tenantId,actorId,model.id(),request.rules());
        return repository.findValidationRules(tenantId,model.id(),null);
    }
    @RequiresPermission("mdm:record:view") @Transactional(readOnly = true)
    public PageResult<MdmModels.Record> records(long tenantId, String code, String keyword, int pageNo, int pageSize) {
        MdmModels.Model model = requireModel(tenantId, code); String q = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return new PageResult<>(repository.findRecords(tenantId, model.id(), q, (long)(pageNo - 1) * pageSize, pageSize), repository.countRecords(tenantId, model.id(), q), pageNo, pageSize);
    }
    @RequiresPermission("mdm:record:view") @Transactional(readOnly = true)
    public List<MdmModels.RecordVersion> recordVersions(long tenantId, String code, UUID id) {
        MdmModels.Model model = requireModel(tenantId, code);
        if (repository.findRecord(tenantId, model.id(), id).isEmpty()) throw new BusinessException(MdmErrorCode.RECORD_NOT_FOUND);
        return repository.findRecordVersions(tenantId, model.id(), id);
    }
    @RequiresPermission("mdm:record:create") @Transactional
    public MdmModels.Record create(long tenantId, long actorId, String code, MdmDtos.SaveRecordRequest request) {
        MdmModels.Model model = requireModel(tenantId, code); validate(tenantId, model, request);
        if (repository.businessCodeExists(tenantId, model.id(), request.businessCode().trim(), null)) throw new BusinessException(MdmErrorCode.BUSINESS_CODE_EXISTS);
        MdmModels.Record saved = repository.insertRecord(tenantId, actorId, model, request.businessCode().trim(), request.name().trim(), "DRAFT", scope(request), request.scopeIds(), request.effectiveFrom(), request.effectiveTo(), request.attributes());
        repository.insertVersion(tenantId, actorId, saved, "CREATE", request.changeReason()); return saved;
    }
    @RequiresPermission("mdm:record:create") @Transactional(readOnly = true)
    public MdmDtos.BatchValidationResult validateBatch(long tenantId, String code, MdmDtos.BatchRecordRequest request) {
        return validateBatchInternal(tenantId,code,request);
    }
    public MdmDtos.BatchValidationResult validateBatchInternal(long tenantId, String code, MdmDtos.BatchRecordRequest request) {
        MdmModels.Model model=requireModel(tenantId,code); var seen=new HashSet<String>(); var rows=new ArrayList<MdmDtos.BatchRowValidation>();
        for(int index=0;index<request.records().size();index++){
            var item=request.records().get(index); var errors=new ArrayList<String>(); String businessCode=item.businessCode()==null?"":item.businessCode().trim();
            if(businessCode.isBlank()) errors.add("businessCode: required");
            if(item.name()==null||item.name().isBlank()) errors.add("name: required");
            if(item.attributes()==null) errors.add("attributes: required");
            errors.addAll(validator.validate(model,item.attributes()==null?java.util.Map.of():item.attributes()));
            if(item.attributes()!=null) errors.addAll(configuredRuleValidator.validate(tenantId,model,item));
            if(!businessCode.isBlank()&&!seen.add(businessCode)) errors.add("businessCode: duplicated in batch");
            if(!businessCode.isBlank()&&repository.businessCodeExists(tenantId,model.id(),businessCode,null)) errors.add("businessCode: already exists");
            rows.add(new MdmDtos.BatchRowValidation(index+2,businessCode,errors.isEmpty(),errors));
        }
        return new MdmDtos.BatchValidationResult(rows.stream().allMatch(MdmDtos.BatchRowValidation::valid),rows.size(),rows);
    }
    @RequiresPermission("mdm:record:create") @Transactional
    public List<MdmModels.Record> createBatch(long tenantId,long actorId,String code,MdmDtos.BatchRecordRequest request){
        var validation=validateBatch(tenantId,code,request); if(!validation.valid()) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED,"Batch contains invalid rows");
        return request.records().stream().map(item->create(tenantId,actorId,code,item)).toList();
    }
    @RequiresPermission("mdm:record:update") @Transactional
    public MdmModels.Record update(long tenantId, long actorId, String code, UUID id, MdmDtos.SaveRecordRequest request) {
        MdmModels.Model model = requireModel(tenantId, code); validate(tenantId, model, request);
        if (request.expectedVersion() == null) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, "expectedVersion is required");
        if (repository.businessCodeExists(tenantId, model.id(), request.businessCode().trim(), id)) throw new BusinessException(MdmErrorCode.BUSINESS_CODE_EXISTS);
        MdmModels.Record current = repository.findRecord(tenantId, model.id(), id).orElseThrow(() -> new BusinessException(MdmErrorCode.RECORD_NOT_FOUND));
        if(!List.of("DRAFT","REJECTED").contains(current.lifecycleStatus()))throw new BusinessException(MdmErrorCode.RECORD_STATE_CONFLICT);
        MdmModels.Record changed = new MdmModels.Record(id, model.id(), model.code(), request.businessCode().trim(), request.name().trim(), current.lifecycleStatus(), current.currentVersionNo()+1, model.currentModelVersion(), scope(request), request.scopeIds(), request.effectiveFrom(), request.effectiveTo(), request.attributes(), current.version()+1, current.createdAt(), current.updatedAt());
        if (!repository.updateRecord(tenantId, actorId, changed, request.expectedVersion())) throw new BusinessException(MdmErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        MdmModels.Record saved = repository.findRecord(tenantId, model.id(), id).orElseThrow(); repository.insertVersion(tenantId, actorId, saved, "UPDATE", request.changeReason()); return saved;
    }
    @RequiresPermission("mdm:record:view") @Transactional(readOnly=true)
    public List<MdmModels.RecordAction> recordActions(long tenantId,String code,UUID id){var model=requireModel(tenantId,code);requireRecord(tenantId,model,id);return repository.findRecordActions(tenantId,model.id(),id);}
    @RequiresPermission("mdm:record:view") @Transactional(readOnly=true)
    public List<MdmModels.ApprovalTask> approvalTasks(long tenantId,long actorId,String scope){return repository.findApprovalTasks(tenantId,actorId,scope==null?"TODO":scope);}
    @RequiresPermission("mdm:record:submit") @Transactional
    public MdmModels.Record submit(long tenantId,long actorId,String code,UUID id,String comment){var model=requireModel(tenantId,code);var current=requireRecord(tenantId,model,id);if(model.approvalRequired()&&approvalRoleId(model.uiSchema())==null)throw new BusinessException(MdmErrorCode.APPROVAL_ROLE_REQUIRED);String target=model.approvalRequired()?"PENDING_APPROVAL":"ACTIVE";return transition(tenantId,actorId,model,current,List.of("DRAFT","REJECTED"),target,"SUBMIT",comment);}
    @RequiresPermission("mdm:record:approve") @Transactional
    public MdmModels.Record approve(long tenantId,long actorId,String code,UUID id,String comment){var model=requireModel(tenantId,code);requireApprover(tenantId,actorId,model);var current=requireRecord(tenantId,model,id);return transition(tenantId,actorId,model,current,List.of("PENDING_APPROVAL"),"ACTIVE","APPROVE",comment);}
    @RequiresPermission("mdm:record:approve") @Transactional
    public MdmModels.Record reject(long tenantId,long actorId,String code,UUID id,String comment){var model=requireModel(tenantId,code);requireApprover(tenantId,actorId,model);var current=requireRecord(tenantId,model,id);return transition(tenantId,actorId,model,current,List.of("PENDING_APPROVAL"),"REJECTED","REJECT",comment);}
    @RequiresPermission("mdm:record:disable") @Transactional
    public MdmModels.Record deactivate(long tenantId,long actorId,String code,UUID id,String comment){var model=requireModel(tenantId,code);var current=requireRecord(tenantId,model,id);return transition(tenantId,actorId,model,current,List.of("ACTIVE"),"INACTIVE","DEACTIVATE",comment);}
    private void validate(long tenantId,MdmModels.Model model,MdmDtos.SaveRecordRequest request){var errors=new ArrayList<>(validator.validate(model,request.attributes()));errors.addAll(configuredRuleValidator.validate(tenantId,model,request));if(!errors.isEmpty())throw new BusinessException(MdmErrorCode.VALIDATION_FAILED,String.join("; ",errors));}
    private MdmModels.Model requireModel(long tenantId, String code) { return repository.findModel(tenantId, code).orElseThrow(() -> new BusinessException(MdmErrorCode.MODEL_NOT_FOUND)); }
    private String scope(MdmDtos.SaveRecordRequest r) { return r.scopeType() == null ? "GROUP" : r.scopeType(); }
    private MdmModels.Record requireRecord(long tenantId,MdmModels.Model model,UUID id){return repository.findRecord(tenantId,model.id(),id).orElseThrow(()->new BusinessException(MdmErrorCode.RECORD_NOT_FOUND));}
    private Long approvalRoleId(java.util.Map<String,Object> ui){if(ui==null)return null;Object approval=ui.get("approval");if(!(approval instanceof java.util.Map<?,?> map))return null;Object value=map.get("roleId");if(value instanceof Number number)return number.longValue();try{return value==null?null:Long.valueOf(String.valueOf(value));}catch(NumberFormatException ignored){return null;}}
    private void requireApprover(long tenantId,long actorId,MdmModels.Model model){Long roleId=approvalRoleId(model.uiSchema());if(roleId==null)throw new BusinessException(MdmErrorCode.APPROVAL_ROLE_REQUIRED);if(!repository.userHasRole(tenantId,actorId,roleId))throw new BusinessException(MdmErrorCode.APPROVAL_FORBIDDEN);}
    private void requireModelApprover(long tenantId,long actorId,MdmModels.Model model){if(model.modelApprovalRoleId()==null||!repository.userHasRole(tenantId,actorId,model.modelApprovalRoleId()))throw new BusinessException(MdmErrorCode.MODEL_APPROVAL_FORBIDDEN);}
    private MdmModels.Record transition(long tenantId,long actorId,MdmModels.Model model,MdmModels.Record current,List<String> from,String target,String action,String comment){if(!repository.transitionRecord(tenantId,model.id(),current.id(),from,target,actorId))throw new BusinessException(MdmErrorCode.RECORD_STATE_CONFLICT);repository.insertRecordAction(tenantId,actorId,current.id(),action,current.lifecycleStatus(),target,comment);var saved=requireRecord(tenantId,model,current.id());repository.insertVersion(tenantId,actorId,saved,action,comment);return saved;}
    private List<String> validateReferenceTargets(long tenantId,List<MdmDtos.FieldDraft> fields){var errors=new ArrayList<String>();var common=java.util.Set.of("businessCode","name","lifecycleStatus");for(var field:fields){if(!"REFERENCE".equals(field.dataType())||field.referenceConfig()==null)continue;var config=field.referenceConfig();var target=repository.findModel(tenantId,config.targetModelCode());if(target.isEmpty()){errors.add(field.code()+": reference target model does not exist");continue;}var codes=new HashSet<>(common);target.get().fields().forEach(item->codes.add(item.code()));if(!codes.contains(config.valueFieldCode()))errors.add(field.code()+": reference value field does not exist");if(!codes.contains(config.displayFieldCode()))errors.add(field.code()+": reference display field does not exist");if(config.statusFieldCode()!=null&&!config.statusFieldCode().isBlank()&&!codes.contains(config.statusFieldCode()))errors.add(field.code()+": reference status field does not exist");}return errors;}
}
