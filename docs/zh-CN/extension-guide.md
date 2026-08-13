# 扩展指南

[English](../en/extension-guide.md) | 简体中文

本指南说明独立打包的 Vaadin Flow 管理模块应如何接入 Spring Boot 宿主。当前只支持
Spring Boot 运行时。这是编译期 Maven 组合，不是运行时插件安装：宿主通过普通 Maven
依赖选择模块，Spring Boot 在启动时发现各模块的自动配置。

`admin-contracts` 和 `admin-platform` 保持 Java 优先且不依赖 Spring。`admin-flow`
拥有 Flow 页面模式和 Spring-free 管理模块契约。`admin-spring-flow` 是 Spring Boot
适配器，负责聚合模块描述符、权限、翻译资源和动态 Flow 路由；宿主拥有自身的应用布局与主题。

## 新增管理模块

使用 `admin-examples/admin-example-orders` 中的独立订单示例作为完整的工作参考。宿主只需
添加一项 Maven 依赖来接入该制品：

```xml
<dependency>
  <groupId>io.github.vaadinadminstarter</groupId>
  <artifactId>admin-example-orders</artifactId>
  <version>${vaadin-admin-starter.version}</version>
</dependency>
```

模块自身依赖 `admin-flow`、`admin-contracts`、`spring-boot-autoconfigure` 和
`vaadin-spring`，不得依赖 `admin-reference-app`。参考应用包含订单制品，仅用于展示宿主
组合方式和验收测试。

每个模块从一个 Boot 自动配置中贡献一个 `AdminModule` Bean。该描述符是导航、路由、权限
和翻译元数据的唯一事实来源：

```java
@AutoConfiguration
public class OrdersAutoConfiguration {
  @Bean
  AdminModule ordersAdminModule() {
    return AdminModule.of("orders",
        List.of(new AdminNavigationGroup("business", "orders.nav.group", 300)),
        List.of(new AdminPage("orders.list", "business", "orders.title",
            "orders.intent", "shopping-cart", 100, "orders",
            PermissionCode.of("orders:order:read"), OrdersView.class)),
        Set.of(PermissionCode.of("orders:order:read")),
        List.of(new AdminMessageBundle("orders", "orders.i18n.messages")));
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  OrdersView ordersView(CurrentUserProvider currentUser,
                         AuthorizationService authorization,
                         OrderQueryService orders) {
    return new OrdersView(currentUser, authorization, orders);
  }
}
```

使用稳定标识符。模块 ID、分组 ID、页面 ID、路由、权限代码和消息资源包基名都是公开配置
标识符。页面标题和意图 key 必须以模块 ID 为前缀，例如 `orders.title`、`orders.intent`。

不要在贡献的页面上声明 `@Route`。`admin-spring-flow` 会通过 Flow 公共路由配置 API，连同
宿主布局一起注册描述符中声明的 View 类型。受保护的 View 应继承
`PermissionProtectedView`，并返回与 `AdminPage` 声明相同的权限；匿名请求会跳转到
`login`，无权请求会跳转到 `access-denied`。

组合后的权限目录是权威来源。不要定义宿主 `PermissionCatalog` Bean。
`admin-spring-flow` 从每个启用的 `AdminModule` 推导权限目录，并通过宿主已有集成同步它；
宿主管理员再以常规方式把新权限授予现有角色。

### 宿主布局

宿主只声明一次其路由布局。模块不得导入具体宿主布局，例如参考应用的 `MainLayout`：

```java
@Bean
AdminHostLayout adminHostLayout() {
  return new AdminHostLayout(MainLayout.class);
}
```

### 生产前端锚点

动态注册可以让 View 在运行时路由，但 Vaadin 生产前端构建不能从路由注解发现该 View。因此，
宿主必须为每个动态注册模块 View 在其组合的 `@Layout` 类上添加一个静态 `@Uses` 注解。
这是必需的宿主生产包锚点，不能替代模块描述符，且模块 View 仍不得声明 `@Route`：

```java
@Layout
@Uses(OrdersView.class)
public final class MainLayout extends AppLayout {
    // 宿主的公共应用壳。
}
```

