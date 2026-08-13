# Vaadin Admin Starter - 架构设计

[English](../en/architecture.md) | 简体中文

状态：草案 0.1  
日期：2026-08-04

## 1. 范围和基线

Vaadin Admin Starter 是一个采用 Apache-2.0 许可、面向使用 Java 构建内部业务应用团队的
生产级脚手架。第 1 版支持 Java 25、Vaadin Flow 25.x、Spring Boot 4.x、PostgreSQL 和
Flyway SQL 迁移。

Vaadin Flow 是第 1 版唯一的 UI 编程模型。Hilla、React 和 TypeScript 不会进入构建或
可复用模块的依赖图。未来的 Hilla 适配器可以复用 contracts 和平台用例，但不属于本脚手架
的兼容性基线。

Spring Boot 是第 1 版唯一的运行时。可复用模块不得导入 Spring Framework、Spring Boot、
Spring Security、Spring Data、JPA 或 Flyway 类型。这些集成被有意隔离，以便未来的运行时
能够实现相同 contracts，而无需重写 Flow UI 或 RBAC 用例。

本设计采用带有选定端口和适配器的模块化单体。它不会引入 CQRS、Event Sourcing、命令
总线，或没有实际运行时、持久化、安全或测试边界的接口。

## 2. 架构决策

### 2.1 依赖方向

```text
Reference application -> Spring adapters -> Flow/platform/contracts
Flow/platform -> contracts
Spring adapters -> Flow/platform/contracts
Contracts -> Java only
```

参考应用是组合根。它选择 Spring Boot、安装适配器，并包含示例业务模块。没有任何可复用
模块依赖于它。

### 2.2 模块

| 模块 | 职责 | 允许的依赖 |
|---|---|---|
| `admin-contracts` | `CurrentUser`、授权、审计、导航、文件 contracts 和错误语义 | Java 25 |
| `admin-platform` | RBAC 管理用例、应用模型、持久化和审计 ports | contracts |
| `admin-flow` | 路由守卫、权限门控、Java 页面模式和 Flow 专用错误呈现 | contracts、platform、Vaadin Flow |
| `admin-spring` | Spring 专用适配器的 Maven parent 和 reactor aggregator；没有运行时代码 | 仅根 POM 继承 |
| `admin-spring-security` | 本地账户认证以及 `CurrentUser`/授权适配器 | Spring Security、contracts、platform |
| `admin-spring-jpa` | PostgreSQL JPA 映射、RBAC/审计 port 实现和 Flyway 集成 | Spring/JPA/Flyway、contracts、platform |
| `admin-spring-boot` | 自动配置、HTTP 错误映射、日志和可观测性装配 | Spring Boot、所有面向适配器的模块 |
| `admin-reference-app` | Spring Boot 启动器、部署配置和示例领域模块 | 可复用模块 |

`admin-spring` 是组织性的 Maven parent 和 reactor aggregator；它没有运行时代码或运行时
依赖角色。使用者仍直接依赖三个叶子 artifact。

`admin-flow` 是用户操作的主要适配器。JPA、Spring Security、文件存储和 HTTP API 是
次要或特定协议的适配器。Flow View 绝不直接与 JPA repository 通信。

### 2.3 Flow 设计系统

Phase 1 将可复用的 Flow 模式保留在 `admin-flow` 中，将主题资源和应用组合保留在
`admin-reference-app` 中：

```text
ApplicationShell (@Theme("admin-theme"))
  -> admin-theme/theme.json + styles.css
  -> MainLayout (AppLayout)
       -> AdminModuleRegistry.pagesVisibleTo(currentUser, authorization)
       -> protected Flow views
            -> PageHeader / PageToolbar / DataWorkspace / EditorDialog
            -> DetailDialog / ConfirmationDialog / OperationFeedback
```

`ApplicationShell` 是 Flow 的 `AppShellConfigurator`；它是放置应用级 `@Theme` 注解的
必要位置。`MainLayout` 是应用组合类，而不是可复用的 Spring 适配器。它从
`AdminModuleRegistry` 获取经权限过滤的页面投影，提供产品页头和分组导航，并在导航后更新
当前路由标题。

