# WMS 基础资料与库存模型详细设计

## 1. 目标

建立 WMS 的仓库、库区、库位、库存余额、库存明细、库存事务基础模型，为采购收货、上架、盘点、移库、调整等场景提供统一底座。

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| Warehouse | 仓库 |
| WarehouseArea | 库区 |
| Location | 库位 |
| InventoryBalance | 库存余额 |
| InventoryDetail | 库存明细 |
| InventoryTransaction | 库存事务 |
| LogisticUnit | 物流包装单元 |
| Batch | 批次 |
| SerialNo | 序列号 |

## 3. 数据库表

### wms_warehouse

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| company_id | bigint | 公司 |
| plant_id | bigint | 工厂 |
| warehouse_code | varchar(64) | 仓库编码 |
| warehouse_name | varchar(128) | 仓库名称 |
| warehouse_type | varchar(32) | RAW/FG/WIP/RETURN/VIRTUAL |
| status | varchar(32) | 状态 |
| ext_json | jsonb | 扩展 |

### wms_location

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| warehouse_id | bigint | 仓库 |
| area_id | bigint | 库区 |
| location_code | varchar(64) | 库位编码 |
| location_type | varchar(32) | NORMAL/STAGING/INSPECTION/FROZEN |
| capacity_qty | numeric | 容量 |
| status | varchar(32) | 状态 |

### wms_inventory_balance

库存余额按关键维度汇总：

- tenant_id。
- plant_id。
- warehouse_id。
- location_id。
- material_id。
- batch_no。
- inventory_status。
- owner_id。

数量字段：

- on_hand_qty。
- available_qty。
- allocated_qty。
- frozen_qty。

### wms_inventory_transaction

库存事务必须记录：

- transaction_no。
- transaction_type。
- source_type。
- source_id。
- source_line_id。
- material_id。
- warehouse_id。
- location_id。
- batch_no。
- qty。
- direction：IN/OUT/TRANSFER。
- idempotency_key。

## 4. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/wms/warehouses | 仓库分页 |
| POST | /api/wms/warehouses | 新增仓库 |
| GET | /api/wms/locations | 库位分页 |
| POST | /api/wms/locations | 新增库位 |
| GET | /api/wms/inventory-balances | 库存余额查询 |
| GET | /api/wms/inventory-transactions | 库存事务查询 |

## 5. 库存过账服务

```java
InventoryPostingResult post(InventoryPostingCommand command);
```

规则：

- 必须校验幂等键。
- 必须生成库存事务。
- 必须更新库存余额。
- 必须支持事务回滚。
- 不允许负库存，除非规则启用。

## 6. 测试

- 新增仓库。
- 新增库位。
- 入库过账增加库存。
- 重复幂等键不得重复增加库存。
- 负库存校验。
- 库存事务记录正确。
