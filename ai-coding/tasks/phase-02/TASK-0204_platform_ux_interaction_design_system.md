# TASK-0204 平台 UX、页面交互与设计系统规范

## 1. 任务目标

定义并落地 IAF 平台管理端的页面交互、视觉风格和组件使用规范，让用户、组织、角色、菜单、字典、审批等平台页面具备一致的查看、新增、编辑、操作体验。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/frontend/00_README.md`
- `docs/frontend/01_前端总体架构.md`
- `docs/frontend/02_PC端布局与交互规范.md`
- `docs/frontend/06_多主题设计规范.md`
- `docs/frontend/08_权限与状态驱动UI规范.md`
- `docs/frontend/16_平台管理页面交互规范.md`
- `docs/frontend/17_平台管理视觉设计规范.md`
- `docs/frontend/18_复杂视图与设计器规范.md`
- `docs/frontend/19_平台应用外壳与导航规范.md`
- `docs/code-map/frontend.md`

## 3. 业务范围

本任务实现：

- 平台管理页面交互规范文档。
- 标准列表页规范。
- 新增/编辑弹窗、抽屉、独立页适用规则。
- 新增/编辑展示方式的页面元数据、主题默认值和用户偏好规则。
- 查看详情页规范。
- 危险操作规范。
- 空态、加载态、错误态规范。
- PC 管理端视觉规范。
- `light-industrial` 和 `dark-industrial` 两个可切换主题的基础 token 方案。
- 企业后台专业工具风与工业制造感 / 大屏感 / 科技感的场景分层规则。
- `table-engine` 和 `form-engine` 的平台样板升级计划。

本任务不实现：

- 具体业务模块。
- 大屏设计器。
- 流程设计器。

## 4. 需要新增/修改的文件

前端规范：

```text
docs/frontend/16_平台管理页面交互规范.md
docs/frontend/17_平台管理视觉设计规范.md
docs/frontend/18_复杂视图与设计器规范.md
docs/frontend/19_平台应用外壳与导航规范.md
```

前端组件，按需要：

```text
frontend/packages/ui-core/src/
frontend/packages/table-engine/src/
frontend/packages/form-engine/src/
```

## 5. 前端设计

必须明确：

- 页面标题区。
- 查询区。
- 工具栏。
- 表格密度。
- 操作列。
- 批量操作。
- 表单布局。
- 详情布局。
- 审批/状态/操作日志展示区域。
- TabWorkspace 中页面打开、关闭、dirty state 规则。
- 标准页面、Kanban、调度、监控、大屏、设计器的视觉强度分层。
- 主题切换、密度切换、操作模式偏好的配置入口和持久化边界。
- 左侧导航搜索、折叠、拖拽宽度、用户档案入口和顶部栏外壳能力。

## 6. 测试要求

- 更新或新增组件测试。
- 现有平台页面测试继续通过。

## 7. 验收标准

- 规范文档可指导后续所有平台页面。
- 用户/组织/角色页面能作为规范样板。
- 至少两个主题方向可用于后续实现和测试主题切换。
- Modal、Drawer、独立页面的选择规则不依赖页面硬编码。
- `./scripts/check-quality.sh` 通过。
