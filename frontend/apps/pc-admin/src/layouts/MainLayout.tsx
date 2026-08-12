import { logout, useAuthStore } from '@iaf/auth';
import { PLATFORM_PERMISSIONS, QMS_PERMISSIONS, hasAnyPermission } from '@iaf/permissions';
import type { PlatformMenu } from '@iaf/domain-types';
import {
  iafDefaultExperienceSettings,
  iafLightShellTokens,
  iafShellTokens,
  iafSurfaceWidths,
  iafThemeNames,
  useIafTheme,
  type IafDensity,
  type IafExperienceSettings,
  type IafFontSize,
  type IafFormInteractionMode,
  type IafMotionLevel,
  type IafSidebarMode,
  type IafSurfaceWidth,
  type IafThemeName,
  type IafWorkspaceMode
} from '@iaf/theme';
import {
  AppstoreOutlined,
  AuditOutlined,
  BellOutlined,
  CompressOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  GlobalOutlined,
  FileSearchOutlined,
  LogoutOutlined,
  MenuOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  MoonOutlined,
  SearchOutlined,
  SettingOutlined,
  SunOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { App, Avatar, Badge, Button, ConfigProvider, Divider, Drawer, Dropdown, Empty, Input, Layout, List, Menu, Modal, Segmented, Select, Slider, Space, Switch, Tag, Tooltip, Typography, theme } from 'antd';
import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useNavigate } from 'react-router-dom';
import { systemConfigApi } from '../modules/platform/systemConfig/api';
import { useCurrentUserMenusQuery } from '../modules/platform/menus/hooks';
import { TabWorkspace } from '../workspace/TabWorkspace';

const DEFAULT_SIDEBAR_WIDTH = 248;
const MIN_SIDEBAR_WIDTH = 220;
const MAX_SIDEBAR_WIDTH = 360;
const COLLAPSED_SIDEBAR_WIDTH = 68;

const fallbackMenus: PlatformMenu[] = [
  { id: 1, tenantId: 1, parentId: null, menuCode: 'platform.users', menuType: 'MENU', titleKey: 'menu.users', routePath: '/platform/users', componentKey: 'platform/users/UserListPage', icon: 'UserOutlined', sortNo: 110, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.userView], children: [] },
  { id: 2, tenantId: 1, parentId: null, menuCode: 'platform.orgs', menuType: 'MENU', titleKey: 'menu.orgs', routePath: '/platform/orgs', componentKey: 'platform/orgs/OrgTreePage', icon: 'TeamOutlined', sortNo: 120, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.orgView], children: [] },
  { id: 3, tenantId: 1, parentId: null, menuCode: 'platform.roles', menuType: 'MENU', titleKey: 'menu.roles', routePath: '/platform/roles', componentKey: 'platform/roles/RoleListPage', icon: 'SettingOutlined', sortNo: 130, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.roleView], children: [] },
  { id: 4, tenantId: 1, parentId: null, menuCode: 'platform.menus', menuType: 'MENU', titleKey: 'menu.menus', routePath: '/platform/menus', componentKey: 'platform/menus/PlatformMenuConsolePage', icon: 'MenuOutlined', sortNo: 140, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.menuView], children: [] },
  { id: 5, tenantId: 1, parentId: null, menuCode: 'platform.dictionaries', menuType: 'MENU', titleKey: 'menu.dictionaries', routePath: '/platform/dictionaries', componentKey: 'platform/config/PlatformDictionaryParameterPage', icon: 'DatabaseOutlined', sortNo: 150, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.dictionaryView, PLATFORM_PERMISSIONS.parameterView], children: [] },
  { id: 6, tenantId: 1, parentId: null, menuCode: 'platform.auditLogs', menuType: 'MENU', titleKey: 'menu.auditLogs', routePath: '/platform/audit-logs', componentKey: 'platform/config/PlatformAuditLogPage', icon: 'AuditOutlined', sortNo: 160, visible: true, enabled: true, version: 0, permissionCodes: [PLATFORM_PERMISSIONS.auditView], children: [] },
  { id: 7, tenantId: 1, parentId: null, menuCode: 'platform.approvalTasks', menuType: 'MENU', titleKey: 'menu.approvalTasks', routePath: '/platform/approval/tasks', componentKey: 'platform/approval/ApprovalTaskCenterPage', icon: 'AuditOutlined', sortNo: 170, visible: true, enabled: true, version: 0, permissionCodes: [], children: [] },
  { id: 8, tenantId: 1, parentId: null, menuCode: 'platform.kanban', menuType: 'MENU', titleKey: 'menu.kanban', routePath: '/platform/kanban', componentKey: 'platform/kanban/PlatformKanbanPage', icon: 'DashboardOutlined', sortNo: 180, visible: true, enabled: true, version: 0, permissionCodes: [], children: [] },
  { id: 9, tenantId: 1, parentId: null, menuCode: 'qms.engineering.parts', menuType: 'MENU', titleKey: 'menu.qmsParts', routePath: '/qms/engineering/parts', componentKey: 'qms/engineering/QmsPartListPage', icon: 'FileSearchOutlined', sortNo: 410, visible: true, enabled: true, version: 0, permissionCodes: [QMS_PERMISSIONS.partView], children: [] }
];

