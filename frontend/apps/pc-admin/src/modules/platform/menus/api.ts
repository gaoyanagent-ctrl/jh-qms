import type { MenuCreateRequest, MenuUpdateRequest, PlatformMenu } from '@iaf/domain-types';
import { apiClient } from '../../../api/client';

export const menusApi = {
  listMenusTree: () => apiClient.get<PlatformMenu[]>('/api/platform/menus/tree'),
  listCurrentUserMenus: () => apiClient.get<PlatformMenu[]>('/api/platform/auth/menus'),
  createMenu: (request: MenuCreateRequest) => apiClient.post<PlatformMenu>('/api/platform/menus', request),
  updateMenu: (id: number, request: MenuUpdateRequest) => apiClient.put<PlatformMenu>(`/api/platform/menus/${id}`, request)
};
