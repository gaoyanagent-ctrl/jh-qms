export interface Result<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNo: number;
  pageSize: number;
}

export interface LoginRequest {
  tenantCode: string;
  username: string;
  password: string;
}

export interface AuthPrincipal {
  tenantId: number;
  userId: number;
  username: string;
  displayName: string;
  currentOrgId?: number | null;
  organizations?: PlatformUserOrg[];
  permissions: string[];
}

export interface LoginResponse extends AuthPrincipal {
  tokenType: 'Bearer' | string;
  accessToken: string;
  expiresAt: string;
}

export type UserStatus = 'ENABLED' | 'DISABLED';

export interface PlatformUser {
  id: number;
  tenantId: number;
  username: string;
  displayName: string;
  mobile: string | null;
  email: string | null;
  status: UserStatus;
  primaryOrgId: number | null;
  organizations?: PlatformUserOrg[];
  createdAt: string;
  updatedAt: string;
}

export interface PlatformUserOrg {
  id: number;
  orgId: number;
  orgCode: string;
  orgName: string;
  orgType: string;
  primary: boolean;
  scopeWeight: number;
  validFrom?: string | null;
  validTo?: string | null;
}

export interface UserOrganizationsResponse {
  userId: number;
  primaryOrgId: number | null;
  organizations: PlatformUserOrg[];
}

export interface UserOrgAssignRequest {
  organizations: Array<{
    orgId: number;
    primary: boolean;
    scopeWeight?: number;
    validFrom?: string | null;
    validTo?: string | null;
  }>;
}

export interface UserOrgContextSwitchRequest {
  orgId: number;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  displayName: string;
  mobile?: string | null;
  email?: string | null;
}

export interface UserUpdateRequest {
  displayName: string;
  mobile?: string | null;
  email?: string | null;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export type OrgType = 'COMPANY' | 'DEPARTMENT' | 'TEAM';
export type OrgStatus = 'ACTIVE' | 'DISABLED';

export interface PlatformOrg {
  id: number;
  tenantId?: number;
  parentId: number | null;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  status: OrgStatus;
  sortNo: number;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  children?: PlatformOrg[];
}

export interface OrgCreateRequest {
  parentId?: number | null;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  status?: OrgStatus;
  sortNo?: number;
}

export interface OrgUpdateRequest extends OrgCreateRequest {
  status: OrgStatus;
}

export type RoleStatus = 'ACTIVE' | 'DISABLED';

export interface PlatformRole {
  id: number;
  tenantId: number;
  roleCode: string;
  roleName: string;
  roleType: string;
  status: RoleStatus;
  version: number;
  permissions: string[];
  menuCodes: string[];
  createdAt: string;
  updatedAt: string;
}

export interface RoleCreateRequest {
  roleCode: string;
  roleName: string;
  roleType: string;
  status?: RoleStatus;
}

export interface RoleUpdateRequest {
  roleCode: string;
  roleName: string;
  roleType: string;
  status: RoleStatus;
}

export interface AssignRolePermissionsRequest {
  permissionCodes: string[];
}

export interface AssignRoleMenusRequest {
  menuCodes: string[];
}

export interface PlatformPermission {
  id?: number;
  tenantId?: number;
  code: string;
  nameKey: string;
  groupKey: string;
  permissionCode?: string;
  permissionName?: string;
  resourceType?: string;
  moduleCode?: string;
  actionCode?: string;
}

export interface PlatformMenu {
  id: number;
  tenantId: number;
  parentId: number | null;
  menuCode: string;
  menuType: string;
  titleKey: string;
  routePath: string | null;
  componentKey: string | null;
  icon: string | null;
  sortNo: number;
  visible: boolean;
  enabled: boolean;
  version: number;
  permissionCodes: string[];
  children: PlatformMenu[];
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuCreateRequest {
  parentId?: number | null;
  menuCode: string;
  menuType: string;
  titleKey: string;
  routePath?: string | null;
  componentKey?: string | null;
  icon?: string | null;
  sortNo?: number;
  visible: boolean;
  enabled: boolean;
}

export interface MenuUpdateRequest extends MenuCreateRequest {
}
