# 扩展指南

本项目是模块化单体。`admin-contracts` 定义 Java 级端口和通用模型，
`admin-platform` 定义授权后的业务用例；Flow、Spring Security、JPA、Spring Boot
位于适配器层；`admin-reference-app` 是组合根和示例业务模块。新增业务功能应沿着
这个依赖方向组织，不要让 Flow 视图直接调用 JPA repository。

## 新增权限和页面

先在 `ApplicationConfiguration.permissionCatalog()` 中新增稳定的权限代码，例如
`orders:order:read`。再在相同配置类中加入 `PageDefinition`，提供稳定的 `pageId`、
中文标题键、图标、顺序、路由和所需权限。启动时目录会同步到数据库，现有角色仍需
由管理员显式授予新权限。

Flow 页面使用 `@Route` 注册，并在页面进入前检查与页面定义相同的权限。参考应用的
受保护页面通过 `SecuredView` 完成此检查；新页面应遵循同样的重定向策略：未登录进入
登录页，无权限进入 `access-denied`。页面上的按钮只是体验层控制，创建、修改、删除
等平台用例必须再次调用 `AuthorizationService`。

## 使用 Flow 设计系统

`admin-flow` 提供可组合的 Java Flow 页面模式，而不是另一个前端组件运行时。页面继续
直接使用标准 Vaadin 组件；只在重复的后台工作流中组合下列模式：

| 模式 | 用途 |
|---|---|
| `PageHeader` | 页面标题、说明、位置和页面级动作。 |
| `PageToolbar` | 查询字段、附加操作与主创建操作。 |
| `DataWorkspace<T>` | `Grid` 的稳定容器、选择数量、批量操作和忙碌/空/失败状态。 |
| `EditorDialog` | 响应式 `FormLayout`、字段校验提示与标准页脚操作。 |
| `EmptyState` | 有明确下一步的空数据或加载失败内容。 |
| `PagedGrid<T>` | 把服务端分页查询绑定到 `Grid`，并统一刷新和空表格文案。 |

下面是一个精简的页面组合示例。领域查询和命令仍是应用层依赖，示例不引入 Spring
类型到 `admin-flow`：

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

编辑器使用实际命令处理器保存，并把可恢复的字段错误显示在对话框内：

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

用 `workspace.setBusy(true)`、`workspace.showEmpty(...)`、
`workspace.showFailure(...)` 和 `workspace.showData()` 显式表示异步查询或加载
结果。不要把“无数据”伪装成成功表格，也不要在可复用模式里写入领域文案或权限判断。
批量操作的资格由页面提供；资格会变化时，页面调用
`workspace.refreshBulkActions()`。

主题属于应用层。参考应用通过 `ApplicationShell` 上的
`@Theme("admin-theme")` 注册
`src/main/frontend/themes/admin-theme/theme.json` 和 `styles.css`。新应用应复制或
新建自己的命名主题，覆盖 `--admin-*` 语义变量，而不是修改 `admin-flow`。当前用户
菜单的深浅模式仅保存在 Vaadin session；它不是账户偏好设置。

应用外壳由参考应用的 `MainLayout` 组合：它使用 `AppLayout`、
`PageRegistry.visibleTo(...)` 和授权服务生成分组菜单，并在窄屏中通过
`DrawerToggle` 保持导航可达。扩展页面应复用该布局或实现同样的权限过滤与直接路由
保护；不能依据菜单隐藏来替代用例级授权。

## 新增业务模块

业务模块应定义自己的实体、查询模型、端口和用例。建议的调用方向是：

```text
Flow view -> business use case -> repository / audit / file-storage ports
                                     ^
                           Spring JPA or other adapter
```

将业务规则和授权放入用例，而非页面事件监听器；将 JPA 映射、SQL 和事务放入 Spring
适配器或参考应用的持久化实现。为每个变更用例增加权限检查和审计结果；为每个新增
表提供不可变的 Flyway 版本化迁移。核心模块不得导入 Spring、JPA、Flyway 或具体
适配器类型。

## 替换文件存储

`FileStorage` 是保存、读取和删除二进制内容的端口。默认的 `LocalFileStorage` 适合
本地开发和演示。生产对象存储适配器应以不透明 UUID 作为键，保留流式读取语义，
处理失败时提供可诊断日志，并在业务删除操作中调用同一删除语义。

新增适配器后，在组合根提供唯一的 `FileStorage` bean，并配置其凭据和存储位置。若
适配器带来新的事务、一致性、重试、删除延迟或安全语义，先记录架构决策并为失败与
恢复路径补充集成测试。

## 中文和品牌

当前参考 UI 以中文为先。新增页面、验证信息和导航标题应先提供中文；需要其他语言
时使用消息键和 Spring `MessageSource`，不要把业务规则绑定到展示文本。品牌名称、
主布局标题、导航文案以及主题变量应由应用层维护。保留 Vaadin 的可访问性和深浅主题
约定，不要修改可复用模块来硬编码某个组织的名称或颜色。

## HTTP 错误与可观测性

对自定义 HTTP API，复用 `ProblemDetailMapper` 将 `BusinessFailure` 映射为 RFC 9457
Problem Details；不要把 Spring 的 `ProblemDetail` 放进 contracts 或 platform。对 Flow
交互，复用 `FlowErrorMapper` 的字段校验、访问拒绝和安全失败呈现。

所有新的 HTTP 入口都应保留 `X-Correlation-Id` 的请求传播，并把关联 ID 写入日志与
相关审计事件。错误响应与日志只能包含安全的诊断信息。
