import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { QmsDrawingCreateRequest, QmsDrawingRevisionCreateRequest, QmsQualityCharacteristicReviewRequest } from '@iaf/domain-types';
import { qmsEngineeringApi } from './api';

export const qmsEngineeringKeys = {
  parts: ['qms-engineering-parts'] as const,
  part: (id: number) => ['qms-engineering-part', id] as const,
  drawings: (partId: number) => ['qms-engineering-drawings', partId] as const,
  revisions: (drawingId: number) => ['qms-engineering-drawing-revisions', drawingId] as const,
  revision: (revisionId: number) => ['qms-engineering-drawing-revision', revisionId] as const,
  parseJobs: (drawingId: number) => ['qms-engineering-drawing-parse-jobs', drawingId] as const,
  revisionFile: (revisionId: number) => ['qms-engineering-revision-file', revisionId] as const,
  intermediateModel: (revisionId: number) => ['qms-engineering-intermediate-model', revisionId] as const,
  evidence: (revisionId: number) => ['qms-engineering-evidence', revisionId] as const,
  characteristics: (revisionId: number) => ['qms-engineering-characteristics', revisionId] as const
};

export const useQmsPartsQuery = (params: { keyword?: string; pageNo: number; pageSize: number }) =>
  useQuery({
    queryKey: [...qmsEngineeringKeys.parts, params.keyword ?? '', params.pageNo, params.pageSize],
    queryFn: () => qmsEngineeringApi.listParts(params)
  });

export const useQmsPartQuery = (id?: number) =>
  useQuery({ queryKey: qmsEngineeringKeys.part(id ?? 0), queryFn: () => qmsEngineeringApi.getPart(id!), enabled: Boolean(id) });

export const useQmsDrawingsQuery = (partId?: number, enabled = true) =>
  useQuery({
    queryKey: qmsEngineeringKeys.drawings(partId ?? 0),
    queryFn: () => qmsEngineeringApi.listDrawings(partId!),
    enabled: Boolean(partId) && enabled
  });

export const useQmsRevisionsQuery = (drawingId?: number, enabled = true) =>
  useQuery({
    queryKey: qmsEngineeringKeys.revisions(drawingId ?? 0),
    queryFn: () => qmsEngineeringApi.listRevisions(drawingId!),
    enabled: Boolean(drawingId) && enabled
  });

export const useQmsRevisionQuery = (revisionId?: number, enabled = true) =>
  useQuery({ queryKey: qmsEngineeringKeys.revision(revisionId ?? 0),
    queryFn: () => qmsEngineeringApi.getRevision(revisionId!), enabled: Boolean(revisionId) && enabled });

export const useQmsParseJobsQuery = (drawingId?: number, enabled = true) =>
  useQuery({
    queryKey: qmsEngineeringKeys.parseJobs(drawingId ?? 0),
    queryFn: () => qmsEngineeringApi.listLatestParseJobs(drawingId!),
    enabled: Boolean(drawingId) && enabled
  });

export const useQmsRevisionFileQuery = (revisionId?: number, enabled = true) =>
  useQuery({ queryKey: qmsEngineeringKeys.revisionFile(revisionId ?? 0),
    queryFn: () => qmsEngineeringApi.getRevisionFileContent(revisionId!), enabled: Boolean(revisionId) && enabled });

export const useQmsIntermediateModelQuery = (revisionId?: number, enabled = true) =>
  useQuery({ queryKey: qmsEngineeringKeys.intermediateModel(revisionId ?? 0),
    queryFn: () => qmsEngineeringApi.getIntermediateModel(revisionId!), enabled: Boolean(revisionId) && enabled, retry: false });

export const useQmsEvidenceQuery = (revisionId?: number, enabled = true) =>
  useQuery({ queryKey: qmsEngineeringKeys.evidence(revisionId ?? 0),
    queryFn: () => qmsEngineeringApi.listEvidence(revisionId!), enabled: Boolean(revisionId) && enabled });

export const useQmsCharacteristicsQuery = (revisionId?: number, enabled = true) =>
  useQuery({ queryKey: qmsEngineeringKeys.characteristics(revisionId ?? 0),
    queryFn: () => qmsEngineeringApi.listCharacteristics(revisionId!), enabled: Boolean(revisionId) && enabled });

export const useReviewQmsCharacteristicMutation = (revisionId: number, decision: 'confirm' | 'reject', onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: QmsQualityCharacteristicReviewRequest }) =>
      decision === 'confirm' ? qmsEngineeringApi.confirmCharacteristic(revisionId, id, request)
        : qmsEngineeringApi.rejectCharacteristic(revisionId, id, request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.characteristics(revisionId) });
      onSuccess?.();
    }
  });
};

export const useCreateQmsPartMutation = (onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: qmsEngineeringApi.createPart,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.parts });
      onSuccess?.();
    }
  });
};

export const useCreateQmsDrawingMutation = (partId: number, onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: QmsDrawingCreateRequest) => qmsEngineeringApi.createDrawing(partId, request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.drawings(partId) });
      onSuccess?.();
    }
  });
};

export const useCreateQmsRevisionMutation = (drawingId?: number, onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: QmsDrawingRevisionCreateRequest) => qmsEngineeringApi.createRevision(drawingId!, request),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.revisions(drawingId ?? 0) }),
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.parseJobs(drawingId ?? 0) })
      ]);
      onSuccess?.();
    }
  });
};

export const useRetryQmsParseJobMutation = (drawingId?: number, onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (revisionId: number) => qmsEngineeringApi.retryParseJob(revisionId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.revisions(drawingId ?? 0) }),
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.parseJobs(drawingId ?? 0) })
      ]);
      onSuccess?.();
    }
  });
};

export const useUploadQmsRevisionFileMutation = (drawingId?: number, onSuccess?: () => void) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ revisionId, file }: { revisionId: number; file: File }) => qmsEngineeringApi.uploadRevisionFile(revisionId, file),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.revisions(drawingId ?? 0) }),
        queryClient.invalidateQueries({ queryKey: qmsEngineeringKeys.parseJobs(drawingId ?? 0) })
      ]);
      onSuccess?.();
    }
  });
};
