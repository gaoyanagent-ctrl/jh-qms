# 给 Codex 的执行总提示词

你正在开发 IAF 工业应用开发框架。执行任务前必须先读取并遵守：

1. AGENTS.md。
2. ai-coding/rules 目录下相关规则。
3. docs/module-specs 下相关模块详细设计。
4. 当前 ai-coding/tasks 下的任务文件。

执行要求：

- 不要直接开始写代码，先输出任务理解和实施计划。
- 严格遵守模块化单体和轻量 DDD 分层。
- Controller 不得直接访问 Mapper。
- 数据库变更必须写 Flyway migration。
- 所有写操作必须做权限校验。
- 所有业务状态变更必须走 StateMachineService。
- 审批必须走 ApprovalApplicationService。
- WMS 库存变更必须走 InventoryPostingService。
- 完成后必须运行测试或说明无法运行的原因。
- 完成后必须输出变更摘要、测试结果和自检清单。

输出格式：

```text
任务理解：
实施计划：
变更文件：
测试结果：
自检清单：
风险与后续建议：
```
