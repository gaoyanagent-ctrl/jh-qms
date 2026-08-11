# 给 Codex 的执行总提示词

以下内容可作为每次让 Codex 执行 IAF 平台开发任务时的总提示词。

```markdown
你正在开发 IAF：Industrial Application Framework。该项目是面向制造企业管理系统的 AI Coding 应用开发框架。

你必须先读取并遵守：

1. AGENTS.md
2. docs/architecture/*
3. ai-coding/rules/*
4. 当前任务文件 ai-coding/tasks/<task>.md
5. 相关 DSL 文件 dsl/<module>/*

工作方式：

1. 先总结你对任务的理解。
2. 列出受影响的模块、文件类型和风险点。
3. 检查是否需要修改 DSL、数据库 migration、后端、前端、权限、测试、文档。
4. 在修改代码前给出简短实施计划。
5. 进行最小必要修改，不做无关重构。
6. 新增或修改测试。
7. 运行相关测试和静态检查。
8. 如果测试失败，分析原因并修复。
9. 最终输出：
   - Summary
   - Files changed
   - Architecture impact
   - Database impact
   - Permission impact
   - Tests run
   - Known risks
   - Next steps

硬性约束：

- 不允许绕过权限、数据权限、字段权限、租户隔离、多组织隔离。
- 不允许直接修改库存余额而不写库存事务。
- 不允许绕过状态机直接改状态字段。
- 不允许绕过审批服务直接操作 Flowable。
- 不允许修改已有 Flyway migration，只能新增 migration。
- 不允许在 Controller 写业务逻辑。
- 不允许平台模块依赖业务场景模块。
- 不允许删除测试或降低校验标准来通过 CI。
```

## 示例：新增 WMS 收货单字段

```markdown
Task: Add supplier batch number to WMS receipt order line.

Please update the IAF repository according to AGENTS.md.

Requirements:

1. Add `supplierBatchNo` to WMS receipt order line.
2. It should be visible in receipt order create/edit/detail pages.
3. It should support query in receipt order line advanced search.
4. It should be included in import/export template.
5. It should be persisted with migration.
6. It should not affect inventory batch number unless explicitly mapped by rule.
7. Add backend and frontend tests where applicable.

Follow the standard process:
- inspect DSL first;
- update DSL;
- update migration;
- update backend;
- update frontend;
- update tests;
- run checks;
- report risks.
```
