# 审批流模块详细设计

## 1. 目标

提供统一审批能力，支持业务单据提交审批、任务处理、审批人解析、审批节点字段权限、审批状态与业务状态映射。Flowable 仅作为内部流程运行内核，不暴露给业务模块。

## 2. 核心模型

| 模型 | 说明 |
|---|---|
| ApprovalDefinition | 审批定义 |
| ApprovalNode | 审批节点 |
| ApprovalCondition | 分支条件 |
| ApprovalAssigneeRule | 审批人规则 |
| ApprovalFieldPermission | 节点字段权限 |
| ApprovalInstance | 审批实例 |
| ApprovalTask | 审批任务 |
| ApprovalAction | 审批动作 |
| ApprovalStateMapping | 状态映射 |

## 3. 数据库表

- platform_approval_def。
- platform_approval_node。
- platform_approval_assignee_rule。
- platform_approval_field_permission。
- platform_approval_instance。
- platform_approval_task。
- platform_approval_action_log。
- platform_approval_state_mapping。

## 4. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/approval-definitions | 审批定义列表 |
| POST | /api/platform/approval-definitions | 新增定义 |
| PUT | /api/platform/approval-definitions/{id} | 修改定义 |
| POST | /api/platform/approval-definitions/{id}/publish | 发布定义 |
| POST | /api/platform/approvals/start | 启动审批 |
| GET | /api/platform/approval-tasks/my | 我的审批任务 |
| POST | /api/platform/approval-tasks/{id}/approve | 同意 |
| POST | /api/platform/approval-tasks/{id}/reject | 拒绝 |
| POST | /api/platform/approval-tasks/{id}/return | 退回 |
| POST | /api/platform/approval-tasks/{id}/transfer | 转办 |

## 5. 审批动作

第一版支持：

- submit。
- approve。
- reject。
- return。
- withdraw。
- transfer。

## 6. 审批人解析

支持：

- 固定用户。
- 角色。
- 岗位。
- 部门负责人。
- 发起人上级。
- 字段指定人员。
- 自定义 Spring Bean。

## 7. 与状态机关系

审批模块只更新 approval_status。document_status 的变化通过 ApprovalStateMapping 触发状态机，不得直接写业务表状态。

## 8. 领域规则

- 一个业务对象同一时间只能有一个进行中的审批实例。
- 审批任务只能由候选人或被授权人处理。
- 审批完成必须记录 action log。
- 审批节点字段权限必须可供前端查询。

## 9. 测试

- 启动审批成功。
- 重复启动审批失败。
- 审批人解析正确。
- 同意后流转到下一节点。
- 拒绝后状态同步。
- 字段权限查询正确。