具名 `admin-theme` 位于
`admin-reference-app/src/main/frontend/themes/admin-theme/`。其 CSS 负责语义化浅色和深色
颜色 token、密度、焦点、外壳、画布和窄视口规则。当前用户菜单将选择的浅色/深色模式
保存在 Vaadin session 中，并将其应用于 Flow UI 根节点。在 Phase 1 中，它有意不作为持久化
账户偏好。使用者应用可以创建自己的应用外壳和具名主题，而无需修改 `admin-flow`。

`admin-flow` 保持不依赖 Spring：它可以依赖 Vaadin Flow、`admin-contracts` 和
`admin-platform`，但不得导入 Spring Framework、Spring Boot、Spring Security、JPA、Flyway、
参考应用或其业务类型。ArchUnit 强制执行这一边界中关于框架导入的部分；模块依赖方向使
参考应用位于可复用模块之外。

Phase 2 在 `admin-flow` 中新增三个仅使用 Java 的交互模式。`DetailDialog` 通过响应式、
只读表单呈现已经完成授权的实体数据。`ConfirmationDialog` 让具有后果的命令必须经由显式
确认，并在忙碌期间保持命令和关闭控件不可用，同时呈现本地命令失败。`OperationFeedback`
在本地呈现成功命令；它只将校验失败交给调用方提供的本地处理器，其他失败均原样重新抛出，
以便 `FlowErrorMapper` 继续拥有全局错误处理职责。这些模式不决定权限，也不调用用例、
持久化、Spring API 或参考应用的业务类型。组合应用中的视图在其外部提供授权、命令和领域
文案。

## 3. 授权和导航

### 3.1 权限模型

权限是授权词汇；角色是可配置的权限组。权限代码采用 `domain:resource:action`，例如：

```text
system:user:read
system:user:create
system:role:grant
```

初始模型为多对多：

```text
users <-> user_roles <-> roles <-> role_permissions <-> permissions
```

权限目录在代码中定义，并同步到数据库中由系统管理的记录。管理员可以将目录权限授予角色，
但不能在 UI 中创建任意权限代码。这使页面声明、动作、测试和文档保持一致。

### 3.2 页面注册表和菜单投影

每个 Flow 页面提供稳定的 `pageId`、所需权限代码、视图类和默认导航元数据。导航是通过
`AuthorizationService` 过滤的页面注册表投影。直接路由进入会在构造视图前由 Flow 路由
守卫检查。

第 1 版不会在 PostgreSQL 中存储任意路由或 Java 类名。未来的菜单配置模块可以持久化顺序、
标题或可见性，但只能引用已有的 `pageId` 值。

### 3.3 授权边界

1. 导航不显示当前用户无权访问的页面。
2. 路由守卫拒绝直接访问受保护页面。
3. `PermissionGate` 隐藏或禁用不可用操作，以提供连贯的 UI。
4. 平台用例会在变更前再次授权操作。

前三项是用户体验控制。第四项才是权威性的安全边界。

## 4. 数据和审计

`users` 保存唯一用户名、密码哈希、启用状态和 `auth_version`。密码变更、禁用或角色变更
都会递增 `auth_version`，以便使活动 session 失效或刷新。

`audit_entries` 记录认证结果和平台管理操作。其字段包括发生时间、已知时的操作者用户 ID、
动作代码、目标类型和 ID、结果、关联 ID 以及已脱敏的元数据。

成功的管理变更会在同一数据库事务中写入审计记录。当可以安全识别操作者或请求账户时，
认证失败和授权拒绝会记录结果。绝不审计密码哈希、原始密码、token、SQL 或 stack trace。

## 5. 运行时、错误和 HTTP 语义

在 JPA 支持的适配器可用之前，Spring Boot 会启动 Flyway。Spring Security 对本地账户进行
认证并提供框架中立的 `CurrentUser`。Flow 路由守卫和平台用例调用相同的授权 contract。

核心定义错误代码和结构化失败信息，例如 `authorization.denied`、`validation.failed`、
`resource.not_found`、`conflict.version` 和 `internal.error`。它不依赖 Web 响应类型。

