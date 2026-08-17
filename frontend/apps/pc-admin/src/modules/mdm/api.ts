import { apiClient } from '../../api/client';
import type { CreateMdmModel, MdmBatchValidation, MdmImportPreview, MdmModel, MdmModelValidation, MdmPage, MdmRecord, MdmRecordVersion, SaveMdmModelDraft, SaveMdmRecord } from './types';
export const mdmApi = {
  models: () => apiClient.get<MdmModel[]>('/api/mdm/models'),
  schema: (code:string) => apiClient.get<MdmModel>(`/api/mdm/models/${code}/schema`),
  createModel: (request:CreateMdmModel) => apiClient.post<MdmModel>('/api/mdm/models',request),
  saveDraft: (code:string,request:SaveMdmModelDraft) => apiClient.put<MdmModel>(`/api/mdm/models/${code}/draft`,request),
  validateModel: (code:string) => apiClient.post<MdmModelValidation>(`/api/mdm/models/${code}/validate`,{}),
  publishModel: (code:string) => apiClient.post<MdmModel>(`/api/mdm/models/${code}/publish`,{}),
  records: (code:string, params:{keyword?:string;pageNo:number;pageSize:number}) => apiClient.get<MdmPage<MdmRecord>>(`/api/mdm/models/${code}/records`,{query:params}),
  recordVersions: (code:string,id:string) => apiClient.get<MdmRecordVersion[]>(`/api/mdm/models/${code}/records/${id}/versions`),
  create: (code:string, request:SaveMdmRecord) => apiClient.post<MdmRecord>(`/api/mdm/models/${code}/records`,request),
  validateBatch: (code:string,records:SaveMdmRecord[]) => apiClient.post<MdmBatchValidation>(`/api/mdm/models/${code}/records/batch-validate`,{records}),
  createBatch: (code:string,records:SaveMdmRecord[]) => apiClient.post<MdmRecord[]>(`/api/mdm/models/${code}/records/batch`,{records}),
  importTemplate: (code:string) => apiClient.getBlob(`/api/mdm/models/${code}/import-template`),
  previewImport: (code:string,file:File) => { const body=new FormData(); body.append('file',file); return apiClient.post<MdmImportPreview>(`/api/mdm/models/${code}/imports`,body); },
  update: (code:string,id:string,request:SaveMdmRecord) => apiClient.put<MdmRecord>(`/api/mdm/models/${code}/records/${id}`,request)
};
