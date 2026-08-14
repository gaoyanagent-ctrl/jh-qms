import type {
  PageResult,
  QmsDrawing,
  QmsDrawingCreateRequest,
  QmsDrawingRevision,
  QmsDrawingRevisionCreateRequest,
  QmsDrawingParseJob,
  QmsDrawingIntermediateModel,
  QmsSourceEvidence,
  QmsQualityCharacteristic,
  QmsQualityCharacteristicReviewRequest,
  QmsQualityCharacteristicCreateRequest,
  QmsQualityCharacteristicBulkReviewRequest,
  QmsPart,
  QmsPartCreateRequest,
  QmsDrawingRevisionFile,
  QmsDrawingRevisionFileRole
} from '@iaf/domain-types';
import type { QmsDrawingLegendRule, QmsDrawingLegendRuleUpdateRequest } from '@iaf/domain-types';
import type { QmsInspectionStandard } from '@iaf/domain-types';
import type { QmsValidationPlan } from '@iaf/domain-types';
import type { QmsFileObject } from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const qmsEngineeringApi = {
  listDrawingLegendRules: () => apiClient.get<QmsDrawingLegendRule[]>('/api/qms/drawing-legend-rules'),
  updateDrawingLegendRules: (request: QmsDrawingLegendRuleUpdateRequest) =>
    apiClient.put<QmsDrawingLegendRule[]>('/api/qms/drawing-legend-rules', request),
  getInspectionStandard:(revisionId:number)=>apiClient.get<QmsInspectionStandard|null>(`/api/qms/drawing-revisions/${revisionId}/inspection-standard`),
  generateInspectionStandard:(revisionId:number)=>apiClient.post<QmsInspectionStandard>(`/api/qms/drawing-revisions/${revisionId}/inspection-standard/generate`),
  updateInspectionStandard:(revisionId:number,id:number,request:unknown)=>apiClient.put<QmsInspectionStandard>(`/api/qms/drawing-revisions/${revisionId}/inspection-standard/${id}`,request),
  actOnInspectionStandard:(revisionId:number,id:number,action:'submit-approval'|'approve'|'reject'|'release',comment?:string)=>apiClient.post<QmsInspectionStandard>(`/api/qms/drawing-revisions/${revisionId}/inspection-standard/${id}/${action}`,{comment}),
  getValidationPlan:(standardId:number)=>apiClient.get<QmsValidationPlan|null>(`/api/qms/inspection-standards/${standardId}/validation-plan`),
  generateValidationPlan:(standardId:number)=>apiClient.post<QmsValidationPlan>(`/api/qms/inspection-standards/${standardId}/validation-plan/generate`),
  updateValidationPlan:(standardId:number,id:number,request:unknown)=>apiClient.put<QmsValidationPlan>(`/api/qms/inspection-standards/${standardId}/validation-plan/${id}`,request),
  actOnValidationPlan:(standardId:number,id:number,action:'submit-approval'|'approve'|'reject'|'release',comment?:string)=>apiClient.post<QmsValidationPlan>(`/api/qms/inspection-standards/${standardId}/validation-plan/${id}/${action}`,{comment}),
  listParts: (params: { keyword?: string; pageNo: number; pageSize: number }) =>
    apiClient.get<PageResult<QmsPart>>('/api/qms/parts', { query: params }),
  getPart: (id: number) => apiClient.get<QmsPart>(`/api/qms/parts/${id}`),
  createPart: (request: QmsPartCreateRequest) => apiClient.post<QmsPart>('/api/qms/parts', request),
  listDrawings: (partId: number) => apiClient.get<QmsDrawing[]>(`/api/qms/parts/${partId}/drawings`),
  createDrawing: (partId: number, request: QmsDrawingCreateRequest) =>
    apiClient.post<QmsDrawing>(`/api/qms/parts/${partId}/drawings`, request),
  listRevisions: (drawingId: number) =>
    apiClient.get<QmsDrawingRevision[]>(`/api/qms/drawings/${drawingId}/revisions`),
  getRevision: (revisionId: number) =>
    apiClient.get<QmsDrawingRevision>(`/api/qms/drawing-revisions/${revisionId}`),
  createRevision: (drawingId: number, request: QmsDrawingRevisionCreateRequest) =>
    apiClient.post<QmsDrawingRevision>(`/api/qms/drawings/${drawingId}/revisions`, request),
  listLatestParseJobs: (drawingId: number) =>
    apiClient.get<QmsDrawingParseJob[]>(`/api/qms/drawings/${drawingId}/parse-jobs`),
  retryParseJob: (revisionId: number) =>
    apiClient.post<QmsDrawingParseJob>(`/api/qms/drawing-revisions/${revisionId}/parse-job/retry`),
  getIntermediateModel: (revisionId: number) =>
    apiClient.get<QmsDrawingIntermediateModel>(`/api/qms/drawing-revisions/${revisionId}/intermediate-model`),
  listEvidence: (revisionId: number) =>
    apiClient.get<QmsSourceEvidence[]>(`/api/qms/drawing-revisions/${revisionId}/evidence`),
  listCharacteristics: (revisionId: number) =>
    apiClient.get<QmsQualityCharacteristic[]>(`/api/qms/drawing-revisions/${revisionId}/characteristics`),
  createCharacteristic: (revisionId: number, request: QmsQualityCharacteristicCreateRequest) =>
    apiClient.post<QmsQualityCharacteristic>(`/api/qms/drawing-revisions/${revisionId}/characteristics`, request),
  bulkReviewCharacteristics: (revisionId: number, request: QmsQualityCharacteristicBulkReviewRequest) =>
    apiClient.post<QmsQualityCharacteristic[]>(`/api/qms/drawing-revisions/${revisionId}/characteristics/bulk-review`, request),
  confirmCharacteristic: (revisionId: number, id: number, request: QmsQualityCharacteristicReviewRequest) =>
    apiClient.post<QmsQualityCharacteristic>(`/api/qms/drawing-revisions/${revisionId}/characteristics/${id}/confirm`, request),
  rejectCharacteristic: (revisionId: number, id: number, request: QmsQualityCharacteristicReviewRequest) =>
    apiClient.post<QmsQualityCharacteristic>(`/api/qms/drawing-revisions/${revisionId}/characteristics/${id}/reject`, request),
  getRevisionFileContent: (revisionId: number) =>
    apiClient.getBlob(`/api/qms/drawing-revisions/${revisionId}/file/content`),
  listRevisionFiles: (revisionId: number) =>
    apiClient.get<QmsDrawingRevisionFile[]>(`/api/qms/drawing-revisions/${revisionId}/files`),
  getRevisionRoleFileContent: (revisionId: number, role: QmsDrawingRevisionFileRole) =>
    apiClient.getBlob(`/api/qms/drawing-revisions/${revisionId}/files/${role}/content`),
  uploadRevisionRoleFile: (revisionId: number, role: QmsDrawingRevisionFileRole, file: File) => {
    const body = new FormData(); body.append('file', file);
    return apiClient.post<QmsFileObject>(`/api/qms/drawing-revisions/${revisionId}/files/${role}`, body);
  },
  uploadRevisionFile: (revisionId: number, file: File) => {
    const body = new FormData();
    body.append('file', file);
    return apiClient.post<QmsFileObject>(`/api/qms/drawing-revisions/${revisionId}/file`, body);
  }
};
