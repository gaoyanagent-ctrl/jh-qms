import { mockSuccess, mockError, MockApiAdapter, MockRequest } from '@iaf/api-client';
import type { PlatformUser, PageResult, PlatformUserOrg, UserOrganizationsResponse } from '@iaf/domain-types';

const orgDirectory = new Map<number, Pick<PlatformUserOrg, 'orgId' | 'orgCode' | 'orgName' | 'orgType'>>([
  [1, { orgId: 1, orgCode: 'CORP', orgName: 'IAF Industrial Corp', orgType: 'COMPANY' }],
  [2, { orgId: 2, orgCode: 'WMS_DEPT', orgName: 'Warehouse Department', orgType: 'DEPARTMENT' }],
  [3, { orgId: 3, orgCode: 'PROD_DEPT', orgName: 'Production Department', orgType: 'DEPARTMENT' }]
]);

let nextUserOrgId = 1;

const makeUserOrg = (orgId: number, primary: boolean): PlatformUserOrg => {
  const org = orgDirectory.get(orgId);
  if (!org) {
    throw new Error(`Unknown mock org ${orgId}`);
  }
  return {
    id: nextUserOrgId++,
    ...org,
    primary,
    scopeWeight: primary ? 100 : 0,
    validFrom: null,
    validTo: null
  };
};

let users: PlatformUser[] = [
  {
    id: 1,
    tenantId: 1,
    username: 'admin',
    displayName: 'System Admin',
    mobile: '13800000000',
    email: 'admin@iaf.com',
    status: 'ENABLED',
    primaryOrgId: 1,
    organizations: [makeUserOrg(1, true), makeUserOrg(2, false)],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 2,
    tenantId: 1,
    username: 'operator',
    displayName: 'WMS Operator',
    mobile: '13800000001',
    email: 'operator@iaf.com',
    status: 'ENABLED',
    primaryOrgId: 2,
    organizations: [makeUserOrg(2, true)],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
];

const toOrganizationsResponse = (user: PlatformUser): UserOrganizationsResponse => ({
  userId: user.id,
  primaryOrgId: user.primaryOrgId,
  organizations: user.organizations ?? []
});

export const registerUserMocks = (adapter: MockApiAdapter) => {
  // GET /api/platform/users
  adapter.register('GET', '/api/platform/users', (req: MockRequest) => {
    const keyword = req.queryParams.keyword || '';
    const pageNo = parseInt(req.queryParams.pageNo || '1', 10);
    const pageSize = parseInt(req.queryParams.pageSize || '10', 10);

    const filtered = users.filter(u => 
      u.username.toLowerCase().includes(keyword.toLowerCase()) || 
      u.displayName.toLowerCase().includes(keyword.toLowerCase()) || 
      (u.mobile && u.mobile.includes(keyword)) ||
      (u.email && u.email.toLowerCase().includes(keyword.toLowerCase()))
    );

    const start = (pageNo - 1) * pageSize;
    const records = filtered.slice(start, start + pageSize);

    const data: PageResult<PlatformUser> = {
      records,
      total: filtered.length,
      pageNo,
      pageSize
    };

    return mockSuccess(data);
  });

  // POST /api/platform/users
  adapter.register('POST', '/api/platform/users', (req: MockRequest) => {
    const body = req.body;
    if (!body.username || !body.displayName) {
      return mockError('Username and Display Name are required', 'BAD_REQUEST', 400);
    }
    if (users.some(u => u.username === body.username)) {
      return mockError('Username already exists', 'USER_ALREADY_EXISTS', 400);
    }

    const newUser: PlatformUser = {
      id: users.length + 1,
      tenantId: 1,
      username: body.username,
      displayName: body.displayName,
      mobile: body.mobile || null,
      email: body.email || null,
      status: body.status || 'ENABLED',
      primaryOrgId: null,
      organizations: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    users.push(newUser);
    return mockSuccess(newUser);
  });

  // PUT /api/platform/users/:id
  adapter.register('PUT', '/api/platform/users/:id', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }

    const body = req.body;
    users[index] = {
      ...users[index],
      displayName: body.displayName,
      mobile: body.mobile !== undefined ? body.mobile : users[index].mobile,
      email: body.email !== undefined ? body.email : users[index].email,
      updatedAt: new Date().toISOString()
    };

    return mockSuccess(users[index]);
  });

  adapter.register('GET', '/api/platform/users/:id/orgs', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const user = users.find(u => u.id === id);
    if (!user) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }
    return mockSuccess(toOrganizationsResponse(user));
  });

  adapter.register('PUT', '/api/platform/users/:id/orgs', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }
    const items: Array<{ orgId: number; primary?: boolean; scopeWeight?: number }> = Array.isArray(req.body?.organizations)
      ? req.body.organizations
      : [];
    const primaryCount = items.filter((item: { primary?: boolean }) => item.primary).length;
    if (items.length > 0 && primaryCount !== 1) {
      return mockError('Exactly one primary organization is required', 'COMMON_VALIDATION_FAILED', 400);
    }
    const organizations: PlatformUserOrg[] = items.map((item) => {
      const base = makeUserOrg(item.orgId, Boolean(item.primary));
      return { ...base, scopeWeight: item.scopeWeight ?? base.scopeWeight };
    });
    users[index] = {
      ...users[index],
      primaryOrgId: organizations.find(item => item.primary)?.orgId ?? null,
      organizations,
      updatedAt: new Date().toISOString()
    };
    return mockSuccess(toOrganizationsResponse(users[index]));
  });

  adapter.register('PATCH', '/api/platform/users/:id/org-context', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }
    const orgId = Number(req.body?.orgId);
    const organizations = users[index].organizations ?? [];
    if (!organizations.some(item => item.orgId === orgId)) {
      return mockError('User is not assigned to the organization', 'COMMON_FORBIDDEN', 403);
    }
    users[index] = {
      ...users[index],
      primaryOrgId: orgId,
      organizations: organizations.map(item => ({
        ...item,
        primary: item.orgId === orgId,
        scopeWeight: item.orgId === orgId ? 100 : 0
      })),
      updatedAt: new Date().toISOString()
    };
    return mockSuccess(users[index]);
  });

  // POST /api/platform/users/:id/disable
  adapter.register('POST', '/api/platform/users/:id/disable', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }

    users[index] = {
      ...users[index],
      status: 'DISABLED',
      updatedAt: new Date().toISOString()
    };

    return mockSuccess(undefined);
  });

  // POST /api/platform/users/:id/reset-password
  adapter.register('POST', '/api/platform/users/:id/reset-password', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const index = users.findIndex(u => u.id === id);
    if (index === -1) {
      return mockError('User not found', 'USER_NOT_FOUND', 404);
    }

    return mockSuccess(undefined);
  });
};
