import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usersApi } from './api';
import type { UserCreateRequest, UserOrgAssignRequest, UserRoleAssignRequest, UserUpdateRequest } from '@iaf/domain-types';

export const useUsersQuery = (params: { keyword?: string; pageNo: number; pageSize: number }) => {
  return useQuery({
    queryKey: ['platform-users', params.keyword, params.pageNo, params.pageSize],
    queryFn: () => usersApi.listUsers(params)
  });
};

export const useCreateUserMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: usersApi.createUser,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      options?.onSuccess?.();
    }
  });
};

export const useUpdateUserMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: UserUpdateRequest }) =>
      usersApi.updateUser(id, values),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      options?.onSuccess?.();
    }
  });
};

export const useUserOrganizationsQuery = (userId?: number) => {
  return useQuery({
    queryKey: ['platform-user-orgs', userId],
    queryFn: () => usersApi.getUserOrganizations(userId!),
    enabled: Boolean(userId)
  });
};

export const useAssignUserOrganizationsMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: UserOrgAssignRequest }) =>
      usersApi.assignUserOrganizations(id, values),
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['platform-users'] }),
        queryClient.invalidateQueries({ queryKey: ['platform-user-orgs', variables.id] })
      ]);
      options?.onSuccess?.();
    }
  });
};

export const useUserRolesQuery = (userId?: number) => useQuery({
  queryKey: ['platform-user-roles', userId],
  queryFn: () => usersApi.getUserRoles(userId!),
  enabled: Boolean(userId)
});

export const useAssignUserRolesMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: UserRoleAssignRequest }) => usersApi.assignUserRoles(id, values),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({ queryKey: ['platform-user-roles', variables.id] });
      options?.onSuccess?.();
    }
  });
};

export const useDisableUserMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: usersApi.disableUser,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      options?.onSuccess?.();
    }
  });
};

export const useResetPasswordMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, newPassword }: { id: number; newPassword: string }) =>
      usersApi.resetPassword(id, { newPassword }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      options?.onSuccess?.();
    }
  });
};
