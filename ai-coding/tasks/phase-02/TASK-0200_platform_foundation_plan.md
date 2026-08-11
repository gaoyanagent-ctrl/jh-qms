# TASK-0200 平台基础能力总计划

## 1. 任务目标

建立 Platform Foundation 阶段的正式执行边界，明确在 WMS 业务开发前必须完成的平台管理基础能力。

## 2. 必须先阅读

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/operations/HANDOFF-PLATFORM-FOUNDATION_execution_plan.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/frontend.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/quality/quality_gate.md`

## 3. 业务范围

本阶段实现：

- 平台页面交互与视觉设计规范。
- 动态菜单、路由、按钮、字段、API 权限闭环。
- 字典、参数、操作日志。
- 多主题、多语言、品牌配置。
- 状态机核心。
- 审批核心、审批任务中心。
- 流程编辑器、审批流编辑器。
- 钉钉、企业微信基础集成。
- Kanban 视图。
- 大屏查看和大屏编辑器。

优先级说明：

- 平台管理视觉必须同时覆盖企业后台专业工具风，以及工业制造感 / 大屏感 / 科技感。
- 第一版主题能力至少包含 `light-industrial` 与 `dark-industrial`，用于验证主题切换。
- 新增/编辑/查看等操作方式需要可配置，不能固定死在单个页面实现中。
- 复杂视图优先推进 Kanban、流程编辑器、审批流编辑器；大屏查看和大屏编辑器后续推进。

本阶段不实现：

- WMS 收货、库存、上架业务闭环。
- MES/SRM/QMS 业务模块。
- 移动离线复杂能力。
- AI Assistant 深度业务执行。

## 4. 任务清单

| 任务 | 说明 |
|---|---|
| `TASK-0204` | 平台 UX、页面交互、设计系统规范 |
| `TASK-0205` | 动态菜单与权限管理闭环 |
| `TASK-0214` | 组织维度与用户多组织归属 |
| `TASK-0215` | 数据范围与字段权限模型 |
| `TASK-0216` | 菜单路由与 API 鉴权一致性 |
| `TASK-0206` | 字典、参数、操作日志 |
| `TASK-0207` | 主题、多语言、品牌配置 |
| `TASK-0201` | 状态机核心 |
| `TASK-0202` | 审批核心 |
| `TASK-0208` | 审批任务中心前端 |
| `TASK-0209` | 流程编辑器与审批流编辑器 |
| `TASK-0210` | 钉钉、企业微信基础集成 |
| `TASK-0211` | Kanban 平台能力 |
| `TASK-0212` | 大屏查看与大屏编辑器 |
| `TASK-0203` | 规则引擎核心 |

## 5. Code Map 更新计划

每个子任务必须按影响范围更新：

- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`
- `docs/code-map/frontend.md`

## 6. 验收标准

- 所有子任务都有正式任务文件。
- 每个子任务都能由其他 agent 独立执行。
- 每个子任务都包含后端、前端、数据库、权限、测试、code map 要求。
- Platform Foundation 完成前，不启动 WMS 业务闭环任务。

## 7. Codex 执行要求

执行本阶段任一任务前，必须先确认任务是否属于 Platform Foundation。若任务试图直接进入 WMS 业务开发，应先暂停并要求用户确认。
