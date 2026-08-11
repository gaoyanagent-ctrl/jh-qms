# WMS 采购收货与上架模块详细设计

## 1. 目标

实现 WMS 第一业务闭环：采购订单/到货信息 -> 收货单 -> 收货确认 -> 生成待上架库存 -> 上架任务 -> 上架确认 -> 库存过账。

## 2. 边界

第一版实现：

- 收货单主子表。
- 收货确认。
- 上架任务生成。
- 简单上架策略。
- 库存过账。

第一版不实现：

- 完整质检流程。
- ERP 自动同步。
- PDA 原生 App。
- 复杂容器包装。

## 3. 收货单表

### wms_receipt_order

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| company_id | bigint | 公司 |
| plant_id | bigint | 工厂 |
| document_no | varchar(64) | 单据号 |
| supplier_id | bigint | 供应商 |
| warehouse_id | bigint | 仓库 |
| receipt_date | date | 收货日期 |
| document_status | varchar(32) | 单据状态 |
| approval_status | varchar(32) | 审批状态 |
| receipt_status | varchar(32) | 收货状态 |
| posting_status | varchar(32) | 过账状态 |
| source_type | varchar(64) | 来源类型 |
| source_id | bigint | 来源单据 |
| remark | varchar(512) | 备注 |
| ext_json | jsonb | 扩展 |

### wms_receipt_order_line

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| receipt_order_id | bigint | 主表 |
| line_no | int | 行号 |
| material_id | bigint | 物料 |
| expected_qty | numeric | 应收数量 |
| received_qty | numeric | 实收数量 |
| unit_id | bigint | 单位 |
| batch_no | varchar(64) | 批次 |
| inventory_status | varchar(32) | 库存状态 |
| location_id | bigint | 暂存库位 |

## 4. 上架任务表

### wms_putaway_task

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| task_no | varchar(64) | 任务号 |
| receipt_order_id | bigint | 收货单 |
| receipt_line_id | bigint | 收货行 |
| material_id | bigint | 物料 |
| qty | numeric | 数量 |
| from_location_id | bigint | 来源暂存库位 |
| suggested_location_id | bigint | 推荐目标库位 |
| actual_location_id | bigint | 实际目标库位 |
| task_status | varchar(32) | OPEN/PROCESSING/DONE/CANCELLED |

## 5. 状态规则

收货单 document_status：

```text
DRAFT -> SUBMITTED -> EFFECTIVE -> CLOSED
DRAFT -> CANCELLED
```

receipt_status：

```text
NOT_RECEIVED -> PARTIAL_RECEIVED -> FULL_RECEIVED
```

posting_status：

```text
NOT_POSTED -> POSTED -> REVERSED
```

上架任务 task_status：

```text
OPEN -> PROCESSING -> DONE
OPEN -> CANCELLED
```

## 6. API

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

## 7. 上架策略

第一版策略：

1. 物料固定库位优先。
2. 同物料集中存放。
3. 空库位优先。
4. 物料分类默认库区。

策略必须通过 RuleEngineService 或 PutawayStrategyService 执行，不允许写死在 Controller。

## 8. 领域规则

- 草稿收货单才允许修改。
- 已作废收货单不允许任何业务动作。
- 实收数量不能小于 0。
- 实收数量不能大于应收数量，除非启用超收规则。
- 确认收货必须生成库存事务。
- 确认收货必须生成上架任务。
- 上架确认必须变更库存位置。
- 所有库存变化必须通过 InventoryPostingService。

## 9. 事件

- ReceiptOrderSubmittedEvent。
- ReceiptConfirmedEvent。
- PutawayTaskCreatedEvent。
- PutawayConfirmedEvent。

## 10. 测试

- 创建收货单。
- 修改草稿收货单。
- 非草稿修改失败。
- 确认收货生成库存事务。
- 确认收货生成上架任务。
- 上架确认移动库存。
- 上架策略返回推荐库位。
