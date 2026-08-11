# Agent Skills 矩阵

## 1. 必需 Agent Skills

IAF 平台开发至少需要以下 Agent Skills。这里的 Skill 不是具体工具插件，而是 AI 在项目中承担的稳定能力单元。

| Skill | 职责 | 输入 | 输出 | 禁止事项 |
|---|---|---|---|---|
| Architecture Guardian | 检查模块边界、技术路线、DDD 分层、依赖方向 | 设计文档、代码 diff | 架构风险报告、修正建议 | 不直接改核心架构 |
| Requirement Structurer | 将自然语言需求转为开发任务 | 用户需求、业务背景 | task.md、验收标准、影响范围 | 不直接编码 |
| DSL Designer | 维护业务对象、单据、状态机、审批、权限 DSL | 需求说明、现有 DSL | YAML/JSON DSL 变更 | 不跳过 DSL 直接改业务实现 |
| Backend Developer | 生成 Spring Boot 后端代码 | DSL、接口契约、任务说明 | Controller/Application/Domain/Mapper/Test | 不跨层调用、不绕过事务 |
| Frontend Developer | 生成 React + Ant Design Pro 前端代码 | API、页面 DSL、权限点 | 页面、组件、service、路由 | 不硬编码权限和枚举 |
| Database Engineer | 生成 migration、索引、约束、测试数据 | 数据模型、DSL | Flyway SQL、seed data | 不直接修改历史 migration |
| Workflow Engineer | 生成审批 DSL、Flowable 适配、任务处理 | 审批需求、单据状态 | ApprovalDefinition、映射代码、测试 | 不暴露 Flowable 细节给业务模块 |
| Rule Engineer | 生成规则定义、条件表达式、动作绑定 | 规则需求、业务字段 | RuleDefinition、JSON Logic、测试用例 | 不使用不安全表达式执行 |
| Test Engineer | 设计和实现测试 | 任务说明、代码 diff | 单元/集成/E2E/契约测试 | 不只写 happy path |
| QA Reviewer | 代码质量、规范、风险审核 | diff、测试结果 | review.md、风险清单 | 不合并代码 |
| Security Reviewer | 认证、权限、数据隔离、安全输入检查 | diff、接口定义 | 安全审查报告 | 不放行缺少鉴权的接口 |
| Release Engineer | 检查构建、版本、migration、发布包 | main 分支、CI 结果 | release checklist | 不修业务代码 |

## 2. 推荐任务分派

| 任务类型 | 主 Skill | 辅助 Skill |
|---|---|---|
| 新增业务单据 | DSL Designer | Backend Developer、Frontend Developer、Test Engineer |
| 修改审批流 | Workflow Engineer | DSL Designer、Security Reviewer |
| 新增 WMS 策略 | Rule Engineer | Backend Developer、Test Engineer |
| 新增基础平台能力 | Backend Developer | Security Reviewer、Architecture Guardian |
| 页面开发 | Frontend Developer | Security Reviewer、QA Reviewer |
| 数据模型调整 | Database Engineer | Architecture Guardian、Test Engineer |
| 大重构 | Architecture Guardian | Backend Developer、Test Engineer、QA Reviewer |

## 3. Agent 执行顺序

标准任务链：

```text
Requirement Structurer
  -> Architecture Guardian
  -> DSL Designer
  -> Database Engineer
  -> Backend Developer
  -> Frontend Developer
  -> Test Engineer
  -> Security Reviewer
  -> QA Reviewer
```

紧急修复任务可以缩短，但不能跳过测试和安全审查。
