export type MdmDataType = 'STRING'|'TEXT'|'INTEGER'|'DECIMAL'|'BOOLEAN'|'DATE'|'DATETIME'|'ENUM'|'REFERENCE';
export interface MdmReferenceConfig { targetModelCode:string; valueFieldCode:string; displayFieldCode:string; statusFieldCode?:string|null; allowedStatuses:string[] }
export interface MdmField { id:number; code:string; name:string; dataType:MdmDataType; required:boolean; unique:boolean; readonly:boolean; searchable:boolean; sortable:boolean; listVisible:boolean; length:number|null; enumOptions:string[]; helpText:string|null; sortNo:number; referenceConfig?:MdmReferenceConfig|null }
export interface MdmModel { id:number; domainCode:string; code:string; name:string; recordType:string; versionEnabled:boolean; effectiveDateEnabled:boolean; organizationScopeEnabled:boolean; approvalRequired:boolean; status:string; currentModelVersion:number; uiSchema:Record<string,unknown>; fields:MdmField[]; modelApprovalRoleId?:number|null; publishApprovalStatus?:string; publishApprovalOrgId?:number|null }
export interface MdmRecord { id:string; modelId:number; businessCode:string; name:string; lifecycleStatus:string; currentVersionNo:number; modelVersionNo:number; scopeType:string; scopeIds:number[]; effectiveFrom:string|null; effectiveTo:string|null; attributes:Record<string,unknown>; version:number; createdAt:string; updatedAt:string }
export interface MdmRecordVersion { id:number; recordId:string; versionNo:number; snapshot:Record<string,unknown>; changeType:string; changeReason:string|null; effectiveFrom:string|null; effectiveTo:string|null; createdBy:number; createdByName:string; createdAt:string }
export type MdmRecordActionType = 'SUBMIT'|'APPROVE'|'REJECT'|'DEACTIVATE';
export interface MdmRecordAction { id:number; recordId:string; action:MdmRecordActionType; fromStatus:string; toStatus:string; comment:string|null; actorId:number; actorName:string; createdAt:string }
export type MdmApprovalTaskScope='TODO'|'DONE'|'STARTED';
export interface MdmApprovalTask { recordId:string; modelCode:string; modelName:string; businessCode:string; recordName:string; lifecycleStatus:string; submittedBy:number; submittedByName:string; submittedAt:string }
export interface MdmModelApprovalTask { modelId:number; modelCode:string; modelName:string; targetVersion:number; approvalStatus:string; submittedBy:number; submittedByName:string; submittedAt:string }
export interface SaveMdmRecord { businessCode:string; name:string; lifecycleStatus:string; scopeType:string; scopeIds:number[]; effectiveFrom?:string|null; effectiveTo?:string|null; attributes:Record<string,unknown>; expectedVersion?:number; changeReason?:string }
export interface MdmBatchUpdateItem { id:string; record:SaveMdmRecord }
export interface MdmBatchDeleteItem { id:string; expectedVersion:number }
export interface MdmBatchRowValidation { rowNo:number; businessCode:string; valid:boolean; errors:string[] }
export interface MdmBatchValidation { valid:boolean; total:number; rows:MdmBatchRowValidation[] }
export interface MdmImportPreview { taskId:string; status:string; fileName:string; records:SaveMdmRecord[]; validation:MdmBatchValidation }
export interface MdmImportTask { id:string; tenantId:number; modelId:number; fileName:string; status:'QUEUED'|'VALIDATING'|'FAILED'|'PRECHECK_FAILED'|'READY'|'COMMITTING'|'COMMITTED'; totalRows:number; validRows:number; invalidRows:number; importedRows:number; sourceFileAvailable:boolean; errorMessage:string|null; createdBy:number; createdByName:string; createdAt:string; committedAt:string|null }
export interface MdmPage<T> { records:T[]; total:number; pageNo:number; pageSize:number }
export interface CreateMdmModel { domainCode:string; code:string; name:string; recordType:string; versionEnabled:boolean; effectiveDateEnabled:boolean; organizationScopeEnabled:boolean; approvalRequired:boolean }
export interface SaveMdmModelDraft { approvalRequired:boolean; modelApprovalRoleId:number; fields:Array<Omit<MdmField,'id'|'length'>&{maxLength:number|null}>; uiSchema:Record<string,unknown> }
export interface MdmModelValidation { valid:boolean; errors:string[]; warnings:string[] }
export interface MdmValidationRule { id?:number; code:string; name:string; triggerPoint:'SAVE'|'BLUR'; ruleType:'REFERENCE_EXISTS'; fieldCode:string; severity:'BLOCK'|'WARNING'; message:string; condition:Record<string,unknown>; assertion:{targetModel:string;conditions:Array<{targetField:string;sourceField?:string;value?:unknown}>}; enabled:boolean; sortNo:number }
export interface MdmValidationIssue { fieldCode:string|null; severity:string; message:string }
export interface MdmValidationOutcome { valid:boolean; errors:MdmValidationIssue[]; warnings:MdmValidationIssue[] }
