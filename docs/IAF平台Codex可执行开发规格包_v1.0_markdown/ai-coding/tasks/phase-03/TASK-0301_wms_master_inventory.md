# TASK-0301 WMS 基础资料与库存核心开发

## 1. 任务目标

开发 WMS 仓库、库区、库位、库存余额、库存事务基础能力，为后续收货和上架提供库存底座。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/02_backend_rules.md
- ai-coding/rules/04_database_rules.md
- ai-coding/rules/06_permission_rules.md
- docs/module-specs/wms/01_wms_master_inventory_spec.md

## 3. 本任务实现

- wms_warehouse。
- wms_warehouse_area。
- wms_location。
- wms_inventory_balance。
- wms_inventory_transaction。
- InventoryPostingService。
- 仓库/库位页面。
- 库存余额/事务查询页面。

## 4. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/wms/warehouses | 仓库分页 |
| POST | /api/wms/warehouses | 新增仓库 |
| PUT | /api/wms/warehouses/{id} | 修改仓库 |
| GET | /api/wms/locations | 库位分页 |
| POST | /api/wms/locations | 新增库位 |
| GET | /api/wms/inventory-balances | 库存余额 |
| GET | /api/wms/inventory-transactions | 库存事务 |

## 5. 权限点

```text
wms:warehouse:view
wms:warehouse:create
wms:warehouse:update
wms:location:view
wms:location:create
wms:location:update
wms:inventory:view
wms:inventory-transaction:view
```

## 6. 领域规则

- warehouse_code 在同一 tenant 下唯一。
- location_code 在同一 warehouse 下唯一。
- 库存过账必须具备 idempotency_key。
- 库存变更必须生成事务。
- 默认不允许负库存。

## 7. 测试

- 创建仓库。
- 创建库位。
- 重复库位编码失败。
- 入库过账增加库存。
- 重复幂等键不重复增加库存。
- 事务记录正确。

## 8. 验收标准

- WMS 库存不能被其他服务直接修改。
- 库存事务和余额一致。
- 后端测试通过。
- 前端查询页面可访问。
