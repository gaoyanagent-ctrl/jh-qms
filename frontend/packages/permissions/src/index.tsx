import { useAuthStore } from '@iaf/auth';
import type { PlatformPermission } from '@iaf/domain-types';
import { Button } from 'antd';
import type { ButtonProps } from 'antd';
import type { ReactNode } from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export const PLATFORM_PERMISSIONS = {
  authMe: 'platform:auth:me',
  userView: 'platform:user:view',
  userCreate: 'platform:user:create',
  userUpdate: 'platform:user:update',
  userDisable: 'platform:user:disable',
  userResetPassword: 'platform:user:reset-password',
  orgView: 'platform:org:view',
  orgCreate: 'platform:org:create',
  orgUpdate: 'platform:org:update',
  roleView: 'platform:role:view',
  roleCreate: 'platform:role:create',
  roleUpdate: 'platform:role:update',
  roleAssignPermission: 'platform:role:assign-permission',
  roleAssignMenu: 'platform:role:assign-menu',
  menuView: 'platform:menu:view',
  menuCreate: 'platform:menu:create',
  menuUpdate: 'platform:menu:update',
  menuDisable: 'platform:menu:disable',
  permissionView: 'platform:permission:view',
  dataPermissionView: 'platform:data-permission:view',
  dataPermissionUpdate: 'platform:data-permission:update',
  fieldPermissionView: 'platform:field-permission:view',
  fieldPermissionUpdate: 'platform:field-permission:update',
  dictionaryView: 'platform:dictionary:view',
  dictionaryUpdate: 'platform:dictionary:update',
  parameterView: 'platform:parameter:view',
  parameterUpdate: 'platform:parameter:update',
  auditView: 'platform:audit:view',
  themeView: 'platform:theme:view',
  themeUpdate: 'platform:theme:update',
  brandView: 'platform:brand:view',
  brandUpdate: 'platform:brand:update',
  i18nView: 'platform:i18n:view',
  i18nUpdate: 'platform:i18n:update',
  preferenceMe: 'platform:preference:me'
} as const;

export const QMS_PERMISSIONS = {
  partView: 'qms:part:view',
  partCreate: 'qms:part:create',
  drawingView: 'qms:drawing:view',
  drawingCreate: 'qms:drawing:create',
  drawingRevisionView: 'qms:drawing-revision:view',
  drawingRevisionCreate: 'qms:drawing-revision:create'
} as const;

export type QmsPermissionCode = (typeof QMS_PERMISSIONS)[keyof typeof QMS_PERMISSIONS];

export type PlatformPermissionCode = (typeof PLATFORM_PERMISSIONS)[keyof typeof PLATFORM_PERMISSIONS];

