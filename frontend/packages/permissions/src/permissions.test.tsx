// @vitest-environment jsdom
import '@testing-library/jest-dom/vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, afterEach } from 'vitest';
import { PermissionGate, PermissionButton, PermissionRoute, PLATFORM_PERMISSION_OPTIONS, PLATFORM_PERMISSIONS, hasAnyPermission, hasPermission, useUserPermissions, useHasPermission } from './index';

afterEach(() => {
  cleanup();
});

vi.mock('@iaf/auth', () => ({
  useAuthStore: (selector: any) => selector({
    principal: { permissions: ['platform:user:create', 'platform:user:view'] }
  })
}));

describe('permissions', () => {
  it('checks single and any permission', () => {
    expect(hasPermission(['platform:user:view'], 'platform:user:view')).toBe(true);
    expect(hasAnyPermission(['platform:user:view'], ['platform:org:view', 'platform:user:view'])).toBe(true);
    expect(hasAnyPermission(['platform:user:view'], ['platform:org:view'])).toBe(false);
  });

  it('keeps platform foundation permission options complete', () => {
    const optionCodes = new Set(PLATFORM_PERMISSION_OPTIONS.map((item) => item.code));

    expect(optionCodes).toEqual(new Set(Object.values(PLATFORM_PERMISSIONS)));
  });

  it('hooks get permissions from store', () => {
    const { result } = renderHook(() => ({
      perms: useUserPermissions(),
      hasCreate: useHasPermission('platform:user:create'),
      hasDelete: useHasPermission('platform:user:delete')
    }));
    expect(result.current.perms).toEqual(['platform:user:create', 'platform:user:view']);
    expect(result.current.hasCreate).toBe(true);
    expect(result.current.hasDelete).toBe(false);
  });

  it('renders children only when permission is present', () => {
    render(
      <PermissionGate require="platform:user:create" fallback={<span>hidden</span>}>
        <span>visible</span>
      </PermissionGate>
    );

    expect(screen.getByText('visible')).toBeInTheDocument();
    expect(screen.queryByText('hidden')).not.toBeInTheDocument();
  });

  it('renders fallback when permission is missing', () => {
    render(
      <PermissionGate require="platform:user:delete" fallback={<span>hidden</span>}>
        <span>visible</span>
      </PermissionGate>
    );

    expect(screen.getByText('hidden')).toBeInTheDocument();
    expect(screen.queryByText('visible')).not.toBeInTheDocument();
  });

  it('renders PermissionButton when permission is present', () => {
    render(
      <PermissionButton require="platform:user:create">
        Create User
      </PermissionButton>
    );
    expect(screen.getByRole('button', { name: 'Create User' })).toBeInTheDocument();
  });

  it('redirects PermissionRoute when permission is missing', () => {
    render(
      <MemoryRouter initialEntries={['/platform/roles']}>
        <Routes>
          <Route path="/" element={<span>fallback-route</span>} />
          <Route element={<PermissionRoute require="platform:role:view" fallbackPath="/" />}>
            <Route path="/platform/roles" element={<span>role-route</span>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('fallback-route')).toBeInTheDocument();
    expect(screen.queryByText('role-route')).not.toBeInTheDocument();
  });
});

// Helper for testing hooks
import { renderHook } from '@testing-library/react';
