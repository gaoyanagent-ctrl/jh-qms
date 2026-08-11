# TASK-0212 大屏查看与大屏编辑器

## 1. 任务目标

实现平台级大屏查看和大屏编辑器，支持拖拽组件、绑定基础数据源、预览、发布和全屏展示。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/frontend/10_组件体系规范.md`

## 3. 业务范围

本任务实现：

- 大屏定义。
- 大屏组件。
- 数据集配置。
- 大屏主题。
- 大屏发布版本。
- 大屏查看端。
- 大屏编辑器。

本任务不实现：

- 复杂 BI 查询引擎。
- 实时流处理。
- 租户级商业模板市场。

## 4. 数据库设计

新增表建议：

- `platform_dashboard`
- `platform_dashboard_widget`
- `platform_dashboard_dataset`
- `platform_dashboard_theme`
- `platform_dashboard_version`

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/dashboards` | 大屏列表 |
| POST | `/api/platform/dashboards` | 新增大屏 |
| PUT | `/api/platform/dashboards/{id}` | 保存大屏 |
| POST | `/api/platform/dashboards/{id}/publish` | 发布 |
| GET | `/api/platform/dashboards/{id}/view` | 查看发布版本 |
| GET | `/api/platform/dashboard-datasets` | 数据集列表 |

## 6. 前端设计

新增应用或模块：

```text
frontend/apps/dashboard-view/
frontend/apps/pc-admin/src/modules/platform/dashboard-designer/
frontend/packages/ui-business/src/dashboard/
```

第一版组件：

- 指标卡。
- 折线图。
- 柱状图。
- 饼图。
- 表格。
- 排行榜。
- 文本。
- 图片。
- iframe。

## 7. 权限点

```text
platform:dashboard:view
platform:dashboard:create
platform:dashboard:update
platform:dashboard:publish
platform:dashboard-designer:view
```

## 8. 验收标准

- 可创建大屏。
- 可拖拽组件。
- 可绑定 Mock/基础数据源。
- 可预览和发布。
- 可全屏查看。
- `./scripts/check-quality.sh` 通过。
