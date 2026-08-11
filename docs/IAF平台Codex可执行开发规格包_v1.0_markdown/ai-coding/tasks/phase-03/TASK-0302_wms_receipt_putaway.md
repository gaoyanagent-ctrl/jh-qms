# TASK-0302 WMS 采购收货与上架闭环开发

## 1. 任务目标

开发 WMS 采购收货与上架闭环，支持收货单创建、提交、确认收货、生成上架任务、上架推荐、上架确认和库存过账。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/02_backend_rules.md
- ai-coding/rules/03_frontend_rules.md
- ai-coding/rules/08_state_machine_rules.md
- docs/module-specs/wms/02_wms_receipt_putaway_spec.md
- docs/module-specs/wms/01_wms_master_inventory_spec.md

## 3. 本任务实现

- wms_receipt_order。
- wms_receipt_order_line。
- wms_putaway_task。
- ReceiptOrderApplicationService。
- PutawayTaskApplicationService。
- PutawayStrategyService。
- 收货单页面。
- 上架任务页面。

## 4. 本任务不实现

- PDA 原生应用。
- 完整质检流程。
- ERP 自动同步。
- 复杂包装单元。

## 5. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/wms/receipt-orders | 收货单分页 |
| GET | /api/wms/receipt-orders/{id} | 收货单详情 |
| POST | /api/wms/receipt-orders | 新增收货单 |
| PUT | /api/wms/receipt-orders/{id} | 修改收货单 |
| POST | /api/wms/receipt-orders/{id}/submit | 提交 |
| POST | /api/wms/receipt-orders/{id}/confirm-receipt | 确认收货 |
| POST | /api/wms/receipt-orders/{id}/cancel | 作废 |
| GET | /api/wms/putaway-tasks | 上架任务分页 |
| POST | /api/wms/putaway-tasks/{id}/confirm | 确认上架 |

## 6. 权限点

```text
wms:receipt:view
wms:receipt:create
wms:receipt:update
wms:receipt:submit
wms:receipt:confirm
wms:receipt:cancel
wms:putaway:view
wms:putaway:confirm
```

## 7. 领域规则

- 草稿收货单才允许修改。
- 已作废单据不允许确认收货。
- 实收数量不能小于 0。
- 实收数量不能大于应收数量，除非超收规则启用。
- 确认收货必须调用 InventoryPostingService。
- 确认收货必须生成 ReceiptConfirmedEvent。
- 确认收货必须生成上架任务。
- 上架确认必须通过库存过账移动库存位置。

## 8. 前端页面

```text
src/modules/wms/receipt/ReceiptOrderList.tsx
src/modules/wms/receipt/ReceiptOrderForm.tsx
src/modules/wms/receipt/ReceiptOrderDetail.tsx
src/modules/wms/putaway/PutawayTaskList.tsx
src/modules/wms/putaway/PutawayTaskDetail.tsx
```

## 9. 测试

- 创建收货单。
- 修改草稿。
- 非草稿修改失败。
- 提交收货单。
- 确认收货生成库存事务。
- 确认收货生成上架任务。
- 上架确认移动库存。
- 重复确认收货幂等处理。

## 10. 验收标准

- 收货到上架闭环可跑通。
- 库存余额和库存事务一致。
- 状态机日志完整。
- 权限按钮按状态显示。
- 后端测试通过。
- 前端构建通过。
