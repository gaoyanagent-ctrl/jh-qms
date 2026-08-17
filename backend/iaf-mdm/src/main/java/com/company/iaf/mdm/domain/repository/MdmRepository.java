package com.company.iaf.mdm.domain.repository;

import com.company.iaf.mdm.domain.model.MdmModels;
import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MdmRepository {
    List<MdmModels.Model> findModels(long tenantId);
    Optional<MdmModels.Model> findModel(long tenantId, String modelCode);
    MdmModels.Model insertModel(long tenantId, long actorId, MdmDtos.CreateModelRequest request);
    void replaceDraft(long tenantId, long actorId, long modelId, List<MdmDtos.FieldDraft> fields, Map<String,Object> uiSchema);
    void publishModel(long tenantId, long actorId, MdmModels.Model model);
    List<MdmModels.Record> findRecords(long tenantId, long modelId, String keyword, long offset, int size);
    long countRecords(long tenantId, long modelId, String keyword);
    boolean businessCodeExists(long tenantId, long modelId, String businessCode, UUID excludingId);
    MdmModels.Record insertRecord(long tenantId, long actorId, MdmModels.Model model, String businessCode,
                                  String name, String lifecycleStatus, String scopeType, List<Long> scopeIds,
                                  java.time.LocalDate effectiveFrom, java.time.LocalDate effectiveTo,
                                  Map<String, Object> attributes);
    Optional<MdmModels.Record> findRecord(long tenantId, long modelId, UUID id);
    boolean updateRecord(long tenantId, long actorId, MdmModels.Record record, int expectedVersion);
    void insertVersion(long tenantId, long actorId, MdmModels.Record record, String changeType, String reason);
    List<MdmModels.RecordVersion> findRecordVersions(long tenantId, long modelId, UUID recordId);
    MdmModels.ImportTask insertImportTask(long tenantId, long actorId, long modelId, String modelCode,
                                          String fileName, List<MdmDtos.SaveRecordRequest> records,
                                          MdmDtos.BatchValidationResult validation, String sourceObjectKey,
                                          String sourceMediaType, long sourceSize);
    Optional<MdmModels.ImportTask> findImportTask(long tenantId, long modelId, UUID taskId);
    List<MdmModels.ImportTask> findImportTasks(long tenantId, long modelId);
    List<MdmDtos.SaveRecordRequest> findImportTaskRecords(long tenantId, long modelId, UUID taskId);
    MdmDtos.BatchValidationResult findImportTaskValidation(long tenantId, long modelId, UUID taskId);
    Optional<MdmModels.ImportArtifact> findImportArtifact(long tenantId, long modelId, UUID taskId);
    boolean claimImportTask(long tenantId, long modelId, UUID taskId, long actorId);
    void refreshImportTaskValidation(long tenantId, long modelId, UUID taskId, long actorId,
                                     MdmDtos.BatchValidationResult validation);
    void completeImportTask(long tenantId, long modelId, UUID taskId, long actorId, int importedRows);
}
