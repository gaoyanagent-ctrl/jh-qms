import { useAuthStore } from '@iaf/auth';
import { QMS_PERMISSIONS } from '@iaf/permissions';
import { IafThemeProvider } from '@iaf/theme';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';
import { QmsPartListPage } from './QmsPartListPage';

vi.mock('./api', () => ({
  qmsEngineeringApi: {
    listParts: vi.fn(),
    getPart: vi.fn(),
    createPart: vi.fn(),
    listDrawings: vi.fn(),
    createDrawing: vi.fn(),
    listRevisions: vi.fn(),
    createRevision: vi.fn()
  }
}));

const renderPage = () => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    <MemoryRouter initialEntries={['/qms/engineering/parts']}>
      <QueryClientProvider client={client}>
        <IafThemeProvider>
          <AntApp><QmsPartListPage /></AntApp>
        </IafThemeProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
};

const setPermissions = (permissions: string[]) => useAuthStore.setState({
  token: 'token',
  principal: { tenantId: 1, userId: 1, username: 'qms-user', displayName: 'QMS User', permissions }
});

describe('QmsPartListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(qmsEngineeringApi.listParts).mockResolvedValue({ records: [], total: 0, pageNo: 1, pageSize: 20 });
    vi.mocked(qmsEngineeringApi.createPart).mockResolvedValue({
      id: 1, orgId: 1, partNo: 'P-1', materialNo: null, partName: 'Bracket', customerId: null,
      vehicleModel: null, supplierId: null, importanceLevel: null, status: 'ACTIVE', version: 0,
      createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    });
  });

  it('shows an empty list and hides create without permission', async () => {
    setPermissions([QMS_PERMISSIONS.partView]);
    renderPage();
    expect(await screen.findByText('工程数据')).toBeInTheDocument();
    expect(await screen.findByText('暂无数据')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /新\s*增/ })).not.toBeInTheDocument();
  });

  it('creates a part when the user has create permission', async () => {
    setPermissions([QMS_PERMISSIONS.partView, QMS_PERMISSIONS.partCreate]);
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: /新\s*增/ }));
    fireEvent.change(await screen.findByLabelText('零件号'), { target: { value: 'P-1' } });
    fireEvent.change(screen.getByLabelText('零件名称'), { target: { value: 'Bracket' } });
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));
    await waitFor(() => expect(qmsEngineeringApi.createPart).toHaveBeenCalled());
    expect(vi.mocked(qmsEngineeringApi.createPart).mock.calls[0][0]).toEqual(expect.objectContaining({ partNo: 'P-1', partName: 'Bracket' }));
  });

  it('shows a recoverable load error', async () => {
    setPermissions([QMS_PERMISSIONS.partView]);
    vi.mocked(qmsEngineeringApi.listParts).mockRejectedValue(new Error('offline'));
    renderPage();
    expect(await screen.findByText('无法加载零件清单，请重试。')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /重\s*试/ })).toBeInTheDocument();
  });
});
