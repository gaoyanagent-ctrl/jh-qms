import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { menusApi } from './api';
import type { MenuUpdateRequest } from '@iaf/domain-types';

export const useMenusTreeQuery = () => {
  return useQuery({
    queryKey: ['platform-menus-tree'],
    queryFn: menusApi.listMenusTree
  });
};

export const useCurrentUserMenusQuery = () => {
  return useQuery({
    queryKey: ['platform-current-user-menus'],
    queryFn: menusApi.listCurrentUserMenus
  });
};

export const useCreateMenuMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: menusApi.createMenu,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-menus-tree'] });
      await queryClient.invalidateQueries({ queryKey: ['platform-current-user-menus'] });
      options?.onSuccess?.();
    }
  });
};

export const useUpdateMenuMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: MenuUpdateRequest }) => menusApi.updateMenu(id, values),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-menus-tree'] });
      await queryClient.invalidateQueries({ queryKey: ['platform-current-user-menus'] });
      options?.onSuccess?.();
    }
  });
};

export const useUpdateMenuStructureMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (updates: Array<{ id: number; values: MenuUpdateRequest }>) =>
      Promise.all(updates.map(({ id, values }) => menusApi.updateMenu(id, values))),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-menus-tree'] });
      await queryClient.invalidateQueries({ queryKey: ['platform-current-user-menus'] });
      options?.onSuccess?.();
    }
  });
};
