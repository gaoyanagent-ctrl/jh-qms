# TASK-0202 审批流核心模块开发

## 1. 任务目标

开发平台审批流核心模型，屏蔽 Flowable 细节，对业务模块提供统一 ApprovalApplicationService。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/07_workflow_rules.md
- docs/module-specs/platform/03_approval_workflow_spec.md

## 3. 本任务实现

- ApprovalDefinition。
- ApprovalNode。
- ApprovalAssigneeRule。
- ApprovalFieldPermission。
- ApprovalInstance。
- ApprovalTask。
- ApprovalActionLog。
- ApprovalApplicationService。

## 4. 本任务不实现

- 完整审批设计器 UI。
- 所有 Flowable 高级特性。
- 加签/减签复杂流程。

## 5. API

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/platform/approvals/start | 启动审批 |
| GET | /api/platform/approval-tasks/my | 我的任务 |
| POST | /api/platform/approval-tasks/{id}/approve | 同意 |
| POST | /api/platform/approval-tasks/{id}/reject | 拒绝 |
| POST | /api/platform/approval-tasks/{id}/return | 退回 |

## 6. 测试

- 启动审批成功。
- 重复启动失败。
- 审批人解析成功。
- 同意任务成功。
- 拒绝任务成功。
- 字段权限返回正确。

## 7. 验收标准

- 业务模块不依赖 Flowable API。
- 审批状态与业务状态分离。
- 审批操作有日志。
