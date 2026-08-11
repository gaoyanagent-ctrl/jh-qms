# 状态机模块详细设计

## 1. 目标

为所有业务单据提供统一状态流转能力，避免业务模块直接修改状态字段，保证状态迁移可配置、可审计、可校验。

## 2. 状态分层

| 状态字段 | 说明 |
|---|---|
| document_status | 单据生命周期 |
| approval_status | 审批状态 |
| execution_status | 执行状态 |
| posting_status | 过账状态 |
| settlement_status | 结算状态 |

## 3. 核心模型

- StateMachineDefinition。
- StateDefinition。
- StateTransition。
- StateTransitionGuard。
- StateTransitionAction。
- StateTransitionLog。

## 4. 数据库表

### platform_state_machine_def

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| machine_code | varchar(128) | 状态机编码 |
| machine_name | varchar(128) | 名称 |
| business_type | varchar(128) | 业务类型，如 WMS_RECEIPT_ORDER |
| status_field | varchar(64) | 状态字段 |
| version_no | int | 版本 |
| enabled | boolean | 是否启用 |
| ext_json | jsonb | 扩展 |

### platform_state_transition

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| machine_id | bigint | 状态机 |
| from_state | varchar(64) | 来源状态 |
| action_code | varchar(64) | 动作 |
| to_state | varchar(64) | 目标状态 |
| guard_rule_id | bigint | 规则 |
| sort_no | int | 排序 |

### platform_state_transition_log

记录业务对象状态迁移日志。

## 5. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/state-machines | 状态机列表 |
| POST | /api/platform/state-machines | 创建状态机 |
| PUT | /api/platform/state-machines/{id} | 修改状态机 |
| POST | /api/platform/state-machines/{id}/enable | 启用 |
| POST | /api/platform/state-transition/execute | 执行状态迁移 |

## 6. 应用服务

```java
StateTransitionResult execute(StateTransitionCommand command);
List<AvailableAction> getAvailableActions(String businessType, Long businessId);
```

## 7. 领域规则

- 未定义的迁移不得执行。
- guard 不通过不得执行。
- 状态迁移必须记录日志。
- 状态迁移完成后可发布事件。
- 同一业务类型可以有多个状态字段状态机。

## 8. 测试

- 合法状态迁移成功。
- 非法状态迁移失败。
- guard 失败时迁移失败。
- 迁移日志正确记录。
- 可用动作查询正确。
