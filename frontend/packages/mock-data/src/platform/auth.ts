import { mockError, mockSuccess, MockApiAdapter, MockRequest } from '@iaf/api-client';
import type { AuthPrincipal, LoginResponse } from '@iaf/domain-types';

const adminPrincipal: AuthPrincipal = {
  tenantId: 1,
  userId: 1,
  username: 'admin',
  displayName: 'System Admin',
  currentOrgId: 1,
  organizations: [
    { id: 1, orgId: 1, orgCode: 'CORP', orgName: 'IAF Industrial Corp', orgType: 'COMPANY', primary: true, scopeWeight: 100 },
    { id: 2, orgId: 2, orgCode: 'WMS_DEPT', orgName: 'Warehouse Department', orgType: 'DEPARTMENT', primary: false, scopeWeight: 0 }
  ],
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
    'platform:preference:me',
    'qms:part:view',
    'qms:part:create',
    'qms:drawing:view',
    'qms:drawing:create',
    'qms:drawing-revision:view',
    'qms:drawing-revision:create'
  ]
};

const operatorPrincipal: AuthPrincipal = {
  tenantId: 1,
  userId: 2,
  username: 'operator',
  displayName: 'WMS Operator',
  currentOrgId: 2,
  organizations: [
    { id: 3, orgId: 2, orgCode: 'WMS_DEPT', orgName: 'Warehouse Department', orgType: 'DEPARTMENT', primary: true, scopeWeight: 100 }
  ],
  permissions: ['platform:auth:me', 'platform:user:view', 'platform:org:view', 'platform:role:view', 'platform:preference:me']
};

const principals: Record<string, AuthPrincipal> = {
  admin: adminPrincipal,
  operator: operatorPrincipal
};

const tokens = new Map<string, AuthPrincipal>();
const mockTenantCode = (import.meta as ImportMeta & { env?: Record<string, string | undefined> }).env?.VITE_IAF_MOCK_TENANT_CODE;

export const registerAuthMocks = (adapter: MockApiAdapter) => {
  adapter.register('POST', '/api/platform/auth/login', (req: MockRequest) => {
    const tenantCode = String(req.body?.tenantCode ?? '');
    const username = String(req.body?.username ?? '');
    const password = String(req.body?.password ?? '');
    const principal = principals[username];
    const tenantAccepted = mockTenantCode ? tenantCode === mockTenantCode : tenantCode.length > 0;

    if (!tenantAccepted || !principal || password.length === 0) {
      return mockError('Login failed', 'COMMON_UNAUTHORIZED', 401);
    }

    const accessToken = `mock-token-${principal.username}`;
    tokens.set(accessToken, principal);

    const response: LoginResponse = {
      ...principal,
      tokenType: 'Bearer',
      accessToken,
      expiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString()
    };

    return mockSuccess(response);
  });

  adapter.register('GET', '/api/platform/auth/me', (req: MockRequest) => {
    const authorization = req.headers?.authorization ?? req.headers?.Authorization;
    const token = authorization?.replace(/^Bearer\s+/i, '');
    const principal = token ? tokens.get(token) : undefined;

    if (!principal) {
      return mockError('Unauthorized', 'COMMON_UNAUTHORIZED', 401);
    }

    return mockSuccess(principal);
  });
};
