import type {
  PageResult,
  QmsDrawing,
  QmsDrawingCreateRequest,
  QmsDrawingRevision,
  QmsDrawingRevisionCreateRequest,
  QmsDrawingParseJob,
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
  createRevision: (drawingId: number, request: QmsDrawingRevisionCreateRequest) =>
    apiClient.post<QmsDrawingRevision>(`/api/qms/drawings/${drawingId}/revisions`, request),
  listLatestParseJobs: (drawingId: number) =>
    apiClient.get<QmsDrawingParseJob[]>(`/api/qms/drawings/${drawingId}/parse-jobs`),
  retryParseJob: (revisionId: number) =>
    apiClient.post<QmsDrawingParseJob>(`/api/qms/drawing-revisions/${revisionId}/parse-job/retry`),
  uploadRevisionFile: (revisionId: number, file: File) => {
    const body = new FormData();
    body.append('file', file);
    return apiClient.post<QmsFileObject>(`/api/qms/drawing-revisions/${revisionId}/file`, body);
  }
};
