package com.company.iaf.mdm.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MdmModels {
    private MdmModels() {}

    public record Field(long id, String code, String name, String dataType, boolean required,
                        boolean unique, boolean readonly, boolean searchable, boolean sortable,
                        boolean listVisible, Integer length, List<String> enumOptions, String helpText, int sortNo,
                        ReferenceConfig referenceConfig) {}

    public record ReferenceConfig(String targetModelCode, String valueFieldCode, String displayFieldCode,
                                  String statusFieldCode, List<String> allowedStatuses) {}

    public record Model(long id, String domainCode, String code, String name, String recordType,
                        boolean versionEnabled, boolean effectiveDateEnabled, boolean organizationScopeEnabled,
                        boolean approvalRequired, String status, int currentModelVersion,
                        Map<String, Object> uiSchema, List<Field> fields) {}

    public record Record(UUID id, long modelId, String modelCode, String businessCode, String name,
                         String lifecycleStatus, int currentVersionNo, int modelVersionNo,
                         String scopeType, List<Long> scopeIds, LocalDate effectiveFrom, LocalDate effectiveTo,
                         Map<String, Object> attributes, int version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}

    public record RecordVersion(long id, UUID recordId, int versionNo, Map<String, Object> snapshot,
                                String changeType, String changeReason, LocalDate effectiveFrom,
                                LocalDate effectiveTo, long createdBy, String createdByName, OffsetDateTime createdAt) {}

    public record ImportTask(UUID id, long tenantId, long modelId, String modelCode, String fileName, String status,
                             int totalRows, int validRows, int invalidRows, int importedRows,
                             boolean sourceFileAvailable, String errorMessage, long createdBy, String createdByName,
                             OffsetDateTime createdAt, OffsetDateTime committedAt) {}

    public record ImportArtifact(String objectKey, String fileName, String mediaType, long size) {}

    public record ValidationRule(long id, long modelId, String code, String name, String triggerPoint,
                                 String ruleType, String fieldCode, String severity, String message,
                                 Map<String, Object> condition, Map<String, Object> assertion,
                                 boolean enabled, int sortNo) {}

    public record RecordAction(long id, UUID recordId, String action, String fromStatus, String toStatus,
                               String comment, long actorId, String actorName, OffsetDateTime createdAt) {}
}