Flow 交互通过 `FlowErrorMapper` 呈现为字段校验、安全通知或专用 403/500 视图。自定义
REST/MVC API 通过 `ProblemDetailMapper` 呈现为 RFC 9457 `application/problem+json`。
API 响应包含稳定的问题类型、HTTP 状态、安全详情、错误代码和关联 ID。API 401/403 响应
使用 Problem Details；浏览器 Flow 导航则使用登录视图或访问拒绝视图。

## 6. 可靠性和运维

每个请求都会获得关联标识符，该标识符包含在结构化日志、适用的审计记录和 Problem Details
中。未知失败会记录带诊断上下文的日志，并仅暴露安全的错误 ID。

参考应用使用外部化配置和环境变量。开发凭据与生产 profile 隔离。Spring Boot 健康检查、
结构化日志和适合容器的关闭是第 1 版要求。指标和追踪 exporter 仍是扩展点。

## 7. 测试和交付

| 测试层级 | 范围 |
|---|---|
| 单元 | contracts 和 platform 授权、RBAC 不变量、审计事件构造 |
| 架构 | ArchUnit 检查核心模块不导入 Spring、JPA、Flyway 或 Spring JPA adapter package |
| 集成 | Testcontainers PostgreSQL 验证 Flyway、JPA ports、认证适配器、事务和审计写入 |
| 浏览器 E2E | 登录、桌面和窄屏外壳导航、session 主题切换、菜单过滤、直接路由拒绝、校验和失败呈现、权限变更以及示例 CRUD 路径 |

CI 运行格式化和静态检查、所有测试层级、生产构建和 Docker image 构建。发布会公布兼容性
基线，并将新的 Java、Spring Boot 或 Vaadin major version 视为专门的升级工作。

## 8. 演进边界

以下内容被明确推迟：多租户、组织层级、数据范围授权、OIDC/SAML/LDAP/MFA、非 Spring
运行时、工作流引擎、低代码设计器，以及任意视图的动态加载。

未来的 Quarkus、Helidon、Jakarta EE 或 servlet adapter 会实现 contracts，并拥有自己的
运行时组合。它不会修改权限目录、平台用例或 Flow UI 模式。

## 9. 校验规则

以下为项目策略，并由测试强制执行：

1. 核心模块不导入 Spring、JPA、Flyway 或 Spring JPA adapter package。
2. Flow 视图调用平台用例，绝不调用 JPA repository。
3. 每个受保护页面和操作都使用目录权限代码。
4. 每个平台变更都会检查授权并产生审计结果。
5. 持久化菜单配置只能引用已知页面 ID。
6. 对于已映射失败，HTTP API 返回 RFC 9457 Problem Details；Flow protocol 请求由 Flow
   专用错误处理处理。

## 10. 架构指导结果

- **阶段**：Architecture Guidance
- **状态**：已完成
- **输入**：已确认的需求、模块化单体选择，以及 Spring 优先运行时和框架中立的可复用核心。
- **摘要**：在保持简单模块化单体的同时，为安全、持久化、审计和呈现选择了有意义的
  ports/adapters。
- **假设**：第 1 版仍是内部业务应用脚手架；本地账户已足够；PostgreSQL 是唯一的生产数据库。
- **决策**：框架独立性是由模块依赖和 ArchUnit 强制执行的项目策略；CQRS 和 Event Sourcing
  没有正当理由。
- **约束**：Flow 是主要适配器，核心拥有用例和 contracts，Spring/JPA 实现保留在核心之外。
- **证据**：需求文档、`outputs/` 中的架构图、Spring 的 RFC 9457 `ProblemDetail` 支持，以及
  Testcontainers PostgreSQL 支持。
- **开放问题**：没有阻塞实施计划的问题。
- **产物**：`outputs/` 中的需求文档和架构图。
- **建议的下一步**：编制分阶段实施计划。
- **交接说明**：package names 是项目约定；强制执行的规则是文档中的依赖和安全边界，而不
  声称每个 Java 业务应用都需要这一架构。
