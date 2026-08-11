# TASK-0002 初始化 Spring Boot 后端工程

## 1. 任务目标

创建 IAF 后端 Spring Boot 3.x 工程，配置 Java 21、基础依赖、统一返回、统一异常、基础安全占位、Flyway、MyBatis Plus、OpenAPI。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/02_backend_rules.md
- ai-coding/rules/04_database_rules.md
- ai-coding/rules/05_api_rules.md

## 3. 技术要求

- Java 21。
- Spring Boot 3.x。
- Maven 多模块工程。
- MyBatis Plus。
- Flyway。
- springdoc-openapi。
- JUnit 5。

## 4. 后端目录

```text
backend/
  pom.xml
  iaf-app/
    src/main/java/com/company/iaf/app/IafApplication.java
  iaf-shared/
  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/
  iaf-manufacturing-core/
  iaf-manufacturing-master/
  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

每个 Maven module 内部必须遵守：

```text
interfaces/
application/
domain/
infrastructure/
```

## 5. 必须实现

- Result<T>。
- PageResult<T>。
- BusinessException。
- ErrorCode。
- GlobalExceptionHandler。
- BaseEntity。
- TenantContext 占位。
- SecurityContext 占位。
- HealthCheckController。
- backend parent pom。
- 所有第一阶段 Maven module pom。

## 6. API

```text
GET /api/health
```

## 7. 测试

- 应用上下文启动测试。
- HealthCheckController 测试。
- GlobalExceptionHandler 基础测试。

## 8. 验收标准

- 后端可以启动。
- /api/health 返回 OK。
- 测试通过。
- OpenAPI 页面可访问。
- Flyway 已配置但可以暂时无业务 migration。
