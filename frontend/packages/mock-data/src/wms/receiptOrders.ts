import { mockSuccess, mockError, MockApiAdapter, MockRequest } from '@iaf/api-client';

let receiptOrders: any[] = [
  {
    id: 1,
    receiptNo: 'REC202607060001',
    documentStatus: 'DRAFT',
    approvalStatus: 'NOT_SUBMITTED',
    executionStatus: 'NOT_STARTED',
    supplierName: 'Industrial Supplies Ltd.',
    eta: '2026-07-07T00:00:00Z',
    itemsCount: 3,
    creatorName: 'System Admin',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    items: [
      { id: 1, materialCode: 'MAT001', materialName: 'Steel Bolt M8', qtyRequired: 100, qtyReceived: 0, uom: 'PCS' },
      { id: 2, materialCode: 'MAT002', materialName: 'Steel Nut M8', qtyRequired: 100, qtyReceived: 0, uom: 'PCS' },
      { id: 3, materialCode: 'MAT003', materialName: 'Metal Washer M8', qtyRequired: 100, qtyReceived: 0, uom: 'PCS' }
    ]
  }
];

export const registerReceiptOrderMocks = (adapter: MockApiAdapter) => {
  // GET /api/wms/receipts
  adapter.register('GET', '/api/wms/receipts', (req: MockRequest) => {
    const keyword = req.queryParams.keyword || '';
    const pageNo = parseInt(req.queryParams.pageNo || '1', 10);
    const pageSize = parseInt(req.queryParams.pageSize || '10', 10);

    const filtered = receiptOrders.filter(o => 
      o.receiptNo.toLowerCase().includes(keyword.toLowerCase()) || 
      o.supplierName.toLowerCase().includes(keyword.toLowerCase())
    );

    const start = (pageNo - 1) * pageSize;
    const records = filtered.slice(start, start + pageSize);

    return mockSuccess({
      records,
      total: filtered.length,
      pageNo,
      pageSize
    });
  });

  // GET /api/wms/receipts/:id
  adapter.register('GET', '/api/wms/receipts/:id', (req: MockRequest) => {
    const id = parseInt(req.pathParams.id, 10);
    const order = receiptOrders.find(o => o.id === id);
    if (!order) {
      return mockError('Receipt order not found', 'RECEIPT_NOT_FOUND', 404);
    }
    return mockSuccess(order);
  });

  // POST /api/wms/receipts
  adapter.register('POST', '/api/wms/receipts', (req: MockRequest) => {
    const body = req.body;
    const newOrder = {
      id: receiptOrders.length + 1,
      receiptNo: `REC${new Date().toISOString().slice(0,10).replace(/-/g,'')}${String(receiptOrders.length + 1).padStart(4,'0')}`,
      documentStatus: 'DRAFT',
      approvalStatus: 'NOT_SUBMITTED',
      executionStatus: 'NOT_STARTED',
      supplierName: body.supplierName,
      eta: body.eta || null,
      itemsCount: body.items ? body.items.length : 0,
      creatorName: 'System Admin',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      items: (body.items || []).map((item: any, idx: number) => ({
        id: idx + 1,
        materialCode: item.materialCode,
        materialName: item.materialName,
        qtyRequired: item.qtyRequired,
        qtyReceived: 0,
        uom: item.uom
      }))
    };
    receiptOrders.push(newOrder);
    return mockSuccess(newOrder);
  });
};
