# Extension Guide

English | [简体中文](../zh-CN/extension-guide.md)

This project is a modular monolith. `admin-contracts` defines Java-level ports
and shared models, while `admin-platform` defines authorized business use
cases. Flow, Spring Security, JPA, and Spring Boot belong to the adapter layer;
`admin-reference-app` is the composition root and sample business module.
Organize new business capabilities along this dependency direction. Do not let
Flow views call JPA repositories directly.

## Add Permissions And Pages

First, add a stable permission code to
`ApplicationConfiguration.permissionCatalog()`, for example
`orders:order:read`. Then add a `PageDefinition` in the same configuration
class. It supplies a stable `pageId`, a Chinese title key, icon, order, route,
and required permission. At startup, the catalog is synchronized to the
database; an administrator must still explicitly grant the new permission to
existing roles.

Register Flow pages with `@Route` and check the same permission as the page
definition before entering the page. Protected pages in the reference
application perform this check through `SecuredView`. New pages should follow
the same redirect policy: unauthenticated users go to the login view, and users
without permission go to `access-denied`. Buttons on pages are only
user-experience controls. Platform use cases for creation, updates, deletion,
and other mutations must call `AuthorizationService` again.

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

Themes belong to the application layer. The reference application uses
`ApplicationShell` and `@Theme("admin-theme")` to register
`src/main/frontend/themes/admin-theme/theme.json` and `styles.css`. A new
application should copy or create its own named theme and override the
`--admin-*` semantic variables instead of modifying `admin-flow`. The
light/dark mode in the current-user menu is stored only in the Vaadin session;
it is not an account preference.

The reference application's `MainLayout` composes the application shell. It
uses `AppLayout`, `PageRegistry.visibleTo(...)`, and the authorization service
to create grouped navigation, and keeps navigation reachable on narrow screens
through `DrawerToggle`. Extension pages should reuse this layout or implement
the same permission filtering and direct-route protection. Hidden navigation
does not replace use-case-level authorization.

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

## Chinese-First UI And Branding

The current reference UI is Chinese-first. Provide Chinese text first for new
pages, validation messages, and navigation titles. When other languages are
needed, use message keys and Spring `MessageSource`; do not bind business rules
to presentation text. The application layer owns brand names, main-layout
titles, navigation copy, and theme variables. Retain Vaadin accessibility and
light/dark-theme conventions, and do not modify reusable modules to hard-code a
specific organization's name or colors.

## HTTP Errors And Observability

For custom HTTP APIs, reuse `ProblemDetailMapper` to map `BusinessFailure` to
RFC 9457 Problem Details. Do not put Spring's `ProblemDetail` in contracts or
platform. For Flow interactions, reuse `FlowErrorMapper` for field validation,
access denial, and safe failure presentation.

Every new HTTP entry point must retain `X-Correlation-Id` request propagation
and write the correlation ID to logs and relevant audit events. Error responses
and logs may contain only safe diagnostic information.
