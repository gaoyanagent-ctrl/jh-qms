import type { UserCreateRequest, UserUpdateRequest } from '@iaf/domain-types';

export type UserFormValues = UserCreateRequest & UserUpdateRequest;

export interface UserOrgFormValues {
  orgIds: number[];
  primaryOrgId?: number;
}
