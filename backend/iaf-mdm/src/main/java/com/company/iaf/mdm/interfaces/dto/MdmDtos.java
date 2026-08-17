package com.company.iaf.mdm.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
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
                             List<String> enumOptions, String helpText, int sortNo) {}
    public record SaveModelDraftRequest(@NotNull List<@Valid FieldDraft> fields,
                                        @NotNull Map<String, Object> uiSchema) {}
    public record ModelValidationResult(boolean valid, List<String> errors, List<String> warnings) {}
    public record SaveRecordRequest(@NotBlank String businessCode, @NotBlank String name, String lifecycleStatus,
                                    String scopeType, List<Long> scopeIds, LocalDate effectiveFrom,
                                    LocalDate effectiveTo, @NotNull Map<String, Object> attributes,
                                    Integer expectedVersion, String changeReason) {}
}
