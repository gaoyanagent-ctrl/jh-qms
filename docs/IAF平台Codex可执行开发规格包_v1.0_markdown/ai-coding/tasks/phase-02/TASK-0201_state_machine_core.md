# TASK-0201 状态机核心模块开发

## 1. 任务目标

开发平台状态机核心能力，支持业务对象状态机定义、状态迁移、迁移校验、迁移日志和可用动作查询。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/08_state_machine_rules.md
- docs/module-specs/platform/02_state_machine_spec.md

## 3. 本任务实现

- 状态机定义表。
- 状态迁移表。
- 状态迁移日志表。
- StateMachineService。
- 状态迁移 API。
- 可用动作查询 API。

## 4. 本任务不实现

- 可视化状态机设计器。
- 复杂脚本 guard。
- 与审批流深度集成。

## 5. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/state-machines | 状态机列表 |
| POST | /api/platform/state-machines | 创建状态机 |
| POST | /api/platform/state-transition/execute | 执行迁移 |
| GET | /api/platform/state-transition/available-actions | 查询可用动作 |

## 6. 核心服务

```java
StateTransitionResult execute(StateTransitionCommand command);
List<AvailableAction> getAvailableActions(AvailableActionQuery query);
```

## 7. 测试

- 合法迁移成功。
- 非法迁移失败。
- guard 不通过失败。
- 日志记录成功。
- 可用动作正确。

## 8. 验收标准

- 状态迁移不允许绕过服务。
- 迁移日志完整。
- 后续 WMS 收货单可复用。