宿主组合更多模块页面时，也在这里添加其 View 类型。注解属于宿主而不是模块，因为宿主决定
哪些 Maven 模块进入生产构建。

### 翻译资源

模块声明消息资源包基名，并在自身制品中提供两种支持语言：

```text
src/main/resources/
  orders/i18n/messages_zh_CN.properties
  orders/i18n/messages_en_US.properties
```

组合 Provider 先解析当前选中的 `zh-CN` 或 `en-US` 资源，再回退到 `zh-CN`；未解析 key
会被记录并显示为显式标记。对用户切换语言后必须刷新的可见 View 文本使用
`LocaleChangeObserver`。导航和工作台条目会自动从描述符的翻译 key 解析。

### 冲突和图标

启动时会拒绝重复的模块 ID、页面 ID、路由、权限代码和消息资源包基名。只有当每项贡献的
ID、标题 key 和顺序完全相同时，多个模块才能共享导航分组；第一项声明拥有其标题 key。
应在贡献模块中修正冲突，不得依赖发现顺序。

`iconKey` 在启动时校验。当前 `AdminIconCatalog` 支持 `briefcase`、`clock`、`history`、
`key`、`shield`、`shopping-cart` 和 `users`。请选择这些稳定 key；未知字符串是配置错误，
不会静默降级为通用图标。

### 接入检查清单

- 在宿主中以普通 Maven 依赖添加模块制品。
- 从模块 Boot 自动配置中恰好贡献一个 `AdminModule` Bean；需要时再提供 Spring 管理的
  prototype View Bean。
- 不要在贡献 View 上声明 `@Route`，也不要定义宿主 `PermissionCatalog`。
- 注册宿主 `AdminHostLayout`，并为宿主布局上的每个动态注册模块 View 添加
  `@Uses(ModuleView.class)`。
- 为每个声明的消息资源包提供完整的 `zh-CN` 默认资源。启动校验器还要求模块声明的每个
  导航分组标题、页面标题和页面意图 key。为第二种支持语言增加 `en-US`；其缺失 key 会稳定
  回退到 `zh-CN`。
- 修改已组合的 View 集合后，运行宿主生产构建。

## 使用 Flow 设计系统

`admin-flow` 提供可组合的 Java Flow 页面模式，而不是另一个前端组件运行时。页面继续直接
使用标准 Vaadin 组件；仅在重复管理工作流中组合以下模式：

| 模式 | 用途 |
| --- | --- |
| `PageHeader` | 页面标题、说明、位置和页面级动作。 |
| `PageToolbar` | 查询字段、次要操作和主要创建动作。 |
| `DataWorkspace<T>` | 稳定的 `Grid` 容器、选中数量、批量操作，以及忙碌、空或失败状态。 |
| `EditorDialog` | 响应式 `FormLayout`、字段校验反馈和标准页脚动作。 |
| `EmptyState` | 带有明确下一步的空数据或加载失败内容。 |
| `PagedGrid<T>` | 将服务端分页查询绑定到 `Grid`，并统一刷新行为和空表格文案。 |

以下是精简的页面组合示例。领域查询和命令仍是应用层依赖；示例不会向 `admin-flow` 引入
Spring 类型：

```java
var header = new PageHeader("订单", "处理可访问订单。");
var grid = new Grid<OrderRow>();
var pages = new PagedGrid<>(grid, queries::orders, "number");
var workspace = new DataWorkspace<>(grid);
workspace.setFooter(pages.getPaginationBar());
add(header, workspace);
```

使用 `workspace.setBusy(true)`、`workspace.showEmpty(...)`、
`workspace.showFailure(...)` 和 `workspace.showData()` 显式表示异步查询或加载结果。
不要将“无数据”显示为成功的表格，也不要在可复用模式中放入领域文案或权限决策。页面提供
批量操作资格；资格发生变化时调用 `workspace.refreshBulkActions()`。

主题属于宿主应用。只有宿主声明 `@Theme` 并控制浅色/深色选择。模块使用文档化的
`--admin-*` 语义令牌或现有 Flow 页面模式，不得声明全局主题或竞争性品牌样式表。规范令牌
契约覆盖 surface、text、border、accent、success、warning、danger、focus、spacing、
typography、radius、elevation 和密集工作区角色，详见[主题令牌](../en/theme-tokens.md)。

