package com.company.iaf.mdm.interfaces.dto;

import com.company.iaf.mdm.domain.model.MdmModels;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class MdmDtos {
    private MdmDtos() {}
    public record CreateModelRequest(@NotBlank String domainCode, @NotBlank String code, @NotBlank String name,
                                     String recordType, boolean versionEnabled, boolean effectiveDateEnabled,
                                     boolean organizationScopeEnabled, boolean approvalRequired) {}
    public record FieldDraft(@NotBlank String code, @NotBlank String name, @NotBlank String dataType,
                             boolean required, boolean unique, boolean readonly, boolean searchable,
                             boolean sortable, boolean listVisible, Integer maxLength,
                             List<String> enumOptions, String helpText, int sortNo,
                             MdmModels.ReferenceConfig referenceConfig) {}
    public record SaveModelDraftRequest(boolean approvalRequired, @NotNull Long modelApprovalRoleId,
                                        @NotNull List<@Valid FieldDraft> fields,
                                        @NotNull Map<String, Object> uiSchema) {}
    public record ModelValidationResult(boolean valid, List<String> errors, List<String> warnings) {}
    public record SaveRecordRequest(@NotBlank String businessCode, @NotBlank String name, String lifecycleStatus,
                                    String scopeType, List<Long> scopeIds, LocalDate effectiveFrom,
                                    LocalDate effectiveTo, @NotNull Map<String, Object> attributes,
                                    Integer expectedVersion, String changeReason) {}
    public record BatchRecordRequest(@NotNull @Size(min=1,max=1000) List<SaveRecordRequest> records) {}
    public record BatchUpdateItem(@NotNull java.util.UUID id, @NotNull @Valid SaveRecordRequest record) {}
    public record BatchUpdateRequest(@NotNull @Size(min=1,max=200) List<@Valid BatchUpdateItem> items) {}
    public record BatchDeleteItem(@NotNull java.util.UUID id, @NotNull Integer expectedVersion) {}
    public record BatchDeleteRequest(@NotNull @Size(min=1,max=200) List<@Valid BatchDeleteItem> items) {}
    public record BatchRowValidation(int rowNo, String businessCode, boolean valid, List<String> errors) {}
    public record BatchValidationResult(boolean valid, int total, List<BatchRowValidation> rows) {}
    public record ImportPreview(java.util.UUID taskId, String status, String fileName, List<SaveRecordRequest> records,
                                BatchValidationResult validation) {}
    public record ImportDownload(byte[] content, String fileName, String mediaType) {}
    public record ValidationRuleDraft(@NotBlank String code, @NotBlank String name, @NotBlank String triggerPoint,
                                      @NotBlank String ruleType, String fieldCode, @NotBlank String severity,
                                      @NotBlank String message, @NotNull Map<String, Object> condition,
                                      @NotNull Map<String, Object> assertion, boolean enabled, int sortNo) {}
    public record SaveValidationRulesRequest(@NotNull List<@Valid ValidationRuleDraft> rules) {}
    public record FieldValidationRequest(@NotBlank String fieldCode, @NotNull @Valid SaveRecordRequest record) {}
    public record ValidationIssue(String fieldCode, String severity, String message) {}
    public record ValidationOutcome(boolean valid, List<ValidationIssue> errors, List<ValidationIssue> warnings) {}
    public record ReferenceCondition(String targetField, Object value) {}
    public record RecordActionRequest(String comment) {}
}
