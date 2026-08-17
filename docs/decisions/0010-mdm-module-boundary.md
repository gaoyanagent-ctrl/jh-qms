# ADR-0010：新增独立 MDM 模块边界

## 状态

Accepted

## 决策

新增平铺 Maven 模块 `iaf-mdm`，归属公共主数据产品能力。它依赖平台核心安全能力，但不依赖 QMS、WMS 或制造模块的 infrastructure。

MDM 在同一 Spring Boot 进程部署；元数据、动态记录、版本与后续治理/集成能力均由该模块拥有。业务模块只能通过 MDM application API、HTTP API 或事件消费主数据。

## 理由

MDM 是跨 QMS/WMS/MES 的权威源，放入任一业务模块都会形成反向依赖。独立模块同时保留未来拆分 worker 的边界。
