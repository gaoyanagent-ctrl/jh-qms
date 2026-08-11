# 规则引擎模块详细设计

## 1. 目标

为业务人员提供可配置规则能力，支持字段校验、审批触发、审批路由、库存策略、上架策略、消息通知等场景。

## 2. 表达式策略

第一版使用 JSON Logic。禁止业务人员配置任意代码脚本。

## 3. 核心模型

- RuleDefinition。
- RuleCondition。
- RuleAction。
- RuleBinding。
- RuleExecutionContext。
- RuleExecutionResult。
- RuleExecutionLog。

## 4. 数据库表

- platform_rule_def。
- platform_rule_binding。
- platform_rule_action。
- platform_rule_execution_log。

## 5. 规则绑定类型

```text
BUSINESS_ACTION
STATE_TRANSITION
APPROVAL_NODE
FIELD_VALIDATION
PUTAWAY_STRATEGY
MESSAGE_NOTIFICATION
```

## 6. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/rules | 规则列表 |
| POST | /api/platform/rules | 新增规则 |
| PUT | /api/platform/rules/{id} | 修改规则 |
| POST | /api/platform/rules/{id}/test | 测试规则 |
| POST | /api/platform/rule-execution/evaluate | 执行规则 |

## 7. 示例

```json
{
  "and": [
    { ">": [ { "var": "amount" }, 100000 ] },
    { "==": [ { "var": "plantCode" }, "PLANT_01" ] }
  ]
}
```

## 8. 领域规则

- 规则必须有版本。
- 启用状态的规则才能执行。
- 每次执行必须记录日志。
- 规则执行异常不得导致系统崩溃，应返回明确错误。

## 9. 测试

- 简单条件命中。
- 复杂 and/or 条件命中。
- 规则不命中。
- 规则执行日志生成。
- 非法表达式返回错误。
