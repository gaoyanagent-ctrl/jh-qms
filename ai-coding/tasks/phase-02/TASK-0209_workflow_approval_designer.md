# TASK-0209 流程编辑器与审批流编辑器

## 1. 任务目标

实现平台流程编辑器和审批流编辑器，使管理员可以配置流程节点、审批人规则、条件、字段权限，并发布版本。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/07_workflow_rules.md`
- `docs/module-specs/platform/03_approval_workflow_spec.md`

## 3. 业务范围

本任务实现：

- 流程定义列表。
- 流程画布。
- 节点拖拽。
- 节点属性面板。
- 审批人规则配置。
- 条件表达式配置。
- 字段权限配置。
- 流程校验。
- 发布版本。
- 历史版本查看。

本任务不实现：

- Flowable 所有高级 BPMN 能力。
- 加签/减签复杂流程。
- 移动端设计器。

## 4. 技术约束

- 可引入 `bpmn-js` 或流程图编辑库，但必须先在任务实现中说明理由。
- 不允许业务模块直接依赖 Flowable API。
- 保存到后端的是 IAF 审批定义模型，不是前端私有模型。

## 5. 前端文件

```text
frontend/apps/pc-admin/src/modules/platform/workflow-designer/
frontend/packages/ui-business/src/
```

## 6. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/approval-definitions` | 审批定义列表 |
| POST | `/api/platform/approval-definitions` | 新增定义 |
| PUT | `/api/platform/approval-definitions/{id}` | 保存草稿 |
| POST | `/api/platform/approval-definitions/{id}/validate` | 校验 |
| POST | `/api/platform/approval-definitions/{id}/publish` | 发布 |
| GET | `/api/platform/approval-definitions/{id}/versions` | 版本列表 |

## 7. 权限点

```text
platform:workflow-designer:view
platform:workflow-designer:create
platform:workflow-designer:update
platform:workflow-designer:publish
```

## 8. 验收标准

- 可以创建基础审批流。
- 可以配置节点审批人和字段权限。
- 可以校验并发布版本。
- `./scripts/check-quality.sh` 通过。
