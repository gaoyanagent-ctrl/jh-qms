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
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import javax.sql.DataSource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnSingleCandidate(DataSource.class)
public class MdmApplicationService {
    private final MdmRepository repository;
    private final DynamicRecordValidator validator = new DynamicRecordValidator();
    private final ModelDefinitionValidator modelValidator = new ModelDefinitionValidator();
    public MdmApplicationService(MdmRepository repository) { this.repository = repository; }

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
        if (!"DRAFT".equals(model.status())) throw new BusinessException(MdmErrorCode.MODEL_NOT_EDITABLE);
        var result = modelValidator.validate(request.fields());
        if (!result.valid()) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, String.join("; ", result.errors()));
        repository.replaceDraft(tenantId, actorId, model.id(), request.fields(), request.uiSchema());
        return requireModel(tenantId, code);
    }
    @RequiresPermission("mdm:model:update") @Transactional(readOnly = true)
    public MdmDtos.ModelValidationResult validateModel(long tenantId, String code) {
        MdmModels.Model model = requireModel(tenantId, code);
        return modelValidator.validate(model.fields().stream().map(f -> new MdmDtos.FieldDraft(f.code(), f.name(), f.dataType(), f.required(), f.unique(), f.readonly(), f.searchable(), f.sortable(), f.listVisible(), f.length(), f.enumOptions(), f.helpText(), f.sortNo())).toList());
    }
    @RequiresPermission("mdm:model:publish") @Transactional
    public MdmModels.Model publish(long tenantId, long actorId, String code) {
        MdmModels.Model model = requireModel(tenantId, code); var result = validateModel(tenantId, code);
        if (!"DRAFT".equals(model.status())) throw new BusinessException(MdmErrorCode.MODEL_NOT_EDITABLE);
        if (!result.valid()) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, String.join("; ", result.errors()));
        repository.publishModel(tenantId, actorId, model); return requireModel(tenantId, code);
    }
    @RequiresPermission("mdm:record:view") @Transactional(readOnly = true)
    public PageResult<MdmModels.Record> records(long tenantId, String code, String keyword, int pageNo, int pageSize) {
        MdmModels.Model model = requireModel(tenantId, code); String q = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return new PageResult<>(repository.findRecords(tenantId, model.id(), q, (long)(pageNo - 1) * pageSize, pageSize), repository.countRecords(tenantId, model.id(), q), pageNo, pageSize);
    }
    @RequiresPermission("mdm:record:create") @Transactional
    public MdmModels.Record create(long tenantId, long actorId, String code, MdmDtos.SaveRecordRequest request) {
        MdmModels.Model model = requireModel(tenantId, code); validate(model, request);
        if (repository.businessCodeExists(tenantId, model.id(), request.businessCode().trim(), null)) throw new BusinessException(MdmErrorCode.BUSINESS_CODE_EXISTS);
        MdmModels.Record saved = repository.insertRecord(tenantId, actorId, model, request.businessCode().trim(), request.name().trim(), status(request), scope(request), request.scopeIds(), request.effectiveFrom(), request.effectiveTo(), request.attributes());
        repository.insertVersion(tenantId, actorId, saved, "CREATE", request.changeReason()); return saved;
    }
    @RequiresPermission("mdm:record:update") @Transactional
    public MdmModels.Record update(long tenantId, long actorId, String code, UUID id, MdmDtos.SaveRecordRequest request) {
        MdmModels.Model model = requireModel(tenantId, code); validate(model, request);
        if (request.expectedVersion() == null) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, "expectedVersion is required");
        if (repository.businessCodeExists(tenantId, model.id(), request.businessCode().trim(), id)) throw new BusinessException(MdmErrorCode.BUSINESS_CODE_EXISTS);
        MdmModels.Record current = repository.findRecord(tenantId, model.id(), id).orElseThrow(() -> new BusinessException(MdmErrorCode.RECORD_NOT_FOUND));
        MdmModels.Record changed = new MdmModels.Record(id, model.id(), model.code(), request.businessCode().trim(), request.name().trim(), status(request), current.currentVersionNo()+1, model.currentModelVersion(), scope(request), request.scopeIds(), request.effectiveFrom(), request.effectiveTo(), request.attributes(), current.version()+1, current.createdAt(), current.updatedAt());
        if (!repository.updateRecord(tenantId, actorId, changed, request.expectedVersion())) throw new BusinessException(MdmErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        MdmModels.Record saved = repository.findRecord(tenantId, model.id(), id).orElseThrow(); repository.insertVersion(tenantId, actorId, saved, "UPDATE", request.changeReason()); return saved;
    }
    private void validate(MdmModels.Model model, MdmDtos.SaveRecordRequest request) { var errors = validator.validate(model, request.attributes()); if (!errors.isEmpty()) throw new BusinessException(MdmErrorCode.VALIDATION_FAILED, String.join("; ", errors)); }
    private MdmModels.Model requireModel(long tenantId, String code) { return repository.findModel(tenantId, code).orElseThrow(() -> new BusinessException(MdmErrorCode.MODEL_NOT_FOUND)); }
    private String status(MdmDtos.SaveRecordRequest r) { return r.lifecycleStatus() == null ? "DRAFT" : r.lifecycleStatus(); }
    private String scope(MdmDtos.SaveRecordRequest r) { return r.scopeType() == null ? "GROUP" : r.scopeType(); }
}
