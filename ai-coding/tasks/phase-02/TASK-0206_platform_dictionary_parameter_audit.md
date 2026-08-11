# TASK-0206 字典、系统参数与操作日志

## 1. 任务目标

实现平台通用字典、系统参数和操作日志能力，为菜单、主题、多语言、审批和业务模块提供基础配置能力。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `docs/code-map/backend.md`
- `docs/code-map/frontend.md`

## 3. 业务范围

本任务实现：

- 字典类型。
- 字典项。
- 系统参数。
- 操作日志记录与查询。
- 字典管理页面。
- 参数配置页面。
- 操作日志查询页面。

本任务不实现：

- 多语言资源后台维护。
- 审批日志。
- 外部集成日志。

## 4. 数据库设计

新增表建议：

- `sys_dict_type`
- `sys_dict_item`
- `sys_config_parameter`
- `sys_operation_log`

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/dict-types` | 字典类型分页 |
| POST | `/api/platform/dict-types` | 新增字典类型 |
| PUT | `/api/platform/dict-types/{id}` | 修改字典类型 |
| GET | `/api/platform/dict-items` | 字典项分页 |
| POST | `/api/platform/dict-items` | 新增字典项 |
| PUT | `/api/platform/dict-items/{id}` | 修改字典项 |
| GET | `/api/platform/parameters` | 参数分页 |
| PUT | `/api/platform/parameters/{id}` | 修改参数 |
| GET | `/api/platform/operation-logs` | 操作日志查询 |

## 6. 权限点

```text
platform:dict:view
platform:dict:create
platform:dict:update
platform:parameter:view
platform:parameter:update
platform:operation-log:view
```

## 7. 前端设计

- 字典类型/字典项使用标准列表和表单。
- 参数配置支持按分组查询。
- 操作日志支持时间、用户、模块、动作、结果查询。

## 8. 测试要求

- 字典编码唯一。
- 参数更新审计。
- 操作日志查询。
- 前端权限按钮隐藏。

## 9. 验收标准

- 平台页面可维护字典和参数。
- 操作日志可查询。
- `./scripts/check-quality.sh` 通过。
