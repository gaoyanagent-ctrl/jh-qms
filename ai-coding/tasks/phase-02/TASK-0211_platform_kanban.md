# TASK-0211 Kanban 平台能力

## 1. 任务目标

实现平台级 Kanban 能力，为审批任务、项目任务、WMS/MES 作业任务等提供可复用看板视图。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/frontend/10_组件体系规范.md`

## 3. 业务范围

本任务实现：

- Kanban board。
- Kanban column。
- Kanban card。
- 卡片来源配置。
- 视图偏好。
- 拖拽排序。
- 卡片详情抽屉。
- 权限控制。

本任务不实现：

- WMS 专属看板。
- 大屏看板。

## 4. 数据库设计

新增表建议：

- `platform_kanban_board`
- `platform_kanban_column`
- `platform_kanban_card`
- `platform_kanban_view_preference`

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/kanban-boards` | 看板列表 |
| POST | `/api/platform/kanban-boards` | 新增看板 |
| PUT | `/api/platform/kanban-boards/{id}` | 修改看板 |
| GET | `/api/platform/kanban-boards/{id}/cards` | 看板卡片 |
| PUT | `/api/platform/kanban-cards/{id}/move` | 移动卡片 |

## 6. 前端文件

```text
frontend/packages/ui-business/src/kanban/
frontend/apps/pc-admin/src/modules/platform/kanban/
```

## 7. 权限点

```text
platform:kanban:view
platform:kanban:create
platform:kanban:update
platform:kanban:move-card
```

## 8. 验收标准

- 可创建看板。
- 可展示列和卡片。
- 可拖动卡片并保存位置。
- `./scripts/check-quality.sh` 通过。
