# HANDOFF-PLATFORM-FOUNDATION 平台基础能力前后端开发计划

## 1. 目标

在进入 WMS、MES、SRM、QMS 等业务模块之前，先把 IAF 平台管理基础能力建设成可交付、可扩展、可复用的企业系统底座。

本阶段重点不是业务闭环，而是平台底座：

- 菜单、路由、权限、角色、用户、组织完整闭环。
- 页面交互、查看/新增/编辑/操作方式统一。
- 多语言、多主题、品牌配置可用。
- 字典、参数、操作日志等平台基础配置可用。
- 工作流、审批、流程编辑、审批流编辑可用。
- 钉钉、企业微信等企业集成具备基础配置、同步和通知能力。
- Kanban、大屏查看、大屏编辑成为平台能力。

本计划是 handoff 计划，不是正式任务源。正式执行必须使用 `ai-coding/tasks/phase-02/TASK-*.md`。

## 2. 当前基线

已完成：

- 后端平台认证、用户、组织、角色权限 API。
- 前端登录、工作台、用户、组织、角色权限页面。
- 前端多标签工作区基础能力。
- 前端 `api-client`、Mock adapter、`permissions`、`i18n`、`theme`、`table-engine`、`form-engine` 基础能力。
- `./scripts/check-quality.sh` 通过。

仍需补齐：

- 动态菜单和后端菜单管理。
- 菜单、按钮、字段、API 权限的统一授权闭环。
- 平台页面交互和视觉设计规范。
- 主题/品牌/多语言后台配置。
- 字典、参数、操作日志。
- 审批运行时和审批任务前端。
- 流程设计器、审批流设计器。
- 钉钉、企业微信基础集成。
- Kanban 视图。
- 大屏查看和大屏编辑器。

## 3. 执行原则

- 先平台，后 WMS。WMS 业务任务暂缓到平台底座稳定后再启动。
- 后端能力和前端页面必须按同一个任务契约交付。
- 所有新增 API 必须有认证和权限控制，公共接口必须显式说明。
- 所有新增业务表必须包含 `tenant_id`、审计字段、`deleted`、`version`、`ext_json`。
- 前端页面必须使用统一 `api.ts`、`hooks.ts`、`types.ts`、i18n、权限组件、状态组件。
- 标准列表优先使用 `table-engine`，标准表单优先使用 `form-engine`。
- 新增菜单、API、数据库表、前端路由、共享组件后必须更新 code map。

## 4. 推荐任务顺序

1. `TASK-0200_platform_foundation_plan`
2. `TASK-0204_platform_ux_interaction_design_system`
3. `TASK-0205_platform_menu_permission_console`
4. `TASK-0206_platform_dictionary_parameter_audit`
5. `TASK-0207_platform_theme_i18n_branding`
6. `TASK-0201_state_machine_core`
7. `TASK-0202_approval_core`
8. `TASK-0208_approval_task_center_frontend`
9. `TASK-0209_workflow_approval_designer`
10. `TASK-0210_enterprise_integration_dingtalk_wecom`
11. `TASK-0211_platform_kanban`
12. `TASK-0212_dashboard_view_and_designer`
13. `TASK-0203_rule_engine_core`

说明：

- `TASK-0201`、`TASK-0202`、`TASK-0203` 已存在，仍保留，但执行优先级调整到平台底座需要的位置。
- `TASK-0204` 应先于更多页面开发执行，用来统一页面设计和操作方式。
- 审批核心和设计器分开做，避免一次性复杂度过高。
- 钉钉/企业微信第一版只做配置、OAuth/绑定、组织用户同步、消息通知、日志，不做复杂生态能力。

## 5. 阶段验收

完成 Platform Foundation 后，应满足：

- 管理员可以在 UI 中维护菜单、角色、权限、用户、组织。
- 角色授权后，前端菜单、路由、按钮、字段、后端 API 同步受控。
- 平台页面具备统一列表、查看、新增、编辑、详情、操作交互规范。
- 主题、品牌、多语言可在后台配置并在前端生效。
- 字典、参数、操作日志可用。
- 可发起审批，可查看待办/已办，可执行同意、拒绝、退回。
- 可通过设计器维护基础流程/审批配置并发布版本。
- 可配置钉钉/企业微信基础集成，支持用户组织同步和审批通知。
- 可创建 Kanban 看板并展示/拖动卡片。
- 可创建大屏，编辑布局，绑定基础数据源，预览发布。
- `./scripts/check-quality.sh` 通过。

## 6. 暂缓事项

以下事项暂缓到平台底座稳定后：

- WMS 仓库、库存、收货、上架业务开发。
- 移动端 WMS 作业。
- 移动离线运行时。
- AI Assistant 深度业务接入。
- 复杂低代码运行时。
