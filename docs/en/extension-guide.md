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

### Production Frontend Anchor

Dynamic registration makes a view routable at runtime, but the Vaadin
production frontend build cannot discover that view from a route annotation.
The host must therefore add one static `@Uses` annotation for every dynamically
registered module view to its composed `@Layout` class. This is a required
host-side production-bundle anchor; it does not replace the module descriptor,
and the module view still must not declare `@Route`:

```java
@Layout
@Uses(OrdersView.class)
public final class MainLayout extends AppLayout {
    // The host's common shell.
}
```

When the host composes additional module pages, add their view types here as
well. Keep the annotations in the host, rather than placing them in the module,
because the host decides which Maven modules are part of its production build.

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

### Adoption Checklist

- Add the module artifact as a normal Maven dependency in the host.
- Contribute exactly one `AdminModule` bean from the module's Boot
  auto-configuration, plus Spring-managed prototype view beans as needed.
- Do not declare `@Route` on a contributed view and do not define a host
  `PermissionCatalog`.
- Register the host `AdminHostLayout` and add `@Uses(ModuleView.class)` for
  every dynamically registered module view on that host layout.
- Package each declared message bundle with a complete `zh-CN` default
  resource. The startup validator also requires each navigation-group title,
  page title, and page intent key declared by the module. Add `en-US` for the
  second supported locale; its missing keys retain deterministic `zh-CN`
  fallback behavior.
- Run the host's production build after changing the composed view set.

### Verify A Local Consumer

Before publishing the starter, validate this adoption path from the repository
root with Docker available:

```bash
./scripts/verify-standalone-consumer.sh
```

The script installs all first-party `0.1.0-SNAPSHOT` artifacts into the local
Maven repository, then builds `verification/standalone-consumer` as an
independent Spring Boot host. That host depends on the starter and
`admin-example-orders` only by Maven coordinates, has its own layout and
bootstrap configuration, logs in with its local development administrator, and
opens the dynamically registered `/orders` page. It also runs the consumer's
production frontend package. This is a local repository acceptance check, not
publication to Maven Central and not a second reference application.

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
workspace.setFooter(pages.getPaginationBar());
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
success, warning, danger, focus, spacing, typography, radius, elevation, and
dense-workspace roles; see [Theme Tokens](theme-tokens.md). A module may add
narrowly scoped CSS for genuinely module-specific presentation, using those
tokens.

Modules must not import the host `admin-theme`, declare a global `@Theme`,
select an appearance profile or density, mutate global Lumo variables, or
depend on Ant-only selectors or Grid internals. Grid headers and rows, pager
footers, workspace states, and destructive confirmations are host-owned
presentation; modules use normal Flow state and dialog APIs. Those are
host-owned choices; see
[Appearance Profiles](appearance-profiles.md).

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

## Extend External Identity Mapping

`admin-contracts` exposes `ExternalIdentityMapper` for applications that opt
into Spring Security OIDC login. It accepts a framework-neutral
`ExternalIdentity` containing the normalized issuer URI, stable `sub`, optional
display name and email, and scalar string claims. It returns either the
application's existing `CurrentUser` or no result.

The consuming application owns the mapper and its data model. Match the
issuer-and-subject pair to a pre-existing, enabled local account; do not make
email, display name, or an unverified group claim the primary identity key.
The starter's OIDC adapter recreates its standard local principal only after
that mapping succeeds, so existing route checks, authorization use cases,
auditing, and authentication-version invalidation keep one local-user model.

Configure any standards-compliant issuer through Spring Security's client
registration and set `vaadin-admin.oidc.registration-id` to that registration's
ID. The corresponding redirect URI is
`{baseUrl}/login/oauth2/code/{registrationId}`. See [Configure Optional OIDC
Login](quick-start.md#configure-optional-oidc-login) for the complete
provider-neutral configuration. Keycloak is test-only; mainland-China, global,
and self-hosted providers use the same standard OIDC path.

Do not place enterprise authorization policy in a reusable module or the
starter's mapper SPI. Group-to-role translation, just-in-time provisioning,
deprovisioning, SCIM, MFA, SAML, LDAP integration, tenant selection, and
data-scope enforcement remain consumer extensions. An OIDC login that cannot
be mapped to an enabled local account must be denied rather than creating an
account or granting a default role.
