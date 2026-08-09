# Extension Guide

This guide describes the supported Spring Boot adoption path for independently
packaged Vaadin Flow administration modules. The current runtime is Spring
Boot only. It is compile-time Maven composition, not runtime plugin
installation: the host selects modules through normal Maven dependencies and
Spring Boot discovers each module's auto-configuration at startup.

`admin-contracts` and `admin-platform` remain Java-first and independent of
Spring. `admin-flow` owns Flow patterns and the Spring-free administration
module contract. `admin-spring-flow` is the Spring Boot adapter that aggregates
module descriptors, permissions, translation resources, and dynamic Flow
routes. The host owns its application layout and theme.

## Add An Administration Module

Use the independent orders example in
`admin-examples/admin-example-orders` as the complete working reference. A
host adopts that artifact with one Maven dependency:

```xml
<dependency>
  <groupId>io.github.vaadinadminstarter</groupId>
  <artifactId>admin-example-orders</artifactId>
  <version>${vaadin-admin-starter.version}</version>
</dependency>
```

The module itself depends on `admin-flow`, `admin-contracts`,
`spring-boot-autoconfigure`, and `vaadin-spring`. It must not depend on
`admin-reference-app`. The reference app includes the orders artifact solely
as a host-composition and acceptance-test example.

Every module contributes one `AdminModule` bean from a Boot auto-configuration.
The descriptor is the single source of truth for its navigation, route,
permission, and translation metadata:

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

Use stable identifiers. The module ID, group ID, page ID, route, permission
code, and message-bundle base name are public configuration identifiers. Page
title and intent keys must be prefixed with the module ID, for example
`orders.title` and `orders.intent`.

Do not add `@Route` to a contributed page. `admin-spring-flow` registers the
declared view type through Flow's public route configuration API, including the
host layout. A protected view extends `PermissionProtectedView` and returns the
same permission declared by its `AdminPage`; anonymous requests are rerouted to
`login`, and unauthorized requests are rerouted to `access-denied`.

The assembled catalog is authoritative. Do not define a host
`PermissionCatalog` bean. `admin-spring-flow` derives it from every enabled
`AdminModule`, synchronizes it through the existing host integration, and the
host administrator grants new permissions to existing roles in the normal way.

### Host Layout

The host declares its router layout once. Modules never import a concrete host
layout such as the reference app's `MainLayout`:

```java
@Bean
AdminHostLayout adminHostLayout() {
  return new AdminHostLayout(MainLayout.class);
}
```

### Translation Resources

The module declares its resource-bundle base name and provides both supported
locales in its own artifact:

```text
src/main/resources/
  orders/i18n/messages_zh_CN.properties
  orders/i18n/messages_en_US.properties
```

The composite provider resolves the selected `zh-CN` or `en-US` resource first,
then falls back to `zh-CN`; an unresolved key is logged and rendered as an
explicit marker. Use `LocaleChangeObserver` for visible view text that must
refresh after the user changes language. Navigation and workplace entries are
resolved from the descriptor's translation keys automatically.

### Collisions And Icons

Startup rejects duplicate module IDs, page IDs, routes, permission codes, and
message-bundle base names. A navigation group may be shared only when every
contribution has identical ID, title key, and order; the first declaration owns
its title key. Correct collisions in the contributing module rather than
depending on discovery order.

`iconKey` is validated at startup. The current `AdminIconCatalog` keys are
`briefcase`, `clock`, `history`, `key`, `shield`, `shopping-cart`, and `users`.
Choose one of these stable keys; unknown strings are configuration errors and
do not silently fall back to a generic icon.

## Use The Flow Design System

`admin-flow` provides composable Java Flow page patterns, not another frontend
component runtime. Pages continue to use standard Vaadin components directly;
compose the following patterns only for recurring administration workflows:

| Pattern | Purpose |
|---|---|
| `PageHeader` | Page title, description, location, and page-level actions. |
| `PageToolbar` | Query fields, secondary actions, and the primary create action. |
| `DataWorkspace<T>` | A stable `Grid` container, selection count, bulk actions, and busy, empty, or failure states. |
| `EditorDialog` | A responsive `FormLayout`, field-validation feedback, and standard footer actions. |
| `EmptyState` | Empty-data or loading-failure content with a clear next step. |
| `PagedGrid<T>` | Binds server-side paged queries to `Grid` and standardizes refresh behavior and empty-grid copy. |

The following is a concise page-composition example. Domain queries and commands
remain application-layer dependencies; the example does not introduce Spring
types into `admin-flow`:

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

The editor saves through a real command handler and displays recoverable field
errors inside the dialog:

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

Use `workspace.setBusy(true)`, `workspace.showEmpty(...)`,
`workspace.showFailure(...)`, and `workspace.showData()` to explicitly
represent asynchronous query or loading results. Do not present "no data" as a
successful grid, and do not place domain copy or permission decisions in
reusable patterns. The page provides bulk-action eligibility; when eligibility
changes, the page calls `workspace.refreshBulkActions()`.

Themes belong to the host application. It alone declares `@Theme` and controls
light/dark selection. Modules use the documented semantic `--admin-*` tokens
or existing Flow pattern classes, never a global theme or a competing brand
stylesheet. The canonical token contract covers surface, text, border, accent,
success, warning, danger, focus, spacing, typography, radius, and elevation;
see [Theme Tokens](theme-tokens.md). A module may add narrowly scoped CSS for
genuinely module-specific presentation, using those tokens.

The host's `MainLayout` renders grouped, permission-filtered navigation from
the assembled `AdminModuleRegistry`, and uses the same metadata for workplace
entries. Hidden navigation is not authorization: mutating use cases must check
`AuthorizationService` again.

## Add A Business Module

A business module should define its own entities, query models, ports, and use
cases. The recommended call direction is:

```text
Flow view -> business use case -> repository / audit / file-storage ports
                                     ^
                           Spring JPA or other adapter
```

Put business rules and authorization in use cases rather than page event
listeners. Put JPA mappings, SQL, and transactions in Spring adapters or in the
reference application's persistence implementation. Add a permission check and
an audit outcome to every mutation use case, and add an immutable versioned
Flyway migration for every new table. Core modules must not import Spring, JPA,
Flyway, or concrete adapter types.

## Replace File Storage

`FileStorage` is the port for storing, reading, and deleting binary content.
The default `LocalFileStorage` is suitable for local development and
demonstrations. A production object-storage adapter should use opaque UUIDs as
keys, retain streaming-read semantics, provide diagnosable logs on failure, and
invoke the same deletion semantics during a business deletion operation.

After adding an adapter, provide the single `FileStorage` bean in the
composition root and configure its credentials and storage location. If the
adapter introduces new transaction, consistency, retry, deletion-delay, or
security semantics, document the architecture decision first and add
integration tests for failure and recovery paths.

## HTTP Errors And Observability

For custom HTTP APIs, reuse `ProblemDetailMapper` to map `BusinessFailure` to
RFC 9457 Problem Details. Do not put Spring's `ProblemDetail` in contracts or
platform. For Flow interactions, reuse `FlowErrorMapper` for field validation,
access denial, and safe failure presentation.

Every new HTTP entry point must retain `X-Correlation-Id` request propagation
and write the correlation ID to logs and relevant audit events. Error responses
and logs may contain only safe diagnostic information.
