# TASK-0208 审批任务中心前端

## 1. 任务目标

在审批核心后端能力基础上，实现平台审批任务中心前端，使用户可以查看待办、已办、我发起的流程，并执行同意、拒绝、退回。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/07_workflow_rules.md`
- `ai-coding/tasks/phase-02/TASK-0202_approval_core.md`
- `docs/module-specs/platform/03_approval_workflow_spec.md`

## 3. 业务范围

本任务实现：

- 我的待办。
- 我的已办。
- 我发起的。
- 审批详情。
- 审批时间线。
- 审批操作按钮。
- 审批字段权限展示。

本任务不实现：

- 流程设计器。
- 审批流设计器。
- 移动端审批。

## 4. 前端文件

```text
frontend/apps/pc-admin/src/modules/platform/approval/
  api.ts
  hooks.ts
  types.ts
  ApprovalTaskListPage.tsx
  ApprovalDetailPage.tsx
  ApprovalTimeline.tsx
```

## 5. API 清单

依赖后端：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/approval-tasks/my` | 我的待办 |
| GET | `/api/platform/approval-tasks/done` | 我的已办 |
| GET | `/api/platform/approvals/my-started` | 我发起的 |
| GET | `/api/platform/approval-tasks/{id}` | 审批任务详情 |
| POST | `/api/platform/approval-tasks/{id}/approve` | 同意 |
| POST | `/api/platform/approval-tasks/{id}/reject` | 拒绝 |
| POST | `/api/platform/approval-tasks/{id}/return` | 退回 |

## 6. 权限点

```text
platform:approval-task:view
platform:approval-task:approve
platform:approval-task:reject
platform:approval-task:return
```

## 7. 测试要求

- 待办列表。
- 审批按钮按权限显示。
- 审批操作成功刷新。
- 字段权限隐藏或只读。

## 8. 验收标准

- 前端审批任务可完整操作。
- `./scripts/check-quality.sh` 通过。
