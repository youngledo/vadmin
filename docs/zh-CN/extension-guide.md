# 扩展指南

[English](../en/extension-guide.md) | 简体中文

本项目是模块化单体。`admin-contracts` 定义 Java 级端口和共享模型，
`admin-platform` 定义经过授权的业务用例。Flow、Spring Security、JPA 和 Spring Boot
属于适配器层；`admin-reference-app` 是组合根和示例业务模块。新增业务能力应沿着这一
依赖方向组织。不要让 Flow 视图直接调用 JPA repository。

## 新增权限和页面

首先在 `ApplicationConfiguration.permissionCatalog()` 中新增稳定的权限代码，例如
`orders:order:read`。然后在同一个配置类中新增 `PageDefinition`。它应提供稳定的
`pageId`、中文标题键、图标、顺序、路由和所需权限。启动时，权限目录会同步到数据库；
管理员仍须显式将新权限授予现有角色。

使用 `@Route` 注册 Flow 页面，并在进入页面前检查与页面定义相同的权限。参考应用中的
受保护页面通过 `SecuredView` 执行该检查。新页面应遵循相同的重定向策略：未登录用户
进入登录页，没有权限的用户进入 `access-denied`。页面上的按钮仅是用户体验控制。用于
创建、修改、删除和其他变更的平台用例必须再次调用 `AuthorizationService`。

## 使用 Flow 设计系统

`admin-flow` 提供可组合的 Java Flow 页面模式，而不是另一个前端组件运行时。页面继续
直接使用标准 Vaadin 组件；仅在重复的管理工作流中组合以下模式：

| 模式 | 用途 |
|---|---|
| `PageHeader` | 页面标题、说明、位置和页面级动作。 |
| `PageToolbar` | 查询字段、次要操作和主要创建操作。 |
| `DataWorkspace<T>` | 稳定的 `Grid` 容器、选中数量、批量操作，以及忙碌、空或失败状态。 |
| `EditorDialog` | 响应式 `FormLayout`、字段校验反馈和标准页脚操作。 |
| `EmptyState` | 带有明确下一步的空数据或加载失败内容。 |
| `PagedGrid<T>` | 将服务端分页查询绑定到 `Grid`，并统一刷新行为和空表格文案。 |

以下是精简的页面组合示例。领域查询和命令仍是应用层依赖；示例不会向
`admin-flow` 引入 Spring 类型：

```java
var header = new PageHeader("订单", "处理可访问订单。");

var filter = new TextField("搜索订单");
var toolbar = new PageToolbar();
toolbar.addFilter(filter);
toolbar.setPrimaryAction(new Button("新增订单", event -> openEditor()));

var grid = new Grid<OrderRow>();
grid.setSelectionMode(Grid.SelectionMode.MULTI);
var pages = new PagedGrid<>(grid, queries::orders,
        () -> Map.of("q", filter.getValue()), "number");
filter.addValueChangeListener(event -> pages.refresh());

var workspace = new DataWorkspace<>(grid);
workspace.addBulkAction(cancelSelected, () -> canCancelOrders());
add(header, toolbar, workspace);
```

编辑器通过实际的命令处理器保存，并在对话框中显示可恢复的字段错误：

```java
var editor = new EditorDialog("新增订单", "保存", () -> { });
editor.addField(number, customer);
editor.getPrimaryAction().addClickListener(event -> {
    if (number.isEmpty()) {
        editor.showValidationMessage("订单号为必填项。");
        return;
    }
    commands.create(requireCurrentUser(), number.getValue(), customer.getValue());
    editor.close();
    pages.refresh();
});
editor.open();
```

使用 `workspace.setBusy(true)`、`workspace.showEmpty(...)`、
`workspace.showFailure(...)` 和 `workspace.showData()` 显式表示异步查询或加载
结果。不要将“无数据”显示为成功的表格，也不要在可复用模式中放入领域文案或权限决策。
页面提供批量操作资格；资格发生变化时，页面调用
`workspace.refreshBulkActions()`。

主题属于应用层。参考应用使用 `ApplicationShell` 和 `@Theme("admin-theme")` 注册
`src/main/frontend/themes/admin-theme/theme.json` 与 `styles.css`。新应用应复制或创建
自己的命名主题，并覆盖 `--admin-*` 语义变量，而不是修改 `admin-flow`。当前用户菜单中
的浅色/深色模式仅保存在 Vaadin session；它不是账户偏好设置。

参考应用的 `MainLayout` 组合了应用外壳。它使用 `AppLayout`、
`PageRegistry.visibleTo(...)` 和授权服务生成分组导航，并通过 `DrawerToggle` 保持窄屏
中的导航可达。扩展页面应复用该布局，或实现相同的权限过滤和直接路由保护。隐藏导航
不能替代用例级授权。

## 新增业务模块

业务模块应定义自己的实体、查询模型、端口和用例。推荐的调用方向为：

```text
Flow view -> business use case -> repository / audit / file-storage ports
                                     ^
                           Spring JPA or other adapter
```

将业务规则和授权放入用例，而不是页面事件监听器。将 JPA 映射、SQL 和事务放入 Spring
适配器或参考应用的持久化实现。为每个变更用例增加权限检查和审计结果，并为每个新表增加
不可变的版本化 Flyway 迁移。核心模块不得导入 Spring、JPA、Flyway 或具体的适配器类型。

## 替换文件存储

`FileStorage` 是用于存储、读取和删除二进制内容的端口。默认的 `LocalFileStorage` 适合
本地开发和演示。生产对象存储适配器应以不透明 UUID 作为键，保留流式读取语义，在失败
时提供可诊断的日志，并在业务删除操作中调用相同的删除语义。

新增适配器后，请在组合根提供唯一的 `FileStorage` bean，并配置其凭据和存储位置。若
适配器引入新的事务、一致性、重试、删除延迟或安全语义，应先记录架构决策，并为失败和
恢复路径增加集成测试。

## 中文优先的 UI 和品牌

当前参考 UI 以中文为先。为新页面、校验消息和导航标题优先提供中文文本。需要其他语言
时，使用消息键和 Spring `MessageSource`；不要将业务规则绑定到展示文本。应用层负责
品牌名称、主布局标题、导航文案和主题变量。保留 Vaadin 的无障碍和浅色/深色主题约定，
不要修改可复用模块来硬编码特定组织的名称或颜色。

## HTTP 错误与可观测性

对于自定义 HTTP API，复用 `ProblemDetailMapper` 将 `BusinessFailure` 映射为 RFC 9457
Problem Details。不要将 Spring 的 `ProblemDetail` 放入 contracts 或 platform。对于 Flow
交互，复用 `FlowErrorMapper` 处理字段校验、访问拒绝和安全失败呈现。

每个新的 HTTP 入口都必须保留 `X-Correlation-Id` 请求传播，并将关联 ID 写入日志和相关
审计事件。错误响应和日志只能包含安全的诊断信息。