const clampSidebarWidth = (width: number) => Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, width));

const flattenRouteMenus = (menus: PlatformMenu[]): PlatformMenu[] =>
  menus.flatMap((menu) => [
    ...(menu.routePath ? [menu] : []),
    ...flattenRouteMenus(menu.children ?? [])
  ]).sort((a, b) => a.sortNo - b.sortNo || a.id - b.id);

const menuIcon = (icon?: string | null): ReactNode => {
  switch (icon) {
    case 'UserOutlined':
      return <UserOutlined />;
    case 'TeamOutlined':
      return <TeamOutlined />;
    case 'SettingOutlined':
      return <SettingOutlined />;
    case 'MenuOutlined':
      return <MenuOutlined />;
    case 'DatabaseOutlined':
      return <DatabaseOutlined />;
    case 'AuditOutlined':
      return <AuditOutlined />;
    case 'DashboardOutlined':
      return <DashboardOutlined />;
    case 'FileSearchOutlined':
      return <FileSearchOutlined />;
    default:
      return <AppstoreOutlined />;
  }
};

const toExperienceSettingsPatch = (raw: Record<string, unknown>): Partial<IafExperienceSettings> => {
  const patch: Partial<IafExperienceSettings> = {};
  if (typeof raw.themeName === 'string' && iafThemeNames.includes(raw.themeName as IafThemeName)) patch.themeName = raw.themeName as IafThemeName;
  if (raw.formInteractionMode === 'modal' || raw.formInteractionMode === 'drawer' || raw.formInteractionMode === 'page') patch.formInteractionMode = raw.formInteractionMode;
  if (raw.density === 'compact' || raw.density === 'standard' || raw.density === 'comfortable') patch.density = raw.density;
  if (raw.fontSize === 'small' || raw.fontSize === 'default' || raw.fontSize === 'large') patch.fontSize = raw.fontSize;
  if (raw.sidebarMode === 'dark' || raw.sidebarMode === 'light') patch.sidebarMode = raw.sidebarMode;
  if (typeof raw.sidebarCollapsed === 'boolean') patch.sidebarCollapsed = raw.sidebarCollapsed;
  if (typeof raw.sidebarWidth === 'number' && Number.isFinite(raw.sidebarWidth)) patch.sidebarWidth = clampSidebarWidth(raw.sidebarWidth);
  if (raw.motionLevel === 'none' || raw.motionLevel === 'subtle' || raw.motionLevel === 'standard') patch.motionLevel = raw.motionLevel;
  if (raw.surfaceWidth === 'standard' || raw.surfaceWidth === 'wide' || raw.surfaceWidth === 'extra-wide') patch.surfaceWidth = raw.surfaceWidth;
  if (raw.workspaceMode === 'simple' || raw.workspaceMode === 'expert') patch.workspaceMode = raw.workspaceMode;
  return patch;
};

