# IAF AI Coding 开发说明书 v1.0

## 1. 总原则
自然语言需求不能直接进入代码修改，必须先转换成结构化开发任务、DSL 变更和影响范围分析。

## 2. 标准流程
自然语言需求 → 需求澄清与范围识别 → task.md → DSL 新增或修改 → migration → 后端代码 → 前端代码 → 权限/菜单/字典/编码规则 → 测试用例 → 自检与 Review。

## 3. 后端规则
- Controller 不写业务逻辑。
- Application Service 管理用例编排和事务边界。
- Domain Service 负责核心业务规则。
- Repository 接口定义在 domain，具体实现放 infrastructure。
- 简单 CRUD 使用 MyBatis Plus，复杂查询使用 Mapper XML。
- 业务单据动作必须走 Command/Action。
- 库存变化必须写 InventoryTransaction。
- 跨模块副作用通过 Outbox。

## 4. 前端规则
- 页面按模块划分。
- 列表优先 ProTable。
- 表单优先 ProForm。
- 按钮必须绑定权限编码。
- 字段隐藏、只读、必填来自后端字段权限和审批节点字段权限。

## 5. DSL 必填内容
module、type、name、table、title、fields、lines、actions、permissions、stateMachine、approval、pages、rules。

## 6. AI 开发任务模板
包含业务目标、业务对象、字段清单、状态与动作、审批与规则、后端改动、前端改动、数据库变更、测试要求、验收标准。

## 7. Review 清单
模块边界、事务边界、状态变更、审批、权限、库存事务、Outbox、Migration、测试、前端交互。