export const PLATFORM_PERMISSION_OPTIONS: PlatformPermission[] = [
  { code: PLATFORM_PERMISSIONS.authMe, nameKey: 'permissions.platform.authMe', groupKey: 'permissions.groups.auth' },
  { code: PLATFORM_PERMISSIONS.userView, nameKey: 'permissions.platform.userView', groupKey: 'permissions.groups.user' },
  { code: PLATFORM_PERMISSIONS.userCreate, nameKey: 'permissions.platform.userCreate', groupKey: 'permissions.groups.user' },
  { code: PLATFORM_PERMISSIONS.userUpdate, nameKey: 'permissions.platform.userUpdate', groupKey: 'permissions.groups.user' },
  { code: PLATFORM_PERMISSIONS.userDisable, nameKey: 'permissions.platform.userDisable', groupKey: 'permissions.groups.user' },
  { code: PLATFORM_PERMISSIONS.userResetPassword, nameKey: 'permissions.platform.userResetPassword', groupKey: 'permissions.groups.user' },
  { code: PLATFORM_PERMISSIONS.orgView, nameKey: 'permissions.platform.orgView', groupKey: 'permissions.groups.org' },
  { code: PLATFORM_PERMISSIONS.orgCreate, nameKey: 'permissions.platform.orgCreate', groupKey: 'permissions.groups.org' },
  { code: PLATFORM_PERMISSIONS.orgUpdate, nameKey: 'permissions.platform.orgUpdate', groupKey: 'permissions.groups.org' },
  { code: PLATFORM_PERMISSIONS.roleView, nameKey: 'permissions.platform.roleView', groupKey: 'permissions.groups.role' },
  { code: PLATFORM_PERMISSIONS.roleCreate, nameKey: 'permissions.platform.roleCreate', groupKey: 'permissions.groups.role' },
  { code: PLATFORM_PERMISSIONS.roleUpdate, nameKey: 'permissions.platform.roleUpdate', groupKey: 'permissions.groups.role' },
  { code: PLATFORM_PERMISSIONS.roleAssignPermission, nameKey: 'permissions.platform.roleAssignPermission', groupKey: 'permissions.groups.role' },
  { code: PLATFORM_PERMISSIONS.roleAssignMenu, nameKey: 'permissions.platform.roleAssignMenu', groupKey: 'permissions.groups.role' },
  { code: PLATFORM_PERMISSIONS.menuView, nameKey: 'permissions.platform.menuView', groupKey: 'permissions.groups.menu' },
  { code: PLATFORM_PERMISSIONS.menuCreate, nameKey: 'permissions.platform.menuCreate', groupKey: 'permissions.groups.menu' },
  { code: PLATFORM_PERMISSIONS.menuUpdate, nameKey: 'permissions.platform.menuUpdate', groupKey: 'permissions.groups.menu' },
  { code: PLATFORM_PERMISSIONS.menuDisable, nameKey: 'permissions.platform.menuDisable', groupKey: 'permissions.groups.menu' },
  { code: PLATFORM_PERMISSIONS.permissionView, nameKey: 'permissions.platform.permissionView', groupKey: 'permissions.groups.permission' },
  { code: PLATFORM_PERMISSIONS.dataPermissionView, nameKey: 'permissions.platform.dataPermissionView', groupKey: 'permissions.groups.permission' },
  { code: PLATFORM_PERMISSIONS.dataPermissionUpdate, nameKey: 'permissions.platform.dataPermissionUpdate', groupKey: 'permissions.groups.permission' },
  { code: PLATFORM_PERMISSIONS.fieldPermissionView, nameKey: 'permissions.platform.fieldPermissionView', groupKey: 'permissions.groups.permission' },
  { code: PLATFORM_PERMISSIONS.fieldPermissionUpdate, nameKey: 'permissions.platform.fieldPermissionUpdate', groupKey: 'permissions.groups.permission' },
  { code: PLATFORM_PERMISSIONS.dictionaryView, nameKey: 'permissions.platform.dictionaryView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.dictionaryUpdate, nameKey: 'permissions.platform.dictionaryUpdate', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.parameterView, nameKey: 'permissions.platform.parameterView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.parameterUpdate, nameKey: 'permissions.platform.parameterUpdate', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.auditView, nameKey: 'permissions.platform.auditView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.themeView, nameKey: 'permissions.platform.themeView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.themeUpdate, nameKey: 'permissions.platform.themeUpdate', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.brandView, nameKey: 'permissions.platform.brandView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.brandUpdate, nameKey: 'permissions.platform.brandUpdate', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.i18nView, nameKey: 'permissions.platform.i18nView', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.i18nUpdate, nameKey: 'permissions.platform.i18nUpdate', groupKey: 'permissions.groups.platformConfig' },
  { code: PLATFORM_PERMISSIONS.preferenceMe, nameKey: 'permissions.platform.preferenceMe', groupKey: 'permissions.groups.platformConfig' }
];

export const hasPermission = (permissions: readonly string[], permissionCode: string) =>
  permissions.includes(permissionCode);

export const hasAnyPermission = (permissions: readonly string[], permissionCodes: readonly string[]) =>
  permissionCodes.some((permissionCode) => hasPermission(permissions, permissionCode));

const EMPTY_PERMISSIONS: string[] = [];

export const useUserPermissions = () => {
  return useAuthStore((state) => state.principal?.permissions ?? EMPTY_PERMISSIONS);
};

export const useHasPermission = (permissionCode: string) => {
  const permissions = useUserPermissions();
  return hasPermission(permissions, permissionCode);
};

export const useHasAnyPermission = (permissionCodes: readonly string[]) => {
  const permissions = useUserPermissions();
  return hasAnyPermission(permissions, permissionCodes);
};

export interface PermissionGateProps {
  permissions?: readonly string[];
  require: string | string[];
  fallback?: ReactNode;
  children: ReactNode;
}

export const PermissionGate = ({ permissions, require, fallback = null, children }: PermissionGateProps) => {
  const userPermissions = useUserPermissions();
  const permissionsToCheck = permissions ?? userPermissions;
  const required = Array.isArray(require) ? require : [require];
  return hasAnyPermission(permissionsToCheck, required) ? children : fallback;
};

export interface PermissionButtonProps extends ButtonProps {
  require: string | string[];
}

export const PermissionButton = ({ require, children, ...props }: PermissionButtonProps) => {
  return (
    <PermissionGate require={require}>
      <Button {...props}>{children}</Button>
    </PermissionGate>
  );
};

export interface PermissionRouteProps {
  require: string | string[];
  fallbackPath?: string;
}

export const PermissionRoute = ({ require, fallbackPath = '/403' }: PermissionRouteProps) => {
  const permissions = useUserPermissions();
  const required = Array.isArray(require) ? require : [require];
  const hasAuth = hasAnyPermission(permissions, required);

  return hasAuth ? <Outlet /> : <Navigate to={fallbackPath} replace />;
};
