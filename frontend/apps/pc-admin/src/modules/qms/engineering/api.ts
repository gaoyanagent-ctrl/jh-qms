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
  QmsPart,
  QmsPartCreateRequest
} from '@iaf/domain-types';
import type { QmsFileObject } from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const qmsEngineeringApi = {
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
  confirmCharacteristic: (revisionId: number, id: number, request: QmsQualityCharacteristicReviewRequest) =>
    apiClient.post<QmsQualityCharacteristic>(`/api/qms/drawing-revisions/${revisionId}/characteristics/${id}/confirm`, request),
  rejectCharacteristic: (revisionId: number, id: number, request: QmsQualityCharacteristicReviewRequest) =>
    apiClient.post<QmsQualityCharacteristic>(`/api/qms/drawing-revisions/${revisionId}/characteristics/${id}/reject`, request),
  getRevisionFileContent: (revisionId: number) =>
    apiClient.getBlob(`/api/qms/drawing-revisions/${revisionId}/file/content`),
  uploadRevisionFile: (revisionId: number, file: File) => {
    const body = new FormData();
    body.append('file', file);
    return apiClient.post<QmsFileObject>(`/api/qms/drawing-revisions/${revisionId}/file`, body);
  }
};
