import { IafThemeProvider } from '@iaf/theme';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';
import { QmsDrawingReviewWorkbenchPage } from './QmsDrawingReviewWorkbenchPage';

vi.mock('pdfjs-dist', () => ({ GlobalWorkerOptions: {}, getDocument: vi.fn() }));
vi.mock('pdfjs-dist/build/pdf.worker.min.mjs?url', () => ({ default: 'worker.js' }));
vi.mock('./api', () => ({ qmsEngineeringApi: {
  getRevision: vi.fn(), getRevisionFileContent: vi.fn(), getIntermediateModel: vi.fn(), listEvidence: vi.fn(),
  listCharacteristics: vi.fn(), confirmCharacteristic: vi.fn(), rejectCharacteristic: vi.fn()
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
    vi.mocked(qmsEngineeringApi.listCharacteristics).mockResolvedValue([]);
    vi.mocked(qmsEngineeringApi.getIntermediateModel).mockRejectedValue(new Error('missing'));
  });

  it('shows revision status and explicit CAD preview boundary', async () => {
    renderPage();
    expect(await screen.findByText('DWG 在线预览将在 CAD 解析适配器阶段接入。')).toBeInTheDocument();
    expect(screen.getByText('暂无解析证据')).toBeInTheDocument();
    expect(screen.getByText('已上传')).toBeInTheDocument();
    await waitFor(() => {
      expect(qmsEngineeringApi.getIntermediateModel).not.toHaveBeenCalled();
      expect(qmsEngineeringApi.listEvidence).not.toHaveBeenCalled();
    });
  });

  it('keeps characteristic text in a full-width block instead of sharing a row with actions', async () => {
    vi.mocked(qmsEngineeringApi.getRevision).mockResolvedValue({
      id: 3, drawingId: 2, revisionCode: 'C', revisionSeq: 3, fileId: 3, fileType: 'DWG',
      effectiveDate: null, releaseDate: null, supersedesRevisionId: null, parseStatus: 'SUCCESS',
      reviewStatus: 'PENDING', status: 'PARSED', checksum: 'sum', version: 1,
      createdAt: '2026-08-12T00:00:00Z', updatedAt: '2026-08-12T00:00:00Z'
    });
    vi.mocked(qmsEngineeringApi.getIntermediateModel).mockResolvedValue({
      id: 1, revisionId: 3, parseJobId: 1, schemaVersion: '1.0.0', documentId: '3', revisionCode: 'C',
      model: { schemaVersion: '1.0.0', documentId: '3', revision: 'C', sheets: [] }, createdAt: '2026-08-12T00:00:00Z'
    });
    vi.mocked(qmsEngineeringApi.listCharacteristics).mockResolvedValue([{
      id: 1, partId: 2, drawingRevisionId: 3, sourceEntityId: 'dwg-1', evidenceId: 1,
      characteristicCode: 'DIM-EV-1410', characteristicType: 'DIMENSION', name: '34', nominalValue: 34,
      upperTolerance: null, lowerTolerance: null, upperLimit: null, lowerLimit: null, unit: 'mm',
      specialCharacteristicCode: null, confidence: 1, status: 'ACTIVE', reviewStatus: 'PENDING',
      reviewedBy: null, reviewedAt: null, reviewComment: null, version: 0
    }]);

    renderPage();

    expect(await screen.findByText('DIM-EV-1410')).toBeInTheDocument();
    const content = screen.getByTestId('characteristic-list-content');
    expect(content.style.width).toBe('100%');
    expect(content.style.minWidth).toBe('0');
    expect(content).toHaveTextContent('34 · 34 mm (- / -)');
  });
});
