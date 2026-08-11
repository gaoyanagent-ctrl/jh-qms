# Agent Skills 定义

本文件定义 IAF 项目开发中 AI coding 工具需要扮演的专业角色。每个任务必须明确使用哪些 Skills。

## 1. System Architect Skill

### 适用任务

- 总体架构设计。
- 模块边界设计。
- 依赖关系审查。
- ADR 编写。
- 技术选型变更评估。

### 输入

- 架构蓝图。
- 技术选型约束。
- 模块需求。
- 当前代码目录。

### 输出

- 模块边界说明。
- 依赖关系说明。
- 目录结构建议。
- ADR。
- 风险清单。

### 审查重点

- 是否符合模块化单体。
- 是否避免提前微服务化。
- 是否避免低代码平台复杂化。
- 是否保留向 IAOS 演进的扩展点。

## 2. Backend Engineer Skill

### 适用任务

- Spring Boot 模块开发。
- ApplicationService 开发。
- DomainService 开发。
- Repository 和 Mapper 开发。
- Controller 和 DTO 开发。
- 后端测试。

### 必须遵守

- 使用 Java 21。
- 使用 Spring Boot 3.x。
- 使用轻量 DDD 分层。
- 使用 MyBatis Plus + Mapper XML。
- 所有修改操作必须有事务边界。
- 所有业务错误必须使用统一异常。

### 输出

- Java 源码。
- Flyway migration。
- 单元测试/集成测试。
- OpenAPI 注解或可生成文档。

## 3. Frontend Engineer Skill

### 适用任务

- React 页面开发。
- Ant Design Pro 页面。
- 表格、表单、详情页。
- API client。
- 前端权限按钮。
- 状态驱动 UI。

### 必须遵守

- TypeScript strict。
- 不得使用 any 绕过类型问题，除非有明确注释说明。
- API 类型必须与后端 Response 对齐。
- 按钮显示必须同时考虑权限和业务状态。
- 所有请求通过统一 request client。

### 输出

- 页面组件。
- api.ts。
- types.ts。
- route 配置。
- 菜单配置。
- 基础 smoke test 或构建验证。

## 4. Database Designer Skill

### 适用任务

- 表结构设计。
- 索引设计。
- Flyway migration。
- 多组织字段设计。
- ext_json 使用策略。

### 审查清单

- 是否包含 id。
- 是否包含 tenant_id。
- 是否包含审计字段。
- 是否包含 deleted/version/ext_json。
- 查询字段是否有索引。
- 唯一约束是否考虑租户/组织隔离。
- 金额/数量是否使用 decimal。
- 状态字段是否有明确枚举。

## 5. Workflow Designer Skill

### 适用任务

- Approval DSL。
- Flowable 适配。
- 审批节点配置。
- 审批人解析。
- 审批字段权限。
- 审批状态映射。

### 禁止事项

- 业务模块直接调用 Flowable API。
- 将 BPMN 细节泄露给业务模块。
- 审批状态和业务状态混用。

## 6. Rule Engine Designer Skill

### 适用任务

- JSON Logic 规则模型。
- 条件编辑器后端模型。
- 规则绑定。
- 规则执行日志。
- 规则测试。

### 规则类型

- 字段校验规则。
- 审批触发规则。
- 审批路由规则。
- 库存策略规则。
- 上架策略规则。
- 编码规则。
- 消息通知规则。

## 7. WMS Domain Expert Skill

### 适用任务

- 仓库、库区、库位建模。
- 库存余额、库存明细、库存事务。
- 采购收货。
- 上架任务。
- 上架策略。
- 批次、序列号、包装单元。
- 库存过账算法。

### 必须避免

- 将 WMS 做成普通 CRUD。
- 直接修改库存余额而不记录事务。
- 收货和上架状态混用。
- 忽略批次/序列号/包装单元扩展点。

## 8. QA Engineer Skill

### 适用任务

- 测试策略。
- 单元测试。
- 集成测试。
- API 测试。
- 回归测试。
- 验收标准。

### 输出

- 测试用例。
- 测试数据。
- 测试执行命令。
- 缺陷清单。

## 9. Code Reviewer Skill

### 适用任务

- PR 审查。
- 架构合规检查。
- 安全检查。
- 事务一致性检查。
- 权限检查。
- 代码规范检查。

### 审核结果格式

必须输出：

- Blocking Issues：必须修复。
- Major Issues：建议本轮修复。
- Minor Issues：可后续优化。
- Positive Findings：符合规范的点。

## 10. DevOps Skill

### 适用任务

- Docker Compose。
- CI。
- 环境变量。
- 数据库初始化。
- 构建脚本。
- 部署检查。

### 输出

- Dockerfile。
- docker-compose.yml。
- CI 配置。
- run-tests.sh。
- check-quality.sh。