模块不得导入宿主 `admin-theme`、声明全局 `@Theme`、选择外观档案或密度、修改全局 Lumo
变量，或依赖仅适用于 Ant 的选择器或 Grid 内部结构。Grid 表头和行、分页页脚、工作区状态
与危险确认内容均由宿主负责呈现；模块只使用普通 Flow 状态和对话框 API，详见
[外观配置档案](appearance-profiles.md)。

宿主 `MainLayout` 从组合后的 `AdminModuleRegistry` 渲染按权限过滤的分组导航，并使用相同
元数据生成工作台条目。隐藏导航不等于授权：变更用例必须再次检查 `AuthorizationService`。

## 新增业务模块

业务模块应定义自己的实体、查询模型、端口和用例。推荐调用方向：

```text
Flow view -> business use case -> repository / audit / file-storage ports
                                     ^
                           Spring JPA or other adapter
```

将业务规则和授权放入用例，而不是页面事件监听器。将 JPA 映射、SQL 和事务放入 Spring
适配器或使用方自身的持久化实现。每个变更用例都增加权限检查和审计结果，每个新表都使用
不可变的版本化 Flyway 迁移。核心模块不得导入 Spring、JPA、Flyway 或具体适配器类型。

## 替换文件存储

`FileStorage` 是存储、读取和删除二进制内容的端口。默认 `LocalFileStorage` 适用于本地开发
和演示。生产对象存储适配器应使用不透明 UUID 键、保留流式读取语义、在失败时记录可诊断
日志，并在业务删除操作中调用相同删除语义。

新增适配器后，在组合根提供唯一 `FileStorage` Bean，并配置其凭据和存储位置。如果适配器
引入新的事务、一致性、重试、删除延迟或安全语义，先记录架构决策并为失败与恢复路径增加
集成测试。

## HTTP 错误与可观测性

自定义 HTTP API 复用 `ProblemDetailMapper` 将 `BusinessFailure` 映射为 RFC 9457 Problem
Details。不要将 Spring 的 `ProblemDetail` 放入 contracts 或 platform。Flow 交互复用
`FlowErrorMapper` 处理字段校验、访问拒绝与安全失败呈现。

每个新的 HTTP 入口必须保留 `X-Correlation-Id` 请求传播，并把关联 ID 写入日志和相关审计
事件。错误响应和日志只能包含安全的诊断信息。

## 扩展外部身份映射

`admin-contracts` 提供 `ExternalIdentityMapper`，供选择 Spring Security OIDC 登录的应用
使用。它接收框架无关的 `ExternalIdentity`，其中包含规范化 issuer URI、稳定 `sub`、可选
显示名称与邮箱，以及标量字符串声明；它返回应用已有 `CurrentUser` 或无结果。

使用方拥有映射器和自身数据模型。把 issuer 与 subject 匹配到预先存在且启用的本地账户；
不要把邮箱、显示名称或未经验证的组声明作为主身份键。只有映射成功后，starter 的 OIDC
适配器才会重建标准本地主体，因此既有路由检查、授权用例、审计和认证版本失效保持同一个
本地用户模型。

通过 Spring Security 客户端注册配置任意符合标准的 issuer，并将
`vaadin-admin.oidc.registration-id` 设为该注册 ID。对应重定向 URI 是
`{baseUrl}/login/oauth2/code/{registrationId}`。完整、与提供商无关的配置参见
[配置可选 OIDC 登录](quick-start.md#配置可选-oidc-登录)。Keycloak 仅用于测试；中国大陆、
全球及自托管提供商都使用相同的标准 OIDC 路径。

不要将企业授权策略放进可复用模块或 starter 的 mapper SPI。组到角色转换、即时创建、
去配、SCIM、MFA、SAML、LDAP 集成、租户选择和数据范围控制均由使用方扩展。无法映射到
启用本地账户的 OIDC 登录必须被拒绝，而不是创建账户或授予默认角色。
