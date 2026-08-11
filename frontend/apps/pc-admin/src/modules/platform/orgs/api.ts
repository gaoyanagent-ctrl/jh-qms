import type { OrgCreateRequest, OrgUpdateRequest, PlatformOrg } from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const orgsApi = {
  listOrgTree: () =>
    apiClient.get<PlatformOrg[]>('/api/platform/orgs/tree'),
  createOrg: (request: OrgCreateRequest) =>
    apiClient.post<PlatformOrg>('/api/platform/orgs', request),
  updateOrg: (id: number, request: OrgUpdateRequest) =>
    apiClient.put<PlatformOrg>(`/api/platform/orgs/${id}`, request)
};
