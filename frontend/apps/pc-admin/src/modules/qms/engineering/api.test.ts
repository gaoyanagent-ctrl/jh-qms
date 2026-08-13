import { apiClient } from '../../../api/client';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';

describe('qmsEngineeringApi', () => {
  beforeEach(() => vi.restoreAllMocks());

  it('uses the TASK-0401 part list contract', async () => {
    const get = vi.spyOn(apiClient, 'get').mockResolvedValue({ records: [], total: 0, pageNo: 2, pageSize: 50 });
    await qmsEngineeringApi.listParts({ keyword: 'BRK', pageNo: 2, pageSize: 50 });
    expect(get).toHaveBeenCalledWith('/api/qms/parts', { query: { keyword: 'BRK', pageNo: 2, pageSize: 50 } });
  });

  it('uses nested drawing and revision paths with unchanged request bodies', async () => {
    const post = vi.spyOn(apiClient, 'post').mockResolvedValue({});
    const drawing = { drawingNo: 'D-1', drawingName: 'Drawing', drawingType: 'PART' as const, sourceSystem: 'MANUAL' as const };
    const revision = { revisionCode: 'A', effectiveDate: '2026-08-12', supersedesRevisionId: null };

    await qmsEngineeringApi.createDrawing(12, drawing);
    await qmsEngineeringApi.createRevision(34, revision);

    expect(post).toHaveBeenNthCalledWith(1, '/api/qms/parts/12/drawings', drawing);
    expect(post).toHaveBeenNthCalledWith(2, '/api/qms/drawings/34/revisions', revision);
  });

  it('uses controlled inspection standard action endpoints', async () => {
    const post = vi.spyOn(apiClient, 'post').mockResolvedValue({});
    await qmsEngineeringApi.actOnInspectionStandard(5, 8, 'submit-approval', 'ready');
    await qmsEngineeringApi.actOnInspectionStandard(5, 8, 'release', 'publish');
    expect(post).toHaveBeenNthCalledWith(1, '/api/qms/drawing-revisions/5/inspection-standard/8/submit-approval', { comment: 'ready' });
    expect(post).toHaveBeenNthCalledWith(2, '/api/qms/drawing-revisions/5/inspection-standard/8/release', { comment: 'publish' });
  });
});
