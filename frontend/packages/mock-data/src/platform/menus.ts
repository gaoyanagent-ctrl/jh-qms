import { mockError, mockSuccess, MockApiAdapter, MockRequest } from '@iaf/api-client';
import type { PlatformMenu } from '@iaf/domain-types';

export const mockMenus: PlatformMenu[] = [
  {
    id: 1,
    tenantId: 1,
    parentId: null,
    menuCode: 'platform',
    menuType: 'GROUP',
    titleKey: 'menu.platform',
    routePath: null,
    componentKey: null,
    icon: 'AppstoreOutlined',
    sortNo: 100,
    visible: true,
    enabled: true,
    version: 1,
    permissionCodes: [],
    children: [
      { id: 2, tenantId: 1, parentId: 1, menuCode: 'platform.users', menuType: 'MENU', titleKey: 'menu.users', routePath: '/platform/users', componentKey: 'platform/users/UserListPage', icon: 'UserOutlined', sortNo: 110, visible: true, enabled: true, version: 1, permissionCodes: ['platform:user:view'], children: [] },
      { id: 3, tenantId: 1, parentId: 1, menuCode: 'platform.orgs', menuType: 'MENU', titleKey: 'menu.orgs', routePath: '/platform/orgs', componentKey: 'platform/orgs/OrgTreePage', icon: 'TeamOutlined', sortNo: 120, visible: true, enabled: true, version: 1, permissionCodes: ['platform:org:view'], children: [] },
      { id: 4, tenantId: 1, parentId: 1, menuCode: 'platform.roles', menuType: 'MENU', titleKey: 'menu.roles', routePath: '/platform/roles', componentKey: 'platform/roles/RoleListPage', icon: 'SettingOutlined', sortNo: 130, visible: true, enabled: true, version: 1, permissionCodes: ['platform:role:view'], children: [] },
      { id: 5, tenantId: 1, parentId: 1, menuCode: 'platform.menus', menuType: 'MENU', titleKey: 'menu.menus', routePath: '/platform/menus', componentKey: 'platform/menus/PlatformMenuConsolePage', icon: 'MenuOutlined', sortNo: 140, visible: true, enabled: true, version: 1, permissionCodes: ['platform:menu:view'], children: [] },
      { id: 6, tenantId: 1, parentId: 1, menuCode: 'platform.dictionaries', menuType: 'MENU', titleKey: 'menu.dictionaries', routePath: '/platform/dictionaries', componentKey: 'platform/config/PlatformDictionaryParameterPage', icon: 'DatabaseOutlined', sortNo: 150, visible: true, enabled: true, version: 1, permissionCodes: ['platform:dictionary:view', 'platform:parameter:view'], children: [] },
      { id: 7, tenantId: 1, parentId: 1, menuCode: 'platform.auditLogs', menuType: 'MENU', titleKey: 'menu.auditLogs', routePath: '/platform/audit-logs', componentKey: 'platform/config/PlatformAuditLogPage', icon: 'AuditOutlined', sortNo: 160, visible: true, enabled: true, version: 1, permissionCodes: ['platform:audit:view'], children: [] },
      { id: 8, tenantId: 1, parentId: 1, menuCode: 'platform.approvalTasks', menuType: 'MENU', titleKey: 'menu.approvalTasks', routePath: '/platform/approval/tasks', componentKey: 'platform/approval/ApprovalTaskCenterPage', icon: 'AuditOutlined', sortNo: 170, visible: true, enabled: true, version: 1, permissionCodes: [], children: [] },
      { id: 9, tenantId: 1, parentId: 1, menuCode: 'platform.kanban', menuType: 'MENU', titleKey: 'menu.kanban', routePath: '/platform/kanban', componentKey: 'platform/kanban/PlatformKanbanPage', icon: 'DashboardOutlined', sortNo: 180, visible: true, enabled: true, version: 1, permissionCodes: [], children: [] }
    ]
  }
];

const flattenMenus = (menus: PlatformMenu[]): PlatformMenu[] =>
  menus.flatMap((menu) => [menu, ...flattenMenus(menu.children ?? [])]);

const cloneMenus = (menus: PlatformMenu[]) => JSON.parse(JSON.stringify(menus)) as PlatformMenu[];

export const registerMenuMocks = (adapter: MockApiAdapter) => {
  adapter.register('GET', '/api/platform/menus/tree', () => mockSuccess(cloneMenus(mockMenus)));

  adapter.register('GET', '/api/platform/auth/menus', (req: MockRequest) => {
    const authorization = req.headers?.authorization ?? req.headers?.Authorization ?? '';
    const isOperator = authorization.includes('mock-token-operator');
    if (!isOperator) {
      return mockSuccess(cloneMenus(mockMenus));
    }
    const allowed = new Set(['platform.users', 'platform.orgs', 'platform.roles']);
    const root = cloneMenus(mockMenus)[0];
    root.children = root.children.filter((menu) => allowed.has(menu.menuCode));
    return mockSuccess([root]);
  });

  adapter.register('POST', '/api/platform/menus', (req: MockRequest) => {
    const root = mockMenus[0];
    const nextId = flattenMenus(mockMenus).length + 1;
    const body = req.body;
    if (flattenMenus(mockMenus).some((menu) => menu.menuCode === body.menuCode)) {
      return mockError('Menu code already exists', 'PLATFORM_PERMISSION_MENU_CODE_ALREADY_EXISTS', 400);
    }
    const menu: PlatformMenu = {
      id: nextId,
      tenantId: 1,
      parentId: body.parentId ?? root.id,
      menuCode: body.menuCode,
      menuType: body.menuType,
      titleKey: body.titleKey,
      routePath: body.routePath ?? null,
      componentKey: body.componentKey ?? null,
      icon: body.icon ?? null,
      sortNo: body.sortNo ?? 0,
      visible: body.visible,
      enabled: body.enabled,
      version: 1,
      permissionCodes: [],
      children: []
    };
    root.children.push(menu);
    return mockSuccess(menu);
  });

  adapter.register('PUT', '/api/platform/menus/:id', (req: MockRequest) => {
    const id = Number(req.pathParams.id);
    const menu = flattenMenus(mockMenus).find((item) => item.id === id);
    if (!menu) {
      return mockError('Menu not found', 'PLATFORM_PERMISSION_MENU_NOT_FOUND', 404);
    }
    Object.assign(menu, req.body, { version: menu.version + 1 });
    return mockSuccess(menu);
  });
};
