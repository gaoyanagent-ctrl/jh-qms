import { mockError, mockSuccess, MockApiAdapter, type MockRequest } from '@iaf/api-client';
import type { PageResult, QmsDrawing, QmsDrawingRevision, QmsPart } from '@iaf/domain-types';

const now = '2026-08-12T08:00:00Z';

const parts: QmsPart[] = [
  { id: 1001, orgId: 1, partNo: 'JH-BRK-001', materialNo: 'MAT-6082', partName: 'Front suspension bracket', customerId: 101, vehicleModel: 'JH-X7', supplierId: 201, importanceLevel: 'A', status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now },
  { id: 1002, orgId: 1, partNo: 'JH-SHAFT-014', materialNo: 'MAT-7140', partName: 'Transmission input shaft', customerId: 102, vehicleModel: 'JH-V3', supplierId: 202, importanceLevel: 'B', status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now }
];

const drawings: QmsDrawing[] = [
  { id: 2001, partId: 1001, drawingNo: 'DWG-BRK-001', drawingName: 'Suspension bracket assembly', drawingType: 'ASSEMBLY', sourceSystem: 'PLM', status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now },
  { id: 2002, partId: 1001, drawingNo: 'DWG-BRK-001-A', drawingName: 'Bracket stamping part', drawingType: 'PART', sourceSystem: 'MANUAL', status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now }
];

const revisions: QmsDrawingRevision[] = [
  { id: 3001, drawingId: 2001, revisionCode: 'A', revisionSeq: 1, fileId: null, fileType: null, effectiveDate: '2026-08-01', releaseDate: null, supersedesRevisionId: null, parseStatus: 'PENDING', reviewStatus: 'PENDING', status: 'DRAFT', checksum: null, version: 0, createdAt: now, updatedAt: now }
];

export const registerQmsEngineeringMocks = (adapter: MockApiAdapter) => {
  adapter.register('GET', '/api/qms/parts', (req: MockRequest) => {
    const keyword = String(req.queryParams?.keyword ?? '').trim().toLowerCase();
    const pageNo = Math.max(1, Number(req.queryParams?.pageNo ?? 1));
    const pageSize = Math.max(1, Number(req.queryParams?.pageSize ?? 20));
    const filtered = keyword
      ? parts.filter((part) => [part.partNo, part.partName, part.materialNo, part.vehicleModel].some((value) => value?.toLowerCase().includes(keyword)))
      : parts;
    const data: PageResult<QmsPart> = {
      records: filtered.slice((pageNo - 1) * pageSize, pageNo * pageSize),
      total: filtered.length,
      pageNo,
      pageSize
    };
    return mockSuccess(data);
  });

  adapter.register('GET', '/api/qms/parts/:id', (req: MockRequest) => {
    const part = parts.find((item) => item.id === Number(req.pathParams.id));
    return part ? mockSuccess(part) : mockError('Part not found', 'QMS_PART_NOT_FOUND', 404);
  });

  adapter.register('POST', '/api/qms/parts', (req: MockRequest) => {
    if (parts.some((part) => part.partNo === req.body.partNo)) return mockError('Part number already exists', 'QMS_PART_ALREADY_EXISTS', 409);
    const part: QmsPart = {
      id: Math.max(...parts.map((item) => item.id)) + 1,
      orgId: 1,
      partNo: String(req.body.partNo),
      materialNo: req.body.materialNo ?? null,
      partName: String(req.body.partName),
      customerId: req.body.customerId ?? null,
      vehicleModel: req.body.vehicleModel ?? null,
      supplierId: req.body.supplierId ?? null,
      importanceLevel: req.body.importanceLevel ?? null,
      status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now
    };
    parts.unshift(part);
    return mockSuccess(part);
  });

  adapter.register('GET', '/api/qms/parts/:partId/drawings', (req: MockRequest) =>
    mockSuccess(drawings.filter((drawing) => drawing.partId === Number(req.pathParams.partId))));

  adapter.register('POST', '/api/qms/parts/:partId/drawings', (req: MockRequest) => {
    const partId = Number(req.pathParams.partId);
    const drawing: QmsDrawing = {
      id: Math.max(2000, ...drawings.map((item) => item.id)) + 1,
      partId,
      drawingNo: String(req.body.drawingNo),
      drawingName: String(req.body.drawingName),
      drawingType: req.body.drawingType,
      sourceSystem: req.body.sourceSystem ?? 'MANUAL',
      status: 'ACTIVE', version: 0, createdAt: now, updatedAt: now
    };
    drawings.push(drawing);
    return mockSuccess(drawing);
  });

  adapter.register('GET', '/api/qms/drawings/:drawingId/revisions', (req: MockRequest) =>
    mockSuccess(revisions.filter((revision) => revision.drawingId === Number(req.pathParams.drawingId))));

  adapter.register('POST', '/api/qms/drawings/:drawingId/revisions', (req: MockRequest) => {
    const drawingId = Number(req.pathParams.drawingId);
    const drawingRevisions = revisions.filter((revision) => revision.drawingId === drawingId);
    const revision: QmsDrawingRevision = {
      id: Math.max(3000, ...revisions.map((item) => item.id)) + 1,
      drawingId,
      revisionCode: String(req.body.revisionCode),
      revisionSeq: drawingRevisions.length + 1,
      fileId: null,
      fileType: null,
      effectiveDate: req.body.effectiveDate ?? null,
      releaseDate: null,
      supersedesRevisionId: req.body.supersedesRevisionId ?? null,
      parseStatus: 'PENDING', reviewStatus: 'PENDING', status: 'DRAFT', checksum: null,
      version: 0, createdAt: now, updatedAt: now
    };
    revisions.push(revision);
    return mockSuccess(revision);
  });
};
