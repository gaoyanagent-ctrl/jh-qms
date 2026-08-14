import { useAuthStore } from '@iaf/auth';
import { PLATFORM_PERMISSIONS } from '@iaf/permissions';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { UserListPage } from './UserListPage';

vi.mock('antd', async () => {
  const actual = await vi.importActual<typeof import('antd')>('antd');
  return {
    ...actual,
    Modal: ({ open, children }: { open?: boolean; children?: ReactNode }) => (open ? <div>{children}</div> : null),
    Table: () => <div data-testid="user-table" />
  };
});

vi.mock('./api', () => ({
  usersApi: {
    listUsers: vi.fn(async () => ({ records: [], total: 0, pageNo: 1, pageSize: 10 })),
    createUser: vi.fn(),
    updateUser: vi.fn(),
    getUserOrganizations: vi.fn(async () => ({ userId: 1, primaryOrgId: null, organizations: [] })),
    assignUserOrganizations: vi.fn(),
    switchUserOrgContext: vi.fn(),
    disableUser: vi.fn(),
    resetPassword: vi.fn()
  }
}));

vi.mock('../orgs/hooks', () => ({
  useOrgTreeQuery: () => ({ data: [], isLoading: false })
}));

const renderPage = () => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <UserListPage />
      </QueryClientProvider>
    </MemoryRouter>
  );
};

describe('UserListPage', () => {
  it('hides create action without create permission', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'viewer',
        displayName: 'Viewer',
        permissions: [PLATFORM_PERMISSIONS.userView]
      }
    });

    renderPage();

    await waitFor(() => expect(screen.getByText('用户管理')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: /新\s*增/ })).not.toBeInTheDocument();
  });

  it('shows create action with create permission', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1,
        userId: 1,
        username: 'operator',
        displayName: 'Operator',
        permissions: [PLATFORM_PERMISSIONS.userView, PLATFORM_PERMISSIONS.userCreate]
      }
    });

    renderPage();

    await waitFor(() => expect(screen.getAllByRole('button', { name: /新\s*增/ }).length).toBeGreaterThan(0));
    fireEvent.click(screen.getAllByRole('button', { name: /新\s*增/ })[0]);
    expect(await screen.findByText('主组织')).toBeInTheDocument();
  });
});
