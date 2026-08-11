# IAF 平台 Codex 可执行开发规格包 v1.0

本规格包用于指导 Codex CLI / Cursor / Claude Code 对 IAF 工业应用开发框架进行进一步详细设计、编码、测试和质量审核。

## 使用方式

1. 将本目录内容复制到 IAF 项目仓库根目录。
2. 每次让 AI coding 工具执行任务前，要求其先读取：
   - `AGENTS.md`
   - `ai-coding/rules/*`
   - 当前任务引用的 `docs/module-specs/*`
   - 当前任务文件 `ai-coding/tasks/*`
3. 每次开发任务必须输出：
   - 实施计划
   - 变更文件清单
   - 数据库 migration
   - 后端测试
   - 前端构建/类型检查结果
   - 自检清单
4. 禁止 AI 直接从自然语言自由修改核心业务代码。自然语言需求必须先转换为结构化任务。

## 文档分层

| 层级 | 目录 | 用途 |
|---|---|---|
| L1 | docs/architecture | 架构背景、ADR、总体约束 |
| L2 | AGENTS.md + ai-coding/rules | Codex 必读工程规则 |
| L3 | docs/module-specs | 模块级详细设计 |
| L4 | ai-coding/tasks | 可执行开发任务 |
| L5 | docs/quality | 测试、质量门禁、验收标准 |

## 当前优先开发路线

1. 初始化 monorepo 与基础工程。
2. 完成平台基础能力：用户、组织、角色、权限、字典、编码规则、附件、审计。
3. 完成状态机、审批流、规则引擎、Outbox。
4. 完成制造通用主数据。
5. 完成 WMS 入库闭环样板：仓库/库位、库存、收货、上架、库存过账。
