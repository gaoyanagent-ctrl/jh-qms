export interface Result<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNo: number;
  pageSize: number;
}

export type QmsPartStatus = 'ACTIVE' | 'INACTIVE';
export type QmsDrawingType = 'PRODUCT' | 'PART' | 'ASSEMBLY' | 'OTHER';
export type QmsDrawingSourceSystem = 'MANUAL' | 'PLM' | 'MIGRATION';
export type QmsDrawingStatus = 'ACTIVE' | 'OBSOLETE';
export type QmsDrawingRevisionStatus =
  | 'DRAFT'
  | 'UPLOADED'
  | 'PARSING'
  | 'PARSED'
  | 'REVIEWING'
  | 'CONFIRMED'
  | 'RELEASED'
  | 'SUPERSEDED'
  | 'OBSOLETE'
  | 'FAILED';
export type QmsParseStatus = 'PENDING' | 'RUNNING' | 'PARTIAL_SUCCESS' | 'SUCCESS' | 'FAILED' | 'CANCELLED';
export type QmsReviewStatus = 'PENDING' | 'REVIEWING' | 'CONFIRMED' | 'REJECTED';
export type QmsParseJobStatus = 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';

export interface QmsPart {
  id: number;
  orgId: number;
  partNo: string;
  materialNo: string | null;
  partName: string;
  customerId: number | null;
  vehicleModel: string | null;
  supplierId: number | null;
  importanceLevel: string | null;
  status: QmsPartStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface QmsPartCreateRequest {
  partNo: string;
  materialNo?: string | null;
  partName: string;
  customerId?: number | null;
  vehicleModel?: string | null;
  supplierId?: number | null;
  importanceLevel?: string | null;
}

export interface QmsDrawing {
  id: number;
  partId: number;
  drawingNo: string;
  drawingName: string;
  drawingType: QmsDrawingType;
  sourceSystem: QmsDrawingSourceSystem;
  status: QmsDrawingStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface QmsDrawingCreateRequest {
  drawingNo: string;
  drawingName: string;
  drawingType: QmsDrawingType;
  sourceSystem?: QmsDrawingSourceSystem | null;
}

export interface QmsDrawingRevision {
  id: number;
  drawingId: number;
  revisionCode: string;
  revisionSeq: number;
  fileId: number | null;
  fileType: string | null;
  effectiveDate: string | null;
  releaseDate: string | null;
  supersedesRevisionId: number | null;
  parseStatus: QmsParseStatus;
  reviewStatus: QmsReviewStatus;
  status: QmsDrawingRevisionStatus;
  checksum: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface QmsDrawingRevisionCreateRequest {
  revisionCode: string;
  effectiveDate?: string | null;
  supersedesRevisionId?: number | null;
}

export interface QmsDrawingParseJob {
  id: number;
  revisionId: number;
  attemptNo: number;
  status: QmsParseJobStatus;
  parserType: string;
  errorCode: string | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface QmsBoundingBox { x: number; y: number; width: number; height: number; }
export interface QmsSourceEvidence {
  id: number; sourceFileId: number; drawingRevisionId: number; parseJobId: number;
  evidenceKey: string; entityId: string | null; entityHandle: string | null;
  sheetNo: string; pageNo: number | null; bbox: QmsBoundingBox;
  rawText: string | null; normalizedText: string | null;
  extractorType: 'PDF_VECTOR' | 'OCR' | 'DWG_ENTITY' | 'VLM' | 'LLM' | 'RULE' | 'MANUAL';
  extractorVersion: string; modelName: string | null; modelVersion: string | null;
  confidence: number; createdAt: string;
}
export interface QmsQualityCharacteristic {
  id: number; partId: number; drawingRevisionId: number; sourceEntityId: string | null;
  evidenceId: number | null; characteristicCode: string; characteristicType: string; name: string;
  nominalValue: number | null; upperTolerance: number | null; lowerTolerance: number | null;
  upperLimit: number | null; lowerLimit: number | null; unit: string | null;
  specialCharacteristicCode: string | null; confidence: number; status: string;
  inspectionDimension: boolean; referenceDimension: boolean; idealDimension: boolean;
  fitDimension: boolean; locationDimension: boolean; regulatoryFlag: boolean;
  mandatoryInspection: boolean;
  reviewStatus: 'PENDING' | 'CONFIRMED' | 'REJECTED'; reviewedBy: number | null;
  reviewedAt: string | null; reviewComment: string | null; version: number;
}
export interface QmsQualityCharacteristicReviewRequest {
  version: number; name?: string | null; nominalValue?: number | null;
  upperTolerance?: number | null; lowerTolerance?: number | null; unit?: string | null;
  characteristicType?: string | null; specialCharacteristicCode?: string | null;
  inspectionDimension?: boolean | null; referenceDimension?: boolean | null;
  idealDimension?: boolean | null; fitDimension?: boolean | null; locationDimension?: boolean | null;
  regulatoryFlag?: boolean | null; mandatoryInspection?: boolean | null;
  comment?: string | null;
}
export interface QmsQualityCharacteristicCreateRequest {
  characteristicType: string; name: string; nominalValue?: number | null;
  upperTolerance?: number | null; lowerTolerance?: number | null; unit?: string | null;
  specialCharacteristicCode?: string | null; inspectionDimension: boolean;
  referenceDimension: boolean; idealDimension: boolean; fitDimension: boolean;
  locationDimension: boolean; regulatoryFlag: boolean; mandatoryInspection: boolean;
  comment?: string | null;
}
export interface QmsQualityCharacteristicBulkReviewRequest {
  decision: 'CONFIRMED' | 'REJECTED'; targets: Array<{ id: number; version: number }>;
  comment?: string | null;
}
export interface QmsDrawingLegendRule {
  id: number; ruleCode: string; marker: string; description: string;
  targetField: 'INSPECTION_DIMENSION' | 'LOCATION_DIMENSION' | 'FIT_DIMENSION' | 'REFERENCE_DIMENSION' | 'SPECIAL_CODE' | 'REGULATORY_FLAG';
  targetValue: string | null; matchMode: 'CONTAINS' | 'WRAPS_VALUE';
  priority: number; enabled: boolean; version: number;
}
export interface QmsDrawingLegendRuleUpdateRequest {
  rules: Array<Pick<QmsDrawingLegendRule, 'id' | 'version' | 'marker' | 'description' | 'enabled' | 'priority'>>;
}
export interface QmsInspectionStandardItem { id:number;sequenceNo:number;category:string;itemName:string;requirement:string;characteristicId:number;nominalValue:number|null;lowerLimit:number|null;upperLimit:number|null;specialCharacteristicCode:string|null;supplierBatchSampling:string|null;supplierBatchMethod:string|null;supplierAnnualSampling:string|null;supplierAnnualMethod:string|null;remark:string|null;sourceType:string;evidenceId:number|null;confidence:number;reviewStatus:string; }
export interface QmsInspectionStandardApprovalAction { action:string;actorId:number;comment:string|null;actedAt:string; }
export interface QmsInspectionStandard { id:number;standardNo:string;partId:number;drawingRevisionId:number;documentVersion:number;status:string;approvalStatus:string;sourceType:string;reactionPlan:string|null;version:number;updatedAt:string;submittedBy:number|null;submittedAt:string|null;approvedBy:number|null;approvedAt:string|null;releasedBy:number|null;releasedAt:string|null;approvalActions:QmsInspectionStandardApprovalAction[];items:QmsInspectionStandardItem[]; }
export interface QmsValidationPlanItem { id:number;sequenceNo:number;testItem:string;standardSource:string|null;methodAcceptanceCriteria:string;laboratoryId:number|null;dvRequired:boolean;pvRequired:boolean;typeRequired:boolean;batchRequired:boolean;quantity:number|null;startDate:string|null;endDate:string|null;equivalentInfo:string|null;sourceInspectionItemId:number;sourceCharacteristicId:number;evidenceId:number|null;reviewStatus:string; }
export interface QmsValidationPlan { id:number;planNo:string;partId:number;inspectionStandardId:number;drawingRevisionId:number;documentVersion:number;status:string;approvalStatus:string;supplierId:number|null;version:number;updatedAt:string;submittedBy:number|null;submittedAt:string|null;approvedBy:number|null;approvedAt:string|null;releasedBy:number|null;releasedAt:string|null;approvalActions:QmsInspectionStandardApprovalAction[];items:QmsValidationPlanItem[]; }
export interface QmsDrawingIntermediateModel {
  id: number; revisionId: number; parseJobId: number; schemaVersion: string;
  documentId: string; revisionCode: string;
  model: { schemaVersion: string; documentId: string; revision: string; sheets: Array<{
    sheetNo: string; width: number; height: number; titleBlock: Record<string, unknown>;
    views: unknown[]; entities: Array<{ entityId: string; geometry?: Record<string, unknown> }>;
    notes: unknown[]; characteristicCandidates: unknown[];
    preview?: { format: 'SVG'; content: string; viewBox: QmsBoundingBox;
      coordinateSystem: 'CAD_Y_UP' | 'SVG_NATIVE'; generatedBy: string };
  }> }; createdAt: string;
}

export interface QmsFileObject {
  id: number;
  originalName: string;
  mediaType: string;
  fileExtension: string;
  sizeBytes: number;
  checksumSha256: string;
  createdAt: string;
}
export type QmsDrawingRevisionFileRole = 'DWG_SOURCE' | 'PDF_REFERENCE';
export interface QmsDrawingRevisionFile {
  role: QmsDrawingRevisionFileRole;
  file: QmsFileObject;
}

export interface LoginRequest {
  tenantCode: string;
  username: string;
  password: string;
}

export interface AuthPrincipal {
  tenantId: number;
  userId: number;
  username: string;
  displayName: string;
  currentOrgId?: number | null;
  organizations?: PlatformUserOrg[];
  permissions: string[];
}

export interface LoginResponse extends AuthPrincipal {
  tokenType: 'Bearer' | string;
  accessToken: string;
  expiresAt: string;
}

export type UserStatus = 'ENABLED' | 'DISABLED';

export interface PlatformUser {
  id: number;
  tenantId: number;
  username: string;
  displayName: string;
  mobile: string | null;
  email: string | null;
  status: UserStatus;
  primaryOrgId: number | null;
  organizations?: PlatformUserOrg[];
  createdAt: string;
  updatedAt: string;
}

export interface PlatformUserOrg {
  id: number;
  orgId: number;
  orgCode: string;
  orgName: string;
  orgType: string;
  primary: boolean;
  scopeWeight: number;
  validFrom?: string | null;
  validTo?: string | null;
}

export interface UserOrganizationsResponse {
  userId: number;
  primaryOrgId: number | null;
  organizations: PlatformUserOrg[];
}

export interface UserOrgAssignRequest {
  organizations: Array<{
    orgId: number;
    primary: boolean;
    scopeWeight?: number;
    validFrom?: string | null;
    validTo?: string | null;
  }>;
}

export interface UserOrgContextSwitchRequest {
  orgId: number;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  displayName: string;
  mobile?: string | null;
  email?: string | null;
  primaryOrgId: number;
}

export interface UserUpdateRequest {
  displayName: string;
  mobile?: string | null;
  email?: string | null;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export type OrgType = 'COMPANY' | 'DEPARTMENT' | 'TEAM';
export type OrgStatus = 'ACTIVE' | 'DISABLED';

export interface PlatformOrg {
  id: number;
  tenantId?: number;
  parentId: number | null;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  status: OrgStatus;
  sortNo: number;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  children?: PlatformOrg[];
}

export interface OrgCreateRequest {
  parentId?: number | null;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  status?: OrgStatus;
  sortNo?: number;
}

export interface OrgUpdateRequest extends OrgCreateRequest {
  status: OrgStatus;
}

export type RoleStatus = 'ACTIVE' | 'DISABLED';

export interface PlatformRole {
  id: number;
  tenantId: number;
  roleCode: string;
  roleName: string;
  roleType: string;
  status: RoleStatus;
  version: number;
  permissions: string[];
  menuCodes: string[];
  createdAt: string;
  updatedAt: string;
}

export interface RoleCreateRequest {
  roleCode: string;
  roleName: string;
  roleType: string;
  status?: RoleStatus;
}

export interface RoleUpdateRequest {
  roleCode: string;
  roleName: string;
  roleType: string;
  status: RoleStatus;
}

export interface AssignRolePermissionsRequest {
  permissionCodes: string[];
}

export interface AssignRoleMenusRequest {
  menuCodes: string[];
}

export interface PlatformPermission {
  id?: number;
  tenantId?: number;
  code: string;
  nameKey: string;
  groupKey: string;
  permissionCode?: string;
  permissionName?: string;
  resourceType?: string;
  moduleCode?: string;
  actionCode?: string;
}

export interface PlatformMenu {
  id: number;
  tenantId: number;
  parentId: number | null;
  menuCode: string;
  menuType: string;
  titleKey: string;
  routePath: string | null;
  componentKey: string | null;
  icon: string | null;
  sortNo: number;
  visible: boolean;
  enabled: boolean;
  version: number;
  permissionCodes: string[];
  children: PlatformMenu[];
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuCreateRequest {
  parentId?: number | null;
  menuCode: string;
  menuType: string;
  titleKey: string;
  routePath?: string | null;
  componentKey?: string | null;
  icon?: string | null;
  sortNo?: number;
  visible: boolean;
  enabled: boolean;
}

export interface MenuUpdateRequest extends MenuCreateRequest {
}
