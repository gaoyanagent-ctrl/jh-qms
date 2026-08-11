import { mockSuccess, mockError, MockApiAdapter, MockRequest } from '@iaf/api-client';
import type { PlatformRole, PageResult } from '@iaf/domain-types';

const platformPermissionCodes = [
  'platform:auth:me',
  'platform:user:view',
  'platform:user:create',
  'platform:user:update',
  'platform:user:disable',
  'platform:user:reset-password',
  'platform:org:view',
  'platform:org:create',
  'platform:org:update',
  'platform:role:view',
  'platform:role:create',
  'platform:role:update',
  'platform:role:assign-permission',
  'platform:role:assign-menu',
  'platform:menu:view',
  'platform:menu:create',
  'platform:menu:update',
  'platform:menu:disable',
  'platform:permission:view',
  'platform:data-permission:view',
  'platform:data-permission:update',
  'platform:field-permission:view',
  'platform:field-permission:update',
  'platform:dictionary:view',
  'platform:dictionary:update',
  'platform:parameter:view',
  'platform:parameter:update',
  'platform:audit:view',
  'platform:theme:view',
  'platform:theme:update',
  'platform:brand:view',
  'platform:brand:update',
  'platform:i18n:view',
  'platform:i18n:update',
  'platform:preference:me'
];

let roles: PlatformRole[] = [
  {
    id: 1,
    tenantId: 1,
    roleCode: 'ADMIN',
    roleName: 'System Administrator',
    roleType: 'PLATFORM',
    status: 'ACTIVE',
    version: 1,
    permissions: [
      'platform:auth:me',
      'platform:user:view',
      'platform:user:create',
      'platform:user:update',
      'platform:user:disable',
      'platform:user:reset-password',
      'platform:org:view',
      'platform:org:create',
      'platform:org:update',
      'platform:role:view',
      'platform:role:create',
      'platform:role:update',
      'platform:role:assign-permission',
      'platform:role:assign-menu',
      'platform:menu:view',
      'platform:menu:create',
      'platform:menu:update',
      'platform:menu:disable',
      'platform:permission:view',
      'platform:data-permission:view',
      'platform:data-permission:update',
      'platform:field-permission:view',
      'platform:field-permission:update',
      'platform:dictionary:view',
      'platform:dictionary:update',
      'platform:parameter:view',
      'platform:parameter:update',
      'platform:audit:view',
      'platform:theme:view',
      'platform:theme:update',
      'platform:brand:view',
      'platform:brand:update',
      'platform:i18n:view',
      'platform:i18n:update',
      'platform:preference:me'
    ],
    menuCodes: ['platform', 'platform.users', 'platform.orgs', 'platform.roles', 'platform.menus', 'platform.dictionaries', 'platform.auditLogs', 'platform.approvalTasks', 'platform.kanban'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 2,
    tenantId: 1,
    roleCode: 'WMS_MGR',
    roleName: 'Warehouse Manager',
    roleType: 'PLATFORM',
    status: 'ACTIVE',
    version: 1,
    permissions: [
      'platform:auth:me',
      'platform:user:view',
      'platform:org:view',
      'platform:role:view'
    ],
    menuCodes: ['platform', 'platform.users', 'platform.orgs', 'platform.roles'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
];

export const registerRoleMocks = (adapter: MockApiAdapter) => {
  // GET /api/platform/roles
  adapter.register('GET', '/api/platform/roles', (req: MockRequest) => {
    const keyword = req.queryParams.keyword || '';
    const pageNo = parseInt(req.queryParams.pageNo || '1', 10);
    const pageSize = parseInt(req.queryParams.pageSize || '10', 10);

    const filtered = roles.filter(r => 
      r.roleCode.toLowerCase().includes(keyword.toLowerCase()) || 
      r.roleName.toLowerCase().includes(keyword.toLowerCase())
    );

    const start = (pageNo - 1) * pageSize;
    const records = filtered.slice(start, start + pageSize);

    const data: PageResult<PlatformRole> = {
      records,
      total: filtered.length,
      pageNo,
      pageSize
    };

    return mockSuccess(data);
  });

  // POST /api/platform/roles
  adapter.register('POST', '/api/platform/roles', (req: MockRequest) => {
    const body = req.body;
    if (roles.some(r => r.roleCode === body.roleCode)) {
      return mockError('Role code already exists', 'ROLE_ALREADY_EXISTS', 400);
    }

    const newRole: PlatformRole = {
      id: roles.length + 1,
      tenantId: 1,
      roleCode: body.roleCode,
      roleName: body.roleName,
      roleType: body.roleType || 'PLATFORM',
      status: body.status || 'ACTIVE',
      version: 1,
      permissions: [],
      menuCodes: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    roles.push(newRole);
    return mockSuccess(newRole);
  });

  adapter.register('GET', '/api/platform/permissions', () => {
    return mockSuccess(platformPermissionCodes.map((code, index) => ({
      id: index + 1,
      tenantId: 1,
      permissionCode: code,
      permissionName: code,
      resourceType: 'API',
      moduleCode: 'platform',
      actionCode: code.substring(code.lastIndexOf(':') + 1)
    })));
  });

  // PUT /api/platform/roles/:id
  adapter.register('PUT', '/api/platform/roles/:id', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = roles.findIndex(r => r.id === id);
    if (index === -1) {
      return mockError('Role not found', 'ROLE_NOT_FOUND', 404);
    }

    const body = req.body;
    roles[index] = {
      ...roles[index],
      roleCode: body.roleCode,
      roleName: body.roleName,
      roleType: body.roleType,
      status: body.status,
      updatedAt: new Date().toISOString()
    };

    return mockSuccess(roles[index]);
  });

  // PUT /api/platform/roles/:id/permissions
  adapter.register('PUT', '/api/platform/roles/:id/permissions', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = roles.findIndex(r => r.id === id);
    if (index === -1) {
      return mockError('Role not found', 'ROLE_NOT_FOUND', 404);
    }

    const body = req.body;
    roles[index] = {
      ...roles[index],
      permissions: body.permissionCodes || [],
      updatedAt: new Date().toISOString()
    };

    return mockSuccess(roles[index]);
  });

  adapter.register('PUT', '/api/platform/roles/:id/menus', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = roles.findIndex(r => r.id === id);
    if (index === -1) {
      return mockError('Role not found', 'ROLE_NOT_FOUND', 404);
    }

    const body = req.body;
    roles[index] = {
      ...roles[index],
      menuCodes: body.menuCodes || [],
      updatedAt: new Date().toISOString()
    };

    return mockSuccess(roles[index]);
  });
};
