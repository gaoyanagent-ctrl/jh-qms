# 后端开发规则

## 1. 包结构

每个模块必须使用：

```text
interfaces/application/domain/infrastructure
```

## 2. Controller

Controller 只接收请求、调用应用服务、返回结果。禁止业务逻辑。

## 3. ApplicationService

负责事务、用例编排、权限、状态机、审批、事件。

## 4. DomainService

负责跨实体或复杂领域规则。

## 5. Repository

领域层只定义 Repository 接口，基础设施层实现。

## 6. 异常

必须使用统一业务异常：

```java
throw new BusinessException(ErrorCode.XYZ, "message");
```

禁止裸 RuntimeException。

## 7. 事务

写操作必须在 ApplicationService 层使用事务。禁止在 Controller 开事务。

## 8. DTO

Request/Response 不得直接使用 Entity。

## 9. 枚举

业务状态必须使用枚举，不允许魔法字符串散落代码。

## 10. OpenAPI

新增 API 必须具备接口说明、参数说明、返回说明。
