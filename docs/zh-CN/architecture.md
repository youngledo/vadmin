# Vaadin Admin Starter - 架构

[English](../en/architecture.md) | 简体中文

## 范围

Vaadin Admin Starter 是面向 Java 内部业务应用的生产级基线。第 1 版支持 Java 25、
Spring Boot 4.x、Vaadin Flow 25.x、PostgreSQL 和 Flyway SQL 迁移。Spring Boot 是唯一
运行时，Vaadin Flow 是唯一 UI 编程模型。

可复用核心不依赖 Spring、JPA、Flyway 或应用业务类型。starter 将 Spring 适配器组合为可直接
使用的默认体验；使用方拥有自己的业务领域和部署配置。

## 模块职责

| 模块 | 职责 |
| --- | --- |
| `admin-contracts` | 与框架无关的身份、授权、审计、错误和文件契约。 |
| `admin-platform` | 与框架无关的 RBAC 用例和端口。 |
| `admin-flow` | 无 Spring 依赖的 Flow 模式、路由守卫和模块元数据。 |
| `admin-spring-security` | 本地认证及可选的标准 OIDC 适配器。 |
| `admin-spring-jpa` | JPA/Flyway RBAC 与审计适配器。 |
| `admin-spring-boot` | 关联 ID 与 Problem Details 配置。 |
| `admin-spring-flow` | 模块组装、动态路由、组合翻译和语言偏好。 |
| `admin-spring-starter` | 默认外壳和主题、系统管理 UI 与翻译、面向使用方的依赖组合。 |
| `admin-reference-app` | 精简 starter 使用方、启动配置、种子数据和浏览器验收覆盖。 |

`admin-spring-starter` 提供外壳、主题、首页，以及 Users、Roles、Permissions、Audit 系统
管理模块。`admin-reference-app` 不再拥有复制的基线，而是验证普通应用仅依赖 starter 并添加
自身功能即可运行。

## 默认使用路径

```text
使用方应用
  -> admin-spring-starter
       -> Spring security、JPA、Boot 和 Flow 适配器
       -> 默认外壳/主题和系统管理
  -> 使用方 AdminModule Bean 与 Flow View Bean
```

普通使用方配置数据源和 Flyway 迁移位置、依赖 `admin-spring-starter` 后即可启动。无需定义
布局、主题或系统页面，即可获得本地登录、按权限过滤的外壳、Users、Roles、Permissions、
Audit、`zh-CN`/`en-US` 翻译、浅色/深色模式以及 Vaadin 或 Ant 风格外观。

业务功能通过 `AdminModule` Bean 声明。每个页面都有稳定元数据、所需权限、路由、图标 key、
Flow View 类型和两套消息资源包。启动时，`admin-spring-flow` 校验模块 ID、页面 ID、路由、
权限、导航分组和翻译资源，然后组装统一权限目录、`AdminModuleRegistry`、组合
`I18NProvider` 与动态路由注册。

导航是按授权过滤后的已组装页面投影。路由守卫也会在创建 View 前检查直接访问；用例会再次
授权变更操作。导航控制改善用户体验，用例检查才是权威安全边界。

## 主题与生产构建

starter 默认应用外壳注册 `admin-theme`。它的语义化 `--admin-*` token 契约支持浅色/深色、
`vaadin` 与 `ant` 视觉语言及舒适/紧凑密度。模块使用公开 token 和共享 Flow 模式，不注册
全局主题，也不修改全局 Lumo 变量。

动态路由需要静态生产前端锚点。使用方新增动态业务 View 时，在自己拥有的应用配置类型上
为该 View 声明一次 `@Uses(该View.class)`。View 本身不声明 `@Route`；模块元数据才是路由
注册的唯一来源。

## 有意的完全替换

默认路径采用完整基线所有权。确实需要不同外壳的使用方可以有意提供 `AdminHostLayout`，并
自行拥有 `AppShellConfigurator` 和 `@Theme` 配置。这是完整外壳替换，不能用于零散替换默认
页面或样式。

使用方仍使用 `AdminModuleRegistry`、模块组装、权限、路由守卫和组合翻译提供器，并必须为
所组合的动态 View 提供一致的布局及生产锚点。不需要此边界的使用方应直接使用 starter 默认
体验。

## 运行与演进

Flyway 在 JPA 适配器可用前运行。本地密码登录是基线；OIDC 是可选的标准授权码适配器，
只会将外部身份映射到已有且已启用的本地账户。它不会创建账户、授予角色、同步组或向 Flow
模块公开提供商 token。

starter 会为认证和系统管理记录审计结果、传播关联 ID，并将 HTTP 失败映射为 RFC 9457
Problem Details。多租户、数据范围授权、SAML/LDAP/MFA、SCIM、非 Spring 运行时、工作流
引擎、低代码 UI 构建和任意 View 的运行时加载均不在当前范围内。
