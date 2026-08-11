# 项目工程规则

## 1. Monorepo 结构

项目必须使用 monorepo：

```text
iaf/
  AGENTS.md
  docs/
  ai-coding/
  backend/
  frontend/
  scripts/
  tools/
```

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

## 5. ADR 规则

以下事项必须写 ADR：

- 技术栈变更。
- 模块边界变更。
- 数据库策略变更。
- 工作流/规则引擎核心模型变更。
- WMS 库存核心算法变更。
