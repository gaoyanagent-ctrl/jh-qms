import { useAuthStore } from '@iaf/auth';
import { QMS_PERMISSIONS } from '@iaf/permissions';
import { IafThemeProvider } from '@iaf/theme';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';
import { QmsPartDetailPage } from './QmsPartDetailPage';

vi.mock('./api', () => ({
  qmsEngineeringApi: {
    listParts: vi.fn(),
    getPart: vi.fn(),
    createPart: vi.fn(),
    listDrawings: vi.fn(),
    createDrawing: vi.fn(),
    listRevisions: vi.fn(),
    createRevision: vi.fn(),
    listLatestParseJobs: vi.fn(),
    retryParseJob: vi.fn(),
    uploadRevisionFile: vi.fn(),
    getRevision: vi.fn()
  }
}));

const renderPage = () => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    <MemoryRouter initialEntries={['/qms/engineering/parts/1001']}>
      <QueryClientProvider client={client}>
        <IafThemeProvider>
          <AntApp>
            <Routes><Route path="/qms/engineering/parts/:partId" element={<QmsPartDetailPage />} /></Routes>
          </AntApp>
        </IafThemeProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
};

describe('QmsPartDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1, userId: 1, username: 'engineer', displayName: 'Engineer',
        permissions: Object.values(QMS_PERMISSIONS)
      }
    });
    vi.mocked(qmsEngineeringApi.getPart).mockResolvedValue({
      id: 1001, orgId: 1, partNo: 'JH-BRK-001', materialNo: 'MAT-6082', partName: 'Bracket',
      customerId: null, vehicleModel: 'JH-X7', supplierId: null, importanceLevel: 'A', status: 'ACTIVE',
      version: 0, createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    });
    vi.mocked(qmsEngineeringApi.listDrawings).mockResolvedValue([{
      id: 2001, partId: 1001, drawingNo: 'DWG-BRK-001', drawingName: 'Bracket drawing', drawingType: 'PART',
      sourceSystem: 'MANUAL', status: 'ACTIVE', version: 0, createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    }]);
    vi.mocked(qmsEngineeringApi.listRevisions).mockResolvedValue([{
      id: 3001, drawingId: 2001, revisionCode: 'A', revisionSeq: 1, fileId: null, fileType: null,
      effectiveDate: '2026-08-01', releaseDate: null, supersedesRevisionId: null, parseStatus: 'PENDING',
      reviewStatus: 'PENDING', status: 'DRAFT', checksum: null, version: 0,
      createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    }]);
    vi.mocked(qmsEngineeringApi.listLatestParseJobs).mockResolvedValue([]);
    vi.mocked(qmsEngineeringApi.createDrawing).mockResolvedValue({} as never);
    vi.mocked(qmsEngineeringApi.createRevision).mockResolvedValue({} as never);
  });

  it('shows the latest parse attempt and retries a failed job', async () => {
    vi.mocked(qmsEngineeringApi.listLatestParseJobs).mockResolvedValue([{
      id: 4001, revisionId: 3001, attemptNo: 2, status: 'FAILED', parserType: 'PDF',
      errorCode: 'PARSER_ERROR', errorMessage: 'Parser unavailable',
      createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    }]);
    vi.mocked(qmsEngineeringApi.retryParseJob).mockResolvedValue({} as never);
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '重试解析' }));
    await waitFor(() => expect(qmsEngineeringApi.retryParseJob).toHaveBeenCalledWith(3001));
    expect(screen.getByText('#2')).toBeInTheDocument();
  });

  it('renders the part, drawing, and selected drawing revision hierarchy', async () => {
    renderPage();
    expect(await screen.findByText('JH-BRK-001 · Bracket')).toBeInTheDocument();
    expect(await screen.findByText('DWG-BRK-001')).toBeInTheDocument();
    expect((await screen.findAllByText('A')).length).toBeGreaterThan(0);
    expect(screen.getByRole('button', { name: '上传文件' })).toBeEnabled();
  });

  it('keeps the upload action visible but disabled without upload permission', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: {
        tenantId: 1, userId: 2, username: 'viewer', displayName: 'Viewer',
        permissions: Object.values(QMS_PERMISSIONS).filter((permission) => permission !== QMS_PERMISSIONS.drawingRevisionUpload)
      }
    });
    renderPage();
    expect(await screen.findByRole('button', { name: '上传文件' })).toBeDisabled();
  });

  it('creates a drawing through the permission-aware form', async () => {
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: /新\s*增\s*图\s*纸/ }));
    fireEvent.change(await screen.findByLabelText('图号'), { target: { value: 'DWG-NEW' } });
    fireEvent.change(screen.getByLabelText('图纸名称'), { target: { value: 'New drawing' } });
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));
    await waitFor(() => expect(qmsEngineeringApi.createDrawing).toHaveBeenCalled());
    expect(vi.mocked(qmsEngineeringApi.createDrawing).mock.calls[0][0]).toBe(1001);
    expect(vi.mocked(qmsEngineeringApi.createDrawing).mock.calls[0][1]).toEqual(expect.objectContaining({ drawingNo: 'DWG-NEW' }));
  });

  it('creates a metadata-only revision for the selected drawing', async () => {
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: /新\s*增\s*版\s*本/ }));
    fireEvent.change(await screen.findByLabelText('版本号'), { target: { value: 'B' } });
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));
    await waitFor(() => expect(qmsEngineeringApi.createRevision).toHaveBeenCalled());
    expect(vi.mocked(qmsEngineeringApi.createRevision).mock.calls[0][0]).toBe(2001);
    expect(vi.mocked(qmsEngineeringApi.createRevision).mock.calls[0][1]).toEqual(expect.objectContaining({ revisionCode: 'B' }));
  });

  it('does not request child data without its independent view permissions', async () => {
    useAuthStore.setState({
      token: 'token',
      principal: { tenantId: 1, userId: 2, username: 'part-viewer', displayName: 'Part Viewer', permissions: [QMS_PERMISSIONS.partView] }
    });
    renderPage();
    expect(await screen.findByText('当前账号无权查看图纸数据。')).toBeInTheDocument();
    expect(screen.getByText('当前账号无权查看版本历史。')).toBeInTheDocument();
    expect(qmsEngineeringApi.listDrawings).not.toHaveBeenCalled();
    expect(qmsEngineeringApi.listRevisions).not.toHaveBeenCalled();
  });
});
