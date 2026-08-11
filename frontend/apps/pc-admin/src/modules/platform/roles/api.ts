import type {
  AssignRoleMenusRequest,
  AssignRolePermissionsRequest,
  PageResult,
  PlatformPermission,
  PlatformRole,
  RoleCreateRequest,
  RoleUpdateRequest
} from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const rolesApi = {
  listRoles: (params: { keyword?: string; pageNo: number; pageSize: number }) =>
    apiClient.get<PageResult<PlatformRole>>('/api/platform/roles', { query: params }),
  createRole: (request: RoleCreateRequest) =>
    apiClient.post<PlatformRole>('/api/platform/roles', request),
  updateRole: (id: number, request: RoleUpdateRequest) =>
    apiClient.put<PlatformRole>(`/api/platform/roles/${id}`, request),
  listPermissions: () =>
    apiClient.get<PlatformPermission[]>('/api/platform/permissions'),
  assignRolePermissions: (id: number, request: AssignRolePermissionsRequest) =>
    apiClient.put<PlatformRole>(`/api/platform/roles/${id}/permissions`, request),
  assignRoleMenus: (id: number, request: AssignRoleMenusRequest) =>
    apiClient.put<PlatformRole>(`/api/platform/roles/${id}/menus`, request)
};