export const MainLayout = () => {
  const { t, i18n } = useTranslation();
  const { token } = theme.useToken();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const location = useLocation();
  const principal = useAuthStore((state) => state.principal);
  const {
    settings,
    themeName,
    setThemeName,
    formInteractionMode,
    density,
    sidebarMode,
    sidebarCollapsed,
    sidebarWidth,
    workspaceMode,
    updateExperienceSettings,
    resetExperienceSettings,
    setPreferenceScope
  } = useIafTheme();
  const [transientSidebarWidth, setTransientSidebarWidth] = useState(sidebarWidth);
  const [menuSearch, setMenuSearch] = useState('');
  const [resizing, setResizing] = useState(false);
  const [preferencesOpen, setPreferencesOpen] = useState(false);
  const [commandOpen, setCommandOpen] = useState(false);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [commandKeyword, setCommandKeyword] = useState('');
  const [preferenceDraft, setPreferenceDraft] = useState<IafExperienceSettings>(settings);
  const sidebarWidthRef = useRef(sidebarWidth);
  const currentUserMenusQuery = useCurrentUserMenusQuery();

  const siderTheme = sidebarMode;
  const collapsed = sidebarCollapsed;
  const effectiveSidebarWidth = collapsed ? COLLAPSED_SIDEBAR_WIDTH : transientSidebarWidth;
  const displayName = principal?.displayName ?? principal?.username ?? 'User';
  const tenantLabel = principal?.tenantId ? t('shell.tenant', { id: principal.tenantId }) : t('shell.tenantFallback');
  const hasMenuSearch = menuSearch.trim().length > 0;
  const isDarkTheme = themeName === 'dark-industrial' || themeName === 'dashboard-industrial' || themeName === 'high-contrast';
  const shellColors = sidebarMode === 'light' ? iafLightShellTokens[themeName] : iafShellTokens[themeName];
  const contentPadding = density === 'compact' ? 16 : density === 'comfortable' ? 28 : token.paddingLG;

  const menuItems = useMemo(
    () => {
      const userPermissions = principal?.permissions ?? [];
      const sourceMenus = currentUserMenusQuery.data?.length
        ? currentUserMenusQuery.data
        : fallbackMenus.filter((menu) => menu.permissionCodes.length === 0 || hasAnyPermission(userPermissions, menu.permissionCodes));
      const items = flattenRouteMenus(sourceMenus).map((menu) => ({
        key: menu.routePath ?? menu.menuCode,
        label: t(menu.titleKey),
        icon: menuIcon(menu.icon)
      })) as Required<MenuProps>['items'];

      if (!menuSearch.trim()) {
        return items;
      }

      const keyword = menuSearch.trim().toLowerCase();
      return items.filter((item) => {
        if (!item || !('label' in item)) return false;
        return String(item.label).toLowerCase().includes(keyword) || String(item.key).toLowerCase().includes(keyword);
      });
    },
    [currentUserMenusQuery.data, principal?.permissions, t]
  );

  const commandItems = useMemo(() => {
    const keyword = commandKeyword.trim().toLowerCase();
    return menuItems.flatMap((item) => {
      if (!item || !('label' in item) || !('key' in item)) return [];
      const label = String(item.label);
      const key = String(item.key);
      if (keyword && !label.toLowerCase().includes(keyword) && !key.toLowerCase().includes(keyword)) {
        return [];
      }
      return [{
        key,
        label,
        icon: 'icon' in item ? item.icon : undefined
      }];
    });
  }, [commandKeyword, menuItems]);

  const persistExperiencePreferences = useCallback(async (patch: Partial<IafExperienceSettings>) => {
    const nextSettings = { ...settings, ...patch };
    updateExperienceSettings(patch);
    try {
      await systemConfigApi.saveMyPreference(nextSettings);
    } catch {
      message.warning(t('profile.preferencesSavedLocally'));
    }
  }, [message, settings, t, updateExperienceSettings]);

  const setSidebarCollapsed = (next: boolean) => {
    void persistExperiencePreferences({ sidebarCollapsed: next });
  };

  useEffect(() => {
    if (resizing) return;
    const nextWidth = clampSidebarWidth(sidebarWidth || DEFAULT_SIDEBAR_WIDTH);
    sidebarWidthRef.current = nextWidth;
    setTransientSidebarWidth(nextWidth);
  }, [resizing, sidebarWidth]);

  useEffect(() => {
    if (!resizing) return undefined;

    const handleMouseMove = (event: MouseEvent) => {
      const nextWidth = Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, event.clientX));
      sidebarWidthRef.current = nextWidth;
      setTransientSidebarWidth(nextWidth);
    };

    const handleMouseUp = () => {
      setResizing(false);
      void persistExperiencePreferences({ sidebarWidth: sidebarWidthRef.current });
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
    document.body.style.cursor = 'ew-resize';
    document.body.style.userSelect = 'none';

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [persistExperiencePreferences, resizing]);

  useEffect(() => {
    if (!principal) return;
    setPreferenceScope(`tenant-${principal.tenantId}-user-${principal.userId}`);
  }, [principal, setPreferenceScope]);

  useEffect(() => {
    if (!principal) return;
    let cancelled = false;

    systemConfigApi.getMyPreference()
      .then((preference) => {
        if (cancelled) return;
        const patch = toExperienceSettingsPatch(preference.settings);
        if (Object.keys(patch).length > 0) {
          updateExperienceSettings(patch);
        }
      })
      .catch(() => {
        // Local persisted preferences remain the fallback when the backend
        // preference API is unavailable during development or offline use.
      });

    return () => {
      cancelled = true;
    };
  }, [principal, updateExperienceSettings]);

  useEffect(() => {
    if (preferencesOpen) {
      setPreferenceDraft(settings);
    }
  }, [preferencesOpen, settings]);

  const patchPreferenceDraft = <Key extends keyof IafExperienceSettings>(key: Key, value: IafExperienceSettings[Key]) => {
    setPreferenceDraft((current) => ({ ...current, [key]: value }));
  };

  const savePreferences = async () => {
    updateExperienceSettings(preferenceDraft);
    try {
      await systemConfigApi.saveMyPreference({ ...preferenceDraft });
      setPreferencesOpen(false);
      message.success(t('profile.preferencesSaved'));
    } catch {
      setPreferencesOpen(false);
      message.warning(t('profile.preferencesSavedLocally'));
    }
  };

  const handleResetPreferences = async () => {
    resetExperienceSettings();
    try {
      await systemConfigApi.saveMyPreference({ ...iafDefaultExperienceSettings });
      setPreferencesOpen(false);
      message.success(t('profile.preferencesReset'));
    } catch {
      setPreferencesOpen(false);
      message.warning(t('profile.preferencesResetLocally'));
    }
  };

  const toggleLanguage = async () => {
    const nextLanguage = i18n.language === 'zh-CN' ? 'en-US' : 'zh-CN';
    await i18n.changeLanguage(nextLanguage);
    message.success(t('shell.languageSwitched'));
  };

  const toggleWorkspaceMode = () => {
    const nextMode = workspaceMode === 'expert' ? 'simple' : 'expert';
    updateExperienceSettings({ workspaceMode: nextMode });
    message.success(t(`settings.workspaceModes.${nextMode}`));
  };

  const accountMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: t('shell.profile')
    },
    {
      key: 'preferences',
      icon: <SettingOutlined />,
      label: t('shell.preferences'),
      onClick: () => setPreferencesOpen(true)
    },
    { type: 'divider' },
    {
      key: 'logout',
      danger: true,
      icon: <LogoutOutlined />,
      label: t('app.logout'),
      onClick: () => {
        logout();
        navigate('/login', { replace: true });
      }
    }
  ];

  const sidebarMenuTheme = {
    components: {
      Menu: {
        darkItemBg: shellColors.sidebarBg,
        darkItemColor: shellColors.sidebarText,
        darkItemHoverBg: shellColors.sidebarActiveBg,
        darkItemHoverColor: shellColors.sidebarText,
        darkItemSelectedBg: shellColors.sidebarActiveBg,
        darkItemSelectedColor: shellColors.sidebarAccent,
        darkSubMenuItemBg: shellColors.sidebarBg,
        itemBg: shellColors.sidebarBg,
        itemColor: shellColors.sidebarText,
        itemHoverBg: shellColors.sidebarActiveBg,
        itemHoverColor: shellColors.sidebarText,
        itemSelectedBg: shellColors.sidebarActiveBg,
        itemSelectedColor: shellColors.sidebarAccent,
        itemBorderRadius: 6
      }
    }
  };

  return (
    <>
      <Layout className="iaf-shell-root" data-testid="iaf-shell" style={{ height: '100vh', minHeight: '100vh', overflow: 'hidden' }}>
        <Layout.Sider
        className="iaf-shell-sidebar"
        data-testid="iaf-shell-sidebar"
        width={effectiveSidebarWidth}
        collapsedWidth={COLLAPSED_SIDEBAR_WIDTH}
        collapsed={collapsed}
        trigger={null}
        theme={siderTheme}
        style={{
          position: 'relative',
          height: '100vh',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          borderRight: `1px solid ${shellColors.sidebarBorder}`,
          background: shellColors.sidebarBg,
          boxShadow: collapsed ? undefined : shellColors.sidebarShadow
        }}
      >
        <div
          style={{
            height: 64,
            flexShrink: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'space-between',
            paddingInline: collapsed ? 12 : 16,
            borderBottom: `1px solid ${shellColors.sidebarBorder}`,
            background: shellColors.sidebarPanel
          }}
        >
          <Space size={10}>
            <Avatar
              shape="square"
              size={34}
              style={{
                background: `linear-gradient(135deg, ${shellColors.sidebarAccent}, ${token.colorSuccess})`,
                color: shellColors.sidebarAvatarText,
                fontWeight: 700,
                boxShadow: `0 0 0 1px ${shellColors.sidebarBorder}, 0 8px 20px ${shellColors.sidebarAvatarGlow}`
              }}
            >
              IAF
            </Avatar>
            {!collapsed && (
              <div>
                <Typography.Text strong style={{ display: 'block', lineHeight: 1.1, color: shellColors.sidebarText }}>
                  {t('app.name')}
                </Typography.Text>
                <Typography.Text style={{ fontSize: 12, color: shellColors.sidebarMuted }}>
                  {t('shell.brandSubtitle')}
                </Typography.Text>
              </div>
            )}
          </Space>
          {!collapsed && (
            <Tooltip title={t('shell.collapseSidebar')}>
              <Button
                type="text"
                aria-label={t('shell.collapseSidebar')}
                icon={<MenuFoldOutlined />}
                onClick={() => setSidebarCollapsed(true)}
                style={{ width: 40, height: 40, color: shellColors.sidebarMuted }}
              />
            </Tooltip>
          )}
        </div>

        <div style={{ padding: collapsed ? 10 : 14, flexShrink: 0 }}>
          {collapsed ? (
            <Tooltip title={t('shell.menuSearch')} placement="right">
              <Button
                block
                aria-label={t('shell.menuSearch')}
                icon={<SearchOutlined />}
                onClick={() => setSidebarCollapsed(false)}
                style={{ height: 44, color: shellColors.sidebarText, borderColor: shellColors.sidebarBorder, background: shellColors.sidebarPanel }}
              />
            </Tooltip>
          ) : (
            <Input
              allowClear
              size="middle"
              prefix={<SearchOutlined />}
              aria-label={t('shell.menuSearch')}
              placeholder={t('shell.menuSearch')}
              value={menuSearch}
              onChange={(event) => setMenuSearch(event.target.value)}
              style={{
                background: shellColors.sidebarPanel,
                borderColor: shellColors.sidebarBorder,
                color: shellColors.sidebarText
              }}
            />
          )}
        </div>

        <div
          className="iaf-shell-menu-scroll"
          data-testid="iaf-shell-menu-scroll"
          style={{
            flex: 1,
            minHeight: 0,
            overflowY: 'auto',
            paddingBottom: 12
          }}
        >
          {!collapsed && (
            <Typography.Text
              type="secondary"
              style={{ display: 'block', paddingInline: 18, paddingBottom: 8, fontSize: 12, fontWeight: 600, color: shellColors.sidebarMuted }}
            >
              {t('menu.platform')}
            </Typography.Text>
          )}
          {hasMenuSearch && menuItems.length === 0 ? (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description={t('shell.noMenuResults')}
              style={{ marginBlock: 24, color: shellColors.sidebarMuted }}
            />
          ) : (
            <ConfigProvider theme={sidebarMenuTheme}>
              <Menu
                theme={siderTheme}
                mode="inline"
                selectedKeys={[location.pathname]}
                items={menuItems}
                onClick={({ key }) => navigate(key)}
                style={{
                  borderInlineEnd: 0,
                  background: 'transparent',
                  color: shellColors.sidebarText
                }}
              />
            </ConfigProvider>
          )}
        </div>

        <div
          className="iaf-shell-profile"
          data-testid="iaf-shell-profile"
          style={{
            flexShrink: 0,
            padding: collapsed ? 10 : 12,
            borderTop: `1px solid ${shellColors.sidebarBorder}`,
            background: shellColors.sidebarPanel
          }}
        >
          <Dropdown menu={{ items: accountMenuItems }} trigger={['click']} placement="topRight">
            <Button
              type="text"
              block
              style={{
                height: collapsed ? 44 : 48,
                display: 'flex',
                alignItems: 'center',
                justifyContent: collapsed ? 'center' : 'flex-start',
                paddingInline: collapsed ? 0 : 8,
                color: shellColors.sidebarText
              }}
            >
              <Space size={10}>
                <Avatar size={30} icon={<UserOutlined />} style={{ background: shellColors.sidebarAccent, color: shellColors.sidebarAvatarText }} />
                {!collapsed && (
                  <span style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', minWidth: 0 }}>
                    <Typography.Text strong ellipsis style={{ maxWidth: effectiveSidebarWidth - 92, lineHeight: 1.1, color: shellColors.sidebarText }}>
                      {displayName}
                    </Typography.Text>
                    <Typography.Text ellipsis style={{ maxWidth: effectiveSidebarWidth - 92, fontSize: 12, color: shellColors.sidebarMuted }}>
                      {tenantLabel}
                    </Typography.Text>
                  </span>
                )}
              </Space>
            </Button>
          </Dropdown>
          {collapsed && (
            <Tooltip title={t('shell.expandSidebar')} placement="right">
              <Button
                block
                type="text"
                aria-label={t('shell.expandSidebar')}
                icon={<MenuUnfoldOutlined />}
                onClick={() => setSidebarCollapsed(false)}
                style={{ height: 40, color: shellColors.sidebarMuted }}
              />
            </Tooltip>
          )}
        </div>

        {!collapsed && (
          <div
            role="separator"
            aria-label={t('shell.resizeSidebar')}
            onMouseDown={() => setResizing(true)}
            style={{
              position: 'absolute',
              top: 0,
              right: -3,
              width: 6,
              height: '100%',
              cursor: 'ew-resize',
              background: resizing ? shellColors.sidebarAccent : 'transparent',
              zIndex: 2
            }}
          />
        )}
      </Layout.Sider>
      <Layout style={{ height: '100vh', minWidth: 0, overflow: 'hidden' }}>
        <Layout.Header
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingInline: 24,
            background: shellColors.topbarBg,
            borderBottom: `1px solid ${token.colorBorder}`,
            boxShadow: isDarkTheme ? undefined : shellColors.topbarShadow,
            flexShrink: 0
          }}
        >
          <Space split={<Divider type="vertical" />}>
            <Space>
              <AppstoreOutlined style={{ color: shellColors.topbarAccent }} />
              <Typography.Text strong>{t('shell.workspace')}</Typography.Text>
            </Space>
            <Typography.Text type="secondary">{tenantLabel}</Typography.Text>
          </Space>
          <Space>
            <Tooltip title={t('shell.globalSearch')}>
              <Button aria-label={t('shell.globalSearch')} icon={<SearchOutlined />} onClick={() => setCommandOpen(true)} style={{ width: 44, height: 44 }} />
            </Tooltip>
            <Tooltip title={t('shell.notifications')}>
              <Badge count={3} size="small">
                <Button aria-label={t('shell.notifications')} icon={<BellOutlined />} onClick={() => setNotificationOpen(true)} style={{ width: 44, height: 44 }} />
              </Badge>
            </Tooltip>
            <Tooltip title={t('settings.theme')}>
              <Button
                aria-label={t('settings.theme')}
                icon={themeName === 'dark-industrial' ? <MoonOutlined /> : <SunOutlined />}
                onClick={() => setThemeName(themeName === 'dark-industrial' ? 'light-industrial' : 'dark-industrial')}
                style={{ width: 44, height: 44 }}
              />
            </Tooltip>
            <Tooltip title={t('shell.language')}>
              <Button aria-label={t('shell.language')} icon={<GlobalOutlined />} onClick={toggleLanguage} style={{ width: 44, height: 44 }} />
            </Tooltip>
            <Tooltip title={t('workspace.expertMode')}>
              <Button
                type={workspaceMode === 'expert' ? 'primary' : 'default'}
                aria-label={t('workspace.expertMode')}
                icon={<CompressOutlined />}
                onClick={toggleWorkspaceMode}
                style={{ width: 44, height: 44 }}
              />
            </Tooltip>
            <Tooltip title={t('shell.preferences')}>
              <Button
                aria-label={t('shell.preferences')}
                icon={<SettingOutlined />}
                onClick={() => setPreferencesOpen(true)}
                style={{ width: 44, height: 44 }}
              />
            </Tooltip>
            <Dropdown menu={{ items: accountMenuItems }} trigger={['click']} placement="bottomRight">
              <Button style={{ height: 44, paddingInline: 10 }}>
                <Space>
                  <Avatar size={24} icon={<UserOutlined />} style={{ background: token.colorPrimary }} />
                  <Typography.Text strong>{displayName}</Typography.Text>
                </Space>
              </Button>
            </Dropdown>
          </Space>
        </Layout.Header>
        <Layout.Content
          style={{
            padding: contentPadding,
            background: token.colorBgLayout,
            minWidth: 0,
            overflow: 'auto'
          }}
        >
          <TabWorkspace />
        </Layout.Content>
      </Layout>
      </Layout>
      <Drawer
        open={preferencesOpen}
        title={t('profile.preferencesTitle')}
        width={iafSurfaceWidths[settings.surfaceWidth]}
        onClose={() => setPreferencesOpen(false)}
        destroyOnHidden
        styles={{
          body: { background: token.colorBgLayout },
          footer: {
            display: 'flex',
            justifyContent: 'space-between',
            borderTop: `1px solid ${token.colorBorderSecondary}`
          }
        }}
        footer={
          <>
            <Button onClick={handleResetPreferences}>{t('profile.resetPreferences')}</Button>
            <Space>
              <Button onClick={() => setPreferencesOpen(false)}>{t('common.actions.cancel')}</Button>
              <Button type="primary" onClick={savePreferences}>
                {t('profile.savePreferences')}
              </Button>
            </Space>
          </>
        }
      >
        <Space direction="vertical" size={20} style={{ width: '100%' }}>
          <div>
            <Typography.Title level={5}>{t('profile.sections.appearance')}</Typography.Title>
            <Space direction="vertical" size={14} style={{ width: '100%' }}>
              <PreferenceRow label={t('settings.theme')}>
                <Segmented
                  value={preferenceDraft.themeName}
                  onChange={(value) => patchPreferenceDraft('themeName', value as IafThemeName)}
                  options={[
                    { label: t('settings.themes.lightIndustrial'), value: 'light-industrial' },
                    { label: t('settings.themes.darkIndustrial'), value: 'dark-industrial' },
                    { label: t('settings.themes.compactIndustrial'), value: 'compact-industrial' },
                    { label: t('settings.themes.dashboardIndustrial'), value: 'dashboard-industrial' },
                    { label: t('settings.themes.mobileWork'), value: 'mobile-work' },
                    { label: t('settings.themes.highContrast'), value: 'high-contrast' },
                    { label: t('settings.themes.customerBrand'), value: 'customer-brand' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.sidebarMode')}>
                <Segmented
                  value={preferenceDraft.sidebarMode}
                  onChange={(value) => patchPreferenceDraft('sidebarMode', value as IafSidebarMode)}
                  options={[
                    { label: t('settings.sidebarModes.dark'), value: 'dark' },
                    { label: t('settings.sidebarModes.light'), value: 'light' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.sidebarCollapsed')}>
                <Switch
                  aria-label={t('settings.sidebarCollapsed')}
                  checked={preferenceDraft.sidebarCollapsed}
                  onChange={(value) => patchPreferenceDraft('sidebarCollapsed', value)}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.sidebarWidth')}>
                <Slider
                  aria-label={t('settings.sidebarWidth')}
                  min={MIN_SIDEBAR_WIDTH}
                  max={MAX_SIDEBAR_WIDTH}
                  step={4}
                  value={preferenceDraft.sidebarWidth}
                  onChange={(value) => patchPreferenceDraft('sidebarWidth', clampSidebarWidth(value))}
                  style={{ width: 220 }}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.fontSize')}>
                <Segmented
                  value={preferenceDraft.fontSize}
                  onChange={(value) => patchPreferenceDraft('fontSize', value as IafFontSize)}
                  options={[
                    { label: t('settings.fontSizes.small'), value: 'small' },
                    { label: t('settings.fontSizes.default'), value: 'default' },
                    { label: t('settings.fontSizes.large'), value: 'large' }
                  ]}
                />
              </PreferenceRow>
            </Space>
          </div>
          <div>
            <Typography.Title level={5}>{t('profile.sections.workspace')}</Typography.Title>
            <Space direction="vertical" size={14} style={{ width: '100%' }}>
              <PreferenceRow label={t('settings.density')}>
                <Segmented
                  value={preferenceDraft.density}
                  onChange={(value) => patchPreferenceDraft('density', value as IafDensity)}
                  options={[
                    { label: t('settings.densities.compact'), value: 'compact' },
                    { label: t('settings.densities.standard'), value: 'standard' },
                    { label: t('settings.densities.comfortable'), value: 'comfortable' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.formInteractionMode')}>
                <Select
                  value={preferenceDraft.formInteractionMode}
                  style={{ width: 180 }}
                  onChange={(value) => patchPreferenceDraft('formInteractionMode', value as IafFormInteractionMode)}
                  options={[
                    { label: t('settings.formInteractionModes.modal'), value: 'modal' },
                    { label: t('settings.formInteractionModes.drawer'), value: 'drawer' },
                    { label: t('settings.formInteractionModes.page'), value: 'page' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.surfaceWidth')}>
                <Segmented
                  value={preferenceDraft.surfaceWidth}
                  onChange={(value) => patchPreferenceDraft('surfaceWidth', value as IafSurfaceWidth)}
                  options={[
                    { label: t('settings.surfaceWidths.standard'), value: 'standard' },
                    { label: t('settings.surfaceWidths.wide'), value: 'wide' },
                    { label: t('settings.surfaceWidths.extraWide'), value: 'extra-wide' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.motionLevel')}>
                <Segmented
                  value={preferenceDraft.motionLevel}
                  onChange={(value) => patchPreferenceDraft('motionLevel', value as IafMotionLevel)}
                  options={[
                    { label: t('settings.motionLevels.none'), value: 'none' },
                    { label: t('settings.motionLevels.subtle'), value: 'subtle' },
                    { label: t('settings.motionLevels.standard'), value: 'standard' }
                  ]}
                />
              </PreferenceRow>
              <PreferenceRow label={t('settings.workspaceMode')}>
                <Segmented
                  value={preferenceDraft.workspaceMode}
                  onChange={(value) => patchPreferenceDraft('workspaceMode', value as IafWorkspaceMode)}
                  options={[
                    { label: t('settings.workspaceModes.simple'), value: 'simple' },
                    { label: t('settings.workspaceModes.expert'), value: 'expert' }
                  ]}
                />
              </PreferenceRow>
            </Space>
          </div>
          <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
            {t('profile.preferenceDescription')}
          </Typography.Paragraph>
        </Space>
      </Drawer>
      <Modal
        open={commandOpen}
        title={t('shell.globalSearch')}
        footer={null}
        width={680}
        style={{ maxWidth: '90vw' }}
        onCancel={() => setCommandOpen(false)}
        destroyOnHidden
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Input
            autoFocus
            allowClear
            prefix={<SearchOutlined />}
            placeholder={t('shell.commandPlaceholder')}
            value={commandKeyword}
            onChange={(event) => setCommandKeyword(event.target.value)}
          />
          <List
            dataSource={commandItems}
            locale={{ emptyText: t('shell.noMenuResults') }}
            renderItem={(item) => (
              <List.Item
                style={{ cursor: 'pointer' }}
                onClick={() => {
                  navigate(item.key);
                  setCommandOpen(false);
                  setCommandKeyword('');
                }}
              >
                <List.Item.Meta
                  avatar={item.icon}
                  title={item.label}
                  description={item.key}
                />
              </List.Item>
            )}
          />
        </Space>
      </Modal>
      <Drawer
        open={notificationOpen}
        title={t('shell.notifications')}
        width={420}
        styles={{ wrapper: { maxWidth: '90vw' } }}
        onClose={() => setNotificationOpen(false)}
      >
        <List
          dataSource={[
            { key: 'approval', tone: 'processing', title: t('notifications.approval.title'), description: t('notifications.approval.description') },
            { key: 'integration', tone: 'warning', title: t('notifications.integration.title'), description: t('notifications.integration.description') },
            { key: 'kanban', tone: 'success', title: t('notifications.kanban.title'), description: t('notifications.kanban.description') }
          ]}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                avatar={<Badge status={item.tone as 'processing' | 'warning' | 'success'} />}
                title={<Space>{item.title}<Tag>{t('platformConfig.mockFirst')}</Tag></Space>}
                description={item.description}
              />
            </List.Item>
          )}
        />
      </Drawer>
    </>
  );
};

const PreferenceRow = ({ label, children }: { label: ReactNode; children: ReactNode }) => (
  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
    <Typography.Text strong>{label}</Typography.Text>
    <div>{children}</div>
  </div>
);
