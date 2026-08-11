# TASK-0203 规则引擎核心模块开发

## 1. 任务目标

开发基于 JSON Logic 的轻量规则引擎，支持规则定义、规则绑定、规则测试、规则执行日志。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/09_rule_engine_rules.md
- docs/module-specs/platform/04_rule_engine_spec.md

## 3. 本任务实现

- RuleDefinition。
- RuleBinding。
- RuleAction。
- RuleExecutionLog。
- RuleEngineService。
- 规则测试 API。

## 4. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/rules | 规则列表 |
| POST | /api/platform/rules | 新增规则 |
| PUT | /api/platform/rules/{id} | 修改规则 |
| POST | /api/platform/rules/{id}/test | 测试规则 |
| POST | /api/platform/rule-execution/evaluate | 执行规则 |

## 5. 测试

- 简单条件命中。
- and/or 条件命中。
- 不命中返回 false。
- 非法表达式返回错误。
- 执行日志生成。

## 6. 验收标准

- 不允许执行任意脚本。
- 规则执行可追踪。
- 可被 WMS 上架策略复用。
