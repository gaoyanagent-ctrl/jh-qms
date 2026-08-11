import type {
  PageResult,
  PlatformUser,
  ResetPasswordRequest,
  UserCreateRequest,
  UserOrgAssignRequest,
  UserOrgContextSwitchRequest,
  UserOrganizationsResponse,
  UserUpdateRequest
} from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const usersApi = {
  listUsers: (params: { keyword?: string; pageNo: number; pageSize: number }) =>
    apiClient.get<PageResult<PlatformUser>>('/api/platform/users', { query: params }),
  createUser: (request: UserCreateRequest) =>
    apiClient.post<PlatformUser>('/api/platform/users', request),
  updateUser: (id: number, request: UserUpdateRequest) =>
    apiClient.put<PlatformUser>(`/api/platform/users/${id}`, request),
  getUserOrganizations: (id: number) =>
    apiClient.get<UserOrganizationsResponse>(`/api/platform/users/${id}/orgs`),
  assignUserOrganizations: (id: number, request: UserOrgAssignRequest) =>
    apiClient.put<UserOrganizationsResponse>(`/api/platform/users/${id}/orgs`, request),
  switchUserOrgContext: (id: number, request: UserOrgContextSwitchRequest) =>
    apiClient.patch<PlatformUser>(`/api/platform/users/${id}/org-context`, request),
  disableUser: (id: number) =>
    apiClient.post<void>(`/api/platform/users/${id}/disable`),
  resetPassword: (id: number, request: ResetPasswordRequest) =>
    apiClient.post<void>(`/api/platform/users/${id}/reset-password`, request)
};
