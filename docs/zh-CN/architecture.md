# VAdmin - 架构

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

[English](../en/architecture.md) | 简体中文

## 范围

VAdmin 是面向 Java 内部业务应用的生产级基线。第 1 版支持 Java 25、
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
| `vadmin-spring-security` | 本地认证及可选的标准 OIDC 适配器。 |
| `vadmin-spring-jpa` | JPA/Flyway RBAC 与审计适配器。 |
| `vadmin-spring-boot` | 关联 ID 与 Problem Details 配置。 |
| `vadmin-spring-flow` | 模块组装、动态路由、组合翻译和语言偏好。 |
| `vadmin-spring-boot-starter` | 默认外壳和主题、系统管理 UI 与翻译、面向使用方的依赖组合。 |
| `vadmin-reference-app` | 精简 starter 使用方、启动配置、种子数据和浏览器验收覆盖。 |

`vadmin-spring-boot-starter` 提供完整的默认后台框架、首页，以及 Users、Roles、Permissions、
Audit 系统管理模块。`vadmin-reference-app` 不再拥有复制的基线，而是验证普通应用仅依赖 starter
并添加自身功能即可运行。

## 默认使用路径

```text
使用方应用
  -> vadmin-spring-boot-starter
       -> Spring security、JPA、Boot 和 Flow 适配器
       -> 默认外壳/主题和系统管理
  -> 使用方 AdminModule Bean 与 Flow View Bean
```

普通使用方配置数据源和 Flyway 迁移位置、依赖 `vadmin-spring-boot-starter` 后即可启动。无需定义
布局、主题或系统页面，即可获得本地登录、按权限过滤的响应式后台外壳、Users、Roles、Permissions、
Audit、`zh-CN`/`en-US` 翻译、跟随系统/浅色/深色配色方案以及 Vaadin 或 Ant 风格外观。

默认后台外壳是 VAdmin 的产品职责，而不是让业务模块从 Flow 的基础布局组件开始拼装。它提供
响应式导航、全局操作区、系统管理、无障碍行为和统一的页面工作流；业务模块只补充领域数据、
命令、权限、翻译和页面中的领域内容。对于反复出现的管理工作流，应扩展 VAdmin 的共享 Flow
模式，而不是让每个使用方重复搭建。VAdmin 并不试图构建低代码页面编辑器，也不替业务方决定
领域页面的内容。

业务功能通过 `AdminModule` Bean 声明。每个页面都有稳定元数据、所需权限、路由、图标 key、
Flow View 类型和两套消息资源包。启动时，`vadmin-spring-flow` 校验模块 ID、页面 ID、路由、
权限、导航分组和翻译资源，然后组装统一权限目录、`AdminModuleRegistry`、组合
`I18NProvider` 与动态路由注册。

导航是按授权过滤后的已组装页面投影。路由守卫也会在创建 View 前检查直接访问；用例会再次
授权变更操作。导航控制改善用户体验，用例检查才是权威安全边界。

## 默认体验与视觉语言

默认 `vaadin` 语言以 Vaadin Aura 的原生组件外观为准。VAdmin 可以通过 `AppLayout`、
`SideNav`、`MenuBar`、`Avatar`、`Notification` 及 Flow 布局组件的公开 API 组合完整的后台
体验，但不得注册改变 Aura 外观的全局 CSS、组件 `part` 覆写，或自建颜色、圆角、间距、密度和
通知 token。结构组合与重造视觉样式是两个不同的职责。

显式 `ant` 语言拥有其自身作用域内的 CSS 覆写，以复现 Ant Design 风格。模块使用共享 Flow
模式，不注册全局主题、不修改全局主题属性，也不依赖 Ant 专用 CSS。系统、浅色和深色模式仅
通过 `ColorScheme` 与 `Page.setColorScheme()` 处理。

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
