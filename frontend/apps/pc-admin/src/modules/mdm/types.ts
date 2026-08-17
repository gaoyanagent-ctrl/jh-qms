export type MdmDataType = 'STRING'|'TEXT'|'INTEGER'|'DECIMAL'|'BOOLEAN'|'DATE'|'DATETIME'|'ENUM';
export interface MdmField { id:number; code:string; name:string; dataType:MdmDataType; required:boolean; unique:boolean; readonly:boolean; searchable:boolean; sortable:boolean; listVisible:boolean; length:number|null; enumOptions:string[]; helpText:string|null; sortNo:number }
export interface MdmModel { id:number; domainCode:string; code:string; name:string; recordType:string; versionEnabled:boolean; effectiveDateEnabled:boolean; organizationScopeEnabled:boolean; approvalRequired:boolean; status:string; currentModelVersion:number; uiSchema:Record<string,unknown>; fields:MdmField[] }
export interface MdmRecord { id:string; modelId:number; businessCode:string; name:string; lifecycleStatus:string; currentVersionNo:number; modelVersionNo:number; scopeType:string; scopeIds:number[]; effectiveFrom:string|null; effectiveTo:string|null; attributes:Record<string,unknown>; version:number; createdAt:string; updatedAt:string }
export interface MdmRecordVersion { id:number; recordId:string; versionNo:number; snapshot:Record<string,unknown>; changeType:string; changeReason:string|null; effectiveFrom:string|null; effectiveTo:string|null; createdBy:number; createdByName:string; createdAt:string }
export interface SaveMdmRecord { businessCode:string; name:string; lifecycleStatus:string; scopeType:string; scopeIds:number[]; effectiveFrom?:string|null; effectiveTo?:string|null; attributes:Record<string,unknown>; expectedVersion?:number; changeReason?:string }
export interface MdmBatchRowValidation { rowNo:number; businessCode:string; valid:boolean; errors:string[] }
export interface MdmBatchValidation { valid:boolean; total:number; rows:MdmBatchRowValidation[] }
export interface MdmImportPreview { taskId:string; status:string; fileName:string; records:SaveMdmRecord[]; validation:MdmBatchValidation }
export interface MdmImportTask { id:string; modelId:number; fileName:string; status:'PRECHECK_FAILED'|'READY'|'COMMITTING'|'COMMITTED'; totalRows:number; validRows:number; invalidRows:number; importedRows:number; createdBy:number; createdByName:string; createdAt:string; committedAt:string|null }
export interface MdmPage<T> { records:T[]; total:number; pageNo:number; pageSize:number }
export interface CreateMdmModel { domainCode:string; code:string; name:string; recordType:string; versionEnabled:boolean; effectiveDateEnabled:boolean; organizationScopeEnabled:boolean; approvalRequired:boolean }
export interface SaveMdmModelDraft { fields:Array<Omit<MdmField,'id'|'length'>&{maxLength:number|null}>; uiSchema:Record<string,unknown> }
export interface MdmModelValidation { valid:boolean; errors:string[]; warnings:string[] }
