# 项目工程规则

## 1. Monorepo 结构

项目必须使用 monorepo：

```text
iaf/
  AGENTS.md
  docs/
    code-map/
  ai-coding/
  backend/
  frontend/
  scripts/
  tools/
```

## 1.1 后端工程结构

后端必须使用 Maven 多模块工程，第一阶段采用平铺模块目录：

```text
backend/
  pom.xml
  iaf-app/
  iaf-shared/
  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/
  iaf-manufacturing-core/
  iaf-manufacturing-master/
  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

禁止第一阶段实现为单 Maven 工程 + 包内模块。后端结构决策见 `docs/decisions/0001-backend-maven-multi-module.md`。

## 1.2 Code Map 结构

项目必须维护 `docs/code-map/` 作为所有 Agent 的代码地图入口：

```text
docs/code-map/
  README.md
  backend.md
  api.md
  database.md
  frontend.md
```

任何新增或修改模块、类、公开方法、API、数据库 migration、前端路由、DSL 合约或跨模块依赖的任务，都必须同步更新对应 code map 文件。

如果任务没有更新 code map，最终回复必须说明原因。

## 2. 开发顺序

优先级：

1. 工程骨架。
2. 数据库 migration 基线。
3. 基础平台模块。
4. 状态机、审批、规则。
5. 制造主数据。
6. WMS 入库闭环。
7. 设计器、报表、集成中心。

## 3. 依赖管理

禁止在任务中随意引入依赖。新增依赖必须说明：

- 依赖名称。
- 版本。
- 用途。
- 替代方案。
- 风险。

## 4. 分支和提交

每个任务使用独立分支：

```text
feature/TASK-xxxx-short-name
```

提交信息：

```text
TASK-xxxx: 简短说明
```

多 Agent 并行开发、worktree、审核和合并流程必须遵守：

```text
ai-coding/rules/13_agent_parallel_work_rules.md
```

## 5. ADR 规则

以下事项必须写 ADR：

- 技术栈变更。
- 模块边界变更。
- 数据库策略变更。
- 工作流/规则引擎核心模型变更。
- WMS 库存核心算法变更。
