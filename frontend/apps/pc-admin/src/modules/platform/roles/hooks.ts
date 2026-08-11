import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { rolesApi } from './api';
import type { RoleCreateRequest, RoleUpdateRequest } from '@iaf/domain-types';

export const useRolesQuery = (params: { keyword?: string; pageNo: number; pageSize: number }) => {
  return useQuery({
    queryKey: ['platform-roles', params.keyword, params.pageNo, params.pageSize],
    queryFn: () => rolesApi.listRoles(params)
  });
};

export const usePermissionsQuery = () => {
  return useQuery({
    queryKey: ['platform-permissions'],
    queryFn: rolesApi.listPermissions
  });
};

export const useCreateRoleMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rolesApi.createRole,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-roles'] });
      options?.onSuccess?.();
    }
  });
};

export const useUpdateRoleMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: RoleUpdateRequest }) =>
      rolesApi.updateRole(id, values),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-roles'] });
      options?.onSuccess?.();
    }
  });
};

export const useAssignPermissionsMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: string[] }) =>
      rolesApi.assignRolePermissions(id, { permissionCodes: values }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-roles'] });
      options?.onSuccess?.();
    }
  });
};

export const useAssignMenusMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: string[] }) =>
      rolesApi.assignRoleMenus(id, { menuCodes: values }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-roles'] });
      await queryClient.invalidateQueries({ queryKey: ['platform-current-user-menus'] });
      options?.onSuccess?.();
    }
  });
};
