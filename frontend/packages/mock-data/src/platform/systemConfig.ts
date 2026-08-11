import { mockSuccess, MockApiAdapter, type MockRequest } from '@iaf/api-client';

const defaultTheme = {
  themeName: 'light-industrial',
  primaryColor: '#334155',
  sidebarMode: 'dark',
  tokens: {}
};

const defaultBrand = {
  brandName: 'IAF 平台',
  loginHeroTitle: '面向制造企业的工业应用基础平台',
  loginHeroSubtitle: '统一平台管理、权限、组织、审批、集成和业务扩展能力，为 WMS、MES、SRM、QMS 等工业系统提供一致工程底座。',
  loginOpsTitle: '工业运营视图',
  loginOpsDescription: '主题、权限和复杂视图能力从平台层统一治理，支撑管理端、Kanban、设计器和大屏场景。',
  loginBackgroundType: 'token',
  loginTemplate: 'standard-industrial'
};

const preferences = new Map<number, Record<string, unknown>>();
const defaultPreferenceSettings = {
  themeName: 'light-industrial',
  formInteractionMode: 'drawer',
  density: 'standard',
  fontSize: 'default',
  sidebarMode: 'dark',
  sidebarCollapsed: false,
  sidebarWidth: 248,
  motionLevel: 'subtle',
  surfaceWidth: 'wide',
  workspaceMode: 'simple'
};

const currentUserId = (authorization?: string) => {
  if (authorization?.includes('mock-token-operator')) return 2;
  return 1;
};

export const registerSystemConfigMocks = (adapter: MockApiAdapter) => {
  adapter.register('GET', '/api/platform/theme/current', () => mockSuccess(defaultTheme));
  adapter.register('PUT', '/api/platform/theme/current', (req: MockRequest) => mockSuccess({ ...defaultTheme, ...req.body }));
  adapter.register('GET', '/api/platform/brand/current', () => mockSuccess(defaultBrand));
  adapter.register('PUT', '/api/platform/brand/current', (req: MockRequest) => mockSuccess({ ...defaultBrand, ...req.body }));
  adapter.register('GET', '/api/platform/i18n/resources', (req: MockRequest) => mockSuccess({ locale: String(req.queryParams?.locale ?? 'zh-CN'), resources: [] }));
  adapter.register('PUT', '/api/platform/i18n/resources', () => mockSuccess(undefined));

  adapter.register('GET', '/api/platform/preferences/me', (req: MockRequest) => {
    const userId = currentUserId(req.headers?.authorization ?? req.headers?.Authorization);
    return mockSuccess({
      userId,
      settings: preferences.get(userId) ?? defaultPreferenceSettings
    });
  });

  adapter.register('PUT', '/api/platform/preferences/me', (req: MockRequest) => {
    const userId = currentUserId(req.headers?.authorization ?? req.headers?.Authorization);
    const settings = (req.body?.settings ?? {}) as Record<string, unknown>;
    preferences.set(userId, settings);
    return mockSuccess({ userId, settings });
  });
};
