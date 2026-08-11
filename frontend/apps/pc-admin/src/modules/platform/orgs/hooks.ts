import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { orgsApi } from './api';
import type { OrgCreateRequest, OrgUpdateRequest } from '@iaf/domain-types';

export const useOrgTreeQuery = () => {
  return useQuery({
    queryKey: ['platform-org-tree'],
    queryFn: orgsApi.listOrgTree
  });
};

export const useCreateOrgMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: orgsApi.createOrg,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-org-tree'] });
      options?.onSuccess?.();
    }
  });
};

export const useUpdateOrgMutation = (options?: { onSuccess?: () => void }) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, values }: { id: number; values: OrgUpdateRequest }) =>
      orgsApi.updateOrg(id, values),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['platform-org-tree'] });
      options?.onSuccess?.();
    }
  });
};
