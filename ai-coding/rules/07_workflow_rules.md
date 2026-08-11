# 审批流开发规则

## 1. 隔离 Flowable

业务模块只调用 ApprovalApplicationService，不得直接使用 Flowable API。

## 2. Approval DSL

审批定义以平台模型为准，发布时转换为 Flowable BPMN。

## 3. 审批状态

审批状态不得与单据状态混用。

## 4. 审批人解析

支持：固定用户、角色、岗位、部门负责人、上级领导、字段指定人员、自定义解析器。

## 5. 字段权限

审批节点可配置字段隐藏、只读、必填、可编辑。
