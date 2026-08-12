import { IafThemeProvider } from '@iaf/theme';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';
import { QmsDrawingReviewWorkbenchPage } from './QmsDrawingReviewWorkbenchPage';

vi.mock('pdfjs-dist', () => ({ GlobalWorkerOptions: {}, getDocument: vi.fn() }));
vi.mock('pdfjs-dist/build/pdf.worker.min.mjs?url', () => ({ default: 'worker.js' }));
vi.mock('./api', () => ({ qmsEngineeringApi: {
  getRevision: vi.fn(), getRevisionFileContent: vi.fn(), getIntermediateModel: vi.fn(), listEvidence: vi.fn()
} }));

const renderPage = () => render(
  <MemoryRouter initialEntries={['/qms/engineering/drawing-revisions/3/review']}>
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <IafThemeProvider><AntApp><Routes><Route path="/qms/engineering/drawing-revisions/:revisionId/review" element={<QmsDrawingReviewWorkbenchPage />} /></Routes></AntApp></IafThemeProvider>
    </QueryClientProvider>
  </MemoryRouter>
);

describe('QmsDrawingReviewWorkbenchPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(qmsEngineeringApi.getRevision).mockResolvedValue({
      id: 3, drawingId: 2, revisionCode: 'C', revisionSeq: 3, fileId: 3, fileType: 'DWG',
      effectiveDate: null, releaseDate: null, supersedesRevisionId: null, parseStatus: 'PENDING',
      reviewStatus: 'PENDING', status: 'UPLOADED', checksum: 'sum', version: 1,
      createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    });
    vi.mocked(qmsEngineeringApi.getRevisionFileContent).mockResolvedValue(new Blob(['dwg']));
    vi.mocked(qmsEngineeringApi.listEvidence).mockResolvedValue([]);
    vi.mocked(qmsEngineeringApi.getIntermediateModel).mockRejectedValue(new Error('missing'));
  });

  it('shows revision status and explicit CAD preview boundary', async () => {
    renderPage();
    expect(await screen.findByText('DWG 在线预览将在 CAD 解析适配器阶段接入。')).toBeInTheDocument();
    expect(screen.getByText('暂无解析证据')).toBeInTheDocument();
    expect(screen.getByText('已上传')).toBeInTheDocument();
  });
});
