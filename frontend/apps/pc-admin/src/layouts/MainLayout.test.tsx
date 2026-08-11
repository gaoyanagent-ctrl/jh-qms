import { useAuthStore } from '@iaf/auth';
import { PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { iafDefaultExperienceSettings, IafThemeProvider } from '@iaf/theme';
import { App as AntApp } from 'antd';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MainLayout } from './MainLayout';
import { systemConfigApi } from '../modules/platform/systemConfig/api';
import { useCurrentUserMenusQuery } from '../modules/platform/menus/hooks';

vi.mock('../modules/platform/systemConfig/api', () => ({
  systemConfigApi: {
    getMyPreference: vi.fn(),
    saveMyPreference: vi.fn()
  }
}));

vi.mock('../modules/platform/menus/hooks', () => ({
  useCurrentUserMenusQuery: vi.fn()
}));

describe('MainLayout', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.mocked(systemConfigApi.getMyPreference).mockResolvedValue({ userId: 1, settings: {} });
    vi.mocked(systemConfigApi.saveMyPreference).mockResolvedValue({ userId: 1, settings: {} });
    vi.mocked(useCurrentUserMenusQuery).mockReturnValue({ data: undefined } as ReturnType<typeof useCurrentUserMenusQuery>);
    useAuthStore.setState({ token: null, principal: null });
  });

  it('hides menu items when read permission is missing', () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'admin',
        displayName: 'Admin',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    render(
      <IafThemeProvider>
        <AntApp>
          <MemoryRouter initialEntries={['/platform/users']}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/platform/users" element={<div>content-marker</div>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AntApp>
      </IafThemeProvider>
    );

    expect(screen.getAllByText('用户管理').length).toBeGreaterThan(0);
    expect(screen.queryByText('组织管理')).not.toBeInTheDocument();
    expect(screen.queryByText('角色权限')).not.toBeInTheDocument();
    expect(screen.queryByText('字典参数')).not.toBeInTheDocument();
    expect(screen.queryByText('操作日志')).not.toBeInTheDocument();
    expect(screen.getByLabelText('主题')).toBeInTheDocument();
    expect(screen.getByLabelText('偏好设置')).toBeInTheDocument();
  });

  it('persists reset preferences to backend', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'admin',
        displayName: 'Admin',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    render(
      <IafThemeProvider>
        <AntApp>
          <MemoryRouter initialEntries={['/platform/users']}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/platform/users" element={<div>content-marker</div>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AntApp>
      </IafThemeProvider>
    );

    fireEvent.click(screen.getByLabelText('偏好设置'));
    fireEvent.click(await screen.findByText('恢复默认'));

    await waitFor(() => {
      expect(systemConfigApi.saveMyPreference).toHaveBeenCalledWith({ ...iafDefaultExperienceSettings });
    });
  });

  it('persists sidebar preferences to backend', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'admin',
        displayName: 'Admin',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    render(
      <IafThemeProvider>
        <AntApp>
          <MemoryRouter initialEntries={['/platform/users']}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/platform/users" element={<div>content-marker</div>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AntApp>
      </IafThemeProvider>
    );

    fireEvent.click(screen.getByLabelText('偏好设置'));
    fireEvent.click(await screen.findByLabelText('默认折叠侧栏'));
    fireEvent.click(screen.getByText('保存偏好'));

    await waitFor(() => {
      expect(systemConfigApi.saveMyPreference).toHaveBeenCalledWith(
        expect.objectContaining({
          sidebarCollapsed: true,
          sidebarWidth: iafDefaultExperienceSettings.sidebarWidth
        })
      );
    });
  });

  it('persists direct sidebar collapse interaction to backend', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'admin',
        displayName: 'Admin',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    render(
      <IafThemeProvider>
        <AntApp>
          <MemoryRouter initialEntries={['/platform/users']}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/platform/users" element={<div>content-marker</div>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AntApp>
      </IafThemeProvider>
    );

    fireEvent.click(screen.getByLabelText('折叠侧边栏'));

    await waitFor(() => {
      expect(systemConfigApi.saveMyPreference).toHaveBeenCalledWith(
        expect.objectContaining({
          sidebarCollapsed: true,
          sidebarWidth: iafDefaultExperienceSettings.sidebarWidth
        })
      );
    });
  });

  it('persists direct sidebar resize interaction to backend', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'admin',
        displayName: 'Admin',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    render(
      <IafThemeProvider>
        <AntApp>
          <MemoryRouter initialEntries={['/platform/users']}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/platform/users" element={<div>content-marker</div>} />
              </Route>
            </Routes>
          </MemoryRouter>
        </AntApp>
      </IafThemeProvider>
    );

    fireEvent.mouseDown(screen.getByLabelText('拖拽调整侧边栏宽度'));
    fireEvent.mouseMove(window, { clientX: 320 });
    fireEvent.mouseUp(window);

    await waitFor(() => {
      expect(systemConfigApi.saveMyPreference).toHaveBeenCalledWith(
        expect.objectContaining({
          sidebarCollapsed: false,
          sidebarWidth: 320
        })
      );
    });
  });
});
