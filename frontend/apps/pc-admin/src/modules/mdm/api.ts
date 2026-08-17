import { apiClient } from '../../api/client';
import type { CreateMdmModel, MdmModel, MdmModelValidation, MdmPage, MdmRecord, SaveMdmModelDraft, SaveMdmRecord } from './types';
export const mdmApi = {
  models: () => apiClient.get<MdmModel[]>('/api/mdm/models'),
  schema: (code:string) => apiClient.get<MdmModel>(`/api/mdm/models/${code}/schema`),
  createModel: (request:CreateMdmModel) => apiClient.post<MdmModel>('/api/mdm/models',request),
  saveDraft: (code:string,request:SaveMdmModelDraft) => apiClient.put<MdmModel>(`/api/mdm/models/${code}/draft`,request),
  validateModel: (code:string) => apiClient.post<MdmModelValidation>(`/api/mdm/models/${code}/validate`,{}),
  publishModel: (code:string) => apiClient.post<MdmModel>(`/api/mdm/models/${code}/publish`,{}),
  records: (code:string, params:{keyword?:string;pageNo:number;pageSize:number}) => apiClient.get<MdmPage<MdmRecord>>(`/api/mdm/models/${code}/records`,{query:params}),
  create: (code:string, request:SaveMdmRecord) => apiClient.post<MdmRecord>(`/api/mdm/models/${code}/records`,request),
  update: (code:string,id:string,request:SaveMdmRecord) => apiClient.put<MdmRecord>(`/api/mdm/models/${code}/records/${id}`,request)
};
