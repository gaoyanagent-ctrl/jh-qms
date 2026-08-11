# TASK-0210 钉钉与企业微信基础集成

## 1. 任务目标

实现钉钉和企业微信基础集成能力，支持应用配置、OAuth/账号绑定、组织用户同步、消息通知和集成日志。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/module-specs/platform/05_outbox_integration_spec.md`

## 3. 业务范围

本任务实现：

- 集成应用配置。
- 钉钉配置。
- 企业微信配置。
- OAuth 登录/绑定预留。
- 用户同步。
- 部门同步。
- 消息通知。
- Webhook 事件接收。
- 集成日志。
- 前端集成配置页面。

本任务不实现：

- 复杂低代码连接器。
- ERP 集成。
- 开放平台应用市场。

## 4. 数据库设计

新增表建议：

- `platform_integration_app`
- `platform_integration_account_binding`
- `platform_integration_sync_job`
- `platform_integration_event_log`
- `platform_integration_message_log`

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/integrations/apps` | 集成应用列表 |
| POST | `/api/platform/integrations/apps` | 新增配置 |
| PUT | `/api/platform/integrations/apps/{id}` | 修改配置 |
| POST | `/api/platform/integrations/apps/{id}/sync-users` | 同步用户组织 |
| GET | `/api/platform/integrations/logs` | 集成日志 |
| POST | `/api/platform/integrations/webhooks/{provider}` | Webhook 接收 |

## 6. 权限点

```text
platform:integration:view
platform:integration:create
platform:integration:update
platform:integration:sync
platform:integration-log:view
```

## 7. 验收标准

- 可配置钉钉和企业微信应用参数。
- 可触发用户/部门同步。
- 可发送基础通知。
- 可查询同步和消息日志。
- `./scripts/check-quality.sh` 通过。
