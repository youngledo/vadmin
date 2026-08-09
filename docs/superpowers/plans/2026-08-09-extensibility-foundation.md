# Extensibility Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Spring Boot reference application host independently packaged,
permission-aware, internationalized Flow administration modules with a stable
theme-token contract.

**Architecture:** `admin-flow` owns immutable module metadata and registry
validation without Spring dependencies. `admin-spring-flow` is the Spring Boot
adapter that assembles descriptors, translations, and Flow routes into a host
application. The reference application becomes one host plus built-in modules;
an independent orders artifact proves the public Maven extension path.

**Tech Stack:** Java 25, Maven 4 wrapper, Spring Boot 4.1, Vaadin Flow 25.2,
Vaadin `RouteConfiguration` and `I18NProvider`, JUnit 5, AssertJ, ArchUnit,
Playwright, Testcontainers PostgreSQL.

---

## Planned File Structure

| Path | Responsibility |
| --- | --- |
| `admin-flow/.../navigation/AdminModule.java` | Spring-free module descriptor API. |
| `admin-flow/.../navigation/AdminNavigationGroup.java` | Immutable navigation-group metadata. |
| `admin-flow/.../navigation/AdminPage.java` | Immutable page, permission, message, and view metadata. |
| `admin-flow/.../navigation/AdminModuleRegistry.java` | Deterministic metadata aggregation and collision validation. |
| `admin-flow/.../navigation/AdminHostLayout.java` | Host layout type exposed without reference-app coupling. |
| `admin-flow/.../navigation/PermissionProtectedView.java` | Reusable direct-route authorization base for external Flow views. |
| `admin-contracts/.../auth/CurrentUserProvider.java` | Runtime-neutral lookup port for the authenticated user. |
| `admin-spring/admin-spring-flow/` | Spring Boot collection, route registration, and i18n adapter. |
| `admin-reference-app/.../modules/` | Built-in system/customer/workplace descriptors. |
| `admin-reference-app/.../i18n/` | Reference application `zh-CN` and `en-US` resource bundles. |
| `admin-examples/admin-example-orders/` | External-package Maven module that proves the adoption path. |
| `docs/en/extension-guide.md` | Public Spring Boot module, i18n, and theme extension guide. |

### Task 1: Add The Spring Flow Adapter And Example Reactor Topology

**Files:**
- Modify: `pom.xml`
- Modify: `admin-spring/pom.xml`
- Create: `admin-spring/admin-spring-flow/pom.xml`
- Create: `admin-examples/pom.xml`
- Create: `admin-examples/admin-example-orders/pom.xml`
- Modify: `admin-reference-app/pom.xml`

- [ ] **Step 1: Add a failing reactor-resolution check for the two new artifacts**

Add the modules to the root and their parent reactors before declaring an
implementation. The intended topology is:

```xml
<subprojects>
    <subproject>admin-spring-security</subproject>
    <subproject>admin-spring-jpa</subproject>
    <subproject>admin-spring-boot</subproject>
    <subproject>admin-spring-flow</subproject>
</subprojects>
```

and:

```xml
<subprojects>
    <subproject>admin-example-orders</subproject>
</subprojects>
```

Run: `./mvnw -B -ntp -pl admin-spring-flow,admin-example-orders test`

Expected: FAIL because the two POMs do not yet exist.

- [ ] **Step 2: Create the POMs with only boundary-appropriate dependencies**

Create `admin-spring-flow/pom.xml` with `admin-flow`, `admin-contracts`,
`vaadin-spring`, `spring-boot-starter`, and test dependencies. Create the
orders module with `admin-flow`, `admin-contracts`, `spring-boot-autoconfigure`,
and `vaadin-spring`; it must not depend on `admin-reference-app`.

Use this dependency shape in the orders artifact:

```xml
<dependency>
    <groupId>io.github.vaadinadminstarter</groupId>
    <artifactId>admin-flow</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
</dependency>
```

Add `admin-spring-flow` and `admin-example-orders` as dependencies of
`admin-reference-app` only after their POMs resolve.

- [ ] **Step 3: Verify the topology**

Run: `./mvnw -B -ntp -pl admin-spring-flow,admin-example-orders -am test`

Expected: PASS with both new, empty modules resolved from the reactor.

- [ ] **Step 4: Commit the topology**

```bash
git add pom.xml admin-spring/pom.xml admin-spring/admin-spring-flow/pom.xml \
  admin-examples/pom.xml admin-examples/admin-example-orders/pom.xml \
  admin-reference-app/pom.xml
git commit -m "build: add Flow extension modules"
```

### Task 2: Define And Test The Spring-Free Module Metadata Contract

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminModule.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminNavigationGroup.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminPage.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminMessageBundle.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminHostLayout.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminModuleRegistry.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/PermissionProtectedView.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/auth/CurrentUserProvider.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/PageRegistry.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminModuleRegistryTest.java`
- Modify: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/PageRegistryTest.java`

- [ ] **Step 1: Write failing registry tests for visible ordering and collisions**

Create descriptors with pages in different groups and assert group/page order,
permission filtering, and actionable errors. The test fixtures must use a
minimal `TestView extends Div` so they do not depend on Spring:

```java
var orders = AdminModule.of("orders",
        List.of(new AdminNavigationGroup("business", "nav.business", 200)),
        List.of(new AdminPage("orders.list", "business", "orders.list.title",
                "orders.list.intent", "briefcase", 100, "orders",
                PermissionCode.of("orders:read"), TestView.class)),
        Set.of(PermissionCode.of("orders:read")),
        List.of(new AdminMessageBundle("orders", "orders.i18n.messages")));

assertThat(registry.visibleTo(userWithOrdersRead, authorization))
        .extracting(AdminPage::route)
        .containsExactly("orders");
```

Add tests asserting duplicate route errors include both `orders` and
`invoices`, and incompatible reuse of `business` fails rather than following
iteration order.

Run: `./mvnw -B -ntp -pl admin-flow test -Dtest=AdminModuleRegistryTest`

Expected: FAIL because the contract classes do not exist.

- [ ] **Step 2: Implement immutable records and the aggregate registry**

Implement non-null, non-blank validation in record compact constructors.
`AdminModuleRegistry` must flatten descriptors once, validate all identifiers,
and expose the exact APIs required by later tasks:

```java
public final class AdminModuleRegistry {
    public AdminModuleRegistry(Collection<AdminModule> modules) { ... }
    public List<AdminNavigationGroup> groupsVisibleTo(CurrentUser user,
            AuthorizationService authorization) { ... }
    public List<AdminPage> pagesVisibleTo(CurrentUser user,
            AuthorizationService authorization) { ... }
    public Set<PermissionCode> permissionCatalog() { ... }
    public List<AdminMessageBundle> messageBundles() { ... }
    public List<AdminPage> pages() { ... }
}
```

An `AdminNavigationGroup` may be contributed by multiple modules only when its
metadata is exactly equal. Its title key is owned by the first declaration and
reused unchanged; every `AdminPage` title and intent key remains prefixed by
the page module ID.

Keep `PageRegistry` as a deprecated compatibility facade only if a caller
remains after the reference-app migration; otherwise remove it and migrate its
single test in the same commit. Do not make this API depend on Spring classes.

Introduce the public authentication port and view base required by an external
module:

```java
public interface CurrentUserProvider {
    Optional<CurrentUser> currentUser();
}

public abstract class PermissionProtectedView extends VerticalLayout
        implements BeforeEnterObserver {
    protected PermissionProtectedView(CurrentUserProvider currentUser,
            AuthorizationService authorization) { ... }
    protected abstract PermissionCode requiredPermission();
    protected final CurrentUser requireCurrentUser() { ... }
    @Override public final void beforeEnter(BeforeEnterEvent event) { ... }
}
```

Its `beforeEnter` behavior is exactly the current `SecuredView` behavior:
reroute anonymous users to `login`, and unauthorized users to `access-denied`.

- [ ] **Step 3: Run the focused contract tests**

Run: `./mvnw -B -ntp -pl admin-flow test -Dtest=AdminModuleRegistryTest,PageRegistryTest`

Expected: PASS.

- [ ] **Step 4: Add an architecture assertion for the public contract**

Extend `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java`
so all classes under `..flow.navigation..` remain free of
`org.springframework..`, `jakarta.persistence..`, `org.flywaydb..`, and
`..app..`. Run its test and confirm the new metadata types satisfy it.

- [ ] **Step 5: Commit the core contract**

```bash
git add admin-flow/src/main admin-flow/src/test \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java
git commit -m "feat: add Flow administration module contract"
```

### Task 3: Implement Spring Boot Assembly, Safe Route Registration, And I18n Provider

**Files:**
- Create: `admin-spring/admin-spring-flow/src/main/java/io/github/vaadinadminstarter/springflow/AdminFlowAutoConfiguration.java`
- Create: `admin-spring/admin-spring-flow/src/main/java/io/github/vaadinadminstarter/springflow/navigation/SpringAdminModuleAssembler.java`
- Create: `admin-spring/admin-spring-flow/src/main/java/io/github/vaadinadminstarter/springflow/navigation/AdminModuleRouteRegistrar.java`
- Create: `admin-spring/admin-spring-flow/src/main/java/io/github/vaadinadminstarter/springflow/i18n/CompositeAdminI18NProvider.java`
- Create: `admin-spring/admin-spring-flow/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `admin-spring/admin-spring-flow/src/test/java/io/github/vaadinadminstarter/springflow/navigation/AdminModuleRouteRegistrarIT.java`
- Create: `admin-spring/admin-spring-flow/src/test/java/io/github/vaadinadminstarter/springflow/i18n/CompositeAdminI18NProviderTest.java`

- [ ] **Step 1: Write the route-registration integration test first**

Use a `@SpringBootTest` test application whose `@SpringBootApplication` package
does not contain `ExternalOrdersView`. Import an `AdminModule` bean referring
to that external class and a host-layout bean. Assert the application route
registry has the route and the view receives its constructor-injected test
service:

```java
assertThat(RouteConfiguration.forRegistry(ApplicationRouteRegistry
        .getInstance(vaadinContext)).getRoute("orders"))
        .contains(ExternalOrdersView.class);
assertThat(context.getBean(ExternalOrdersView.class).service()).isSameAs(service);
```

Run: `./mvnw -B -ntp -pl admin-spring-flow test -Dtest=AdminModuleRouteRegistrarIT`

Expected: FAIL because the adapter does not exist.

- [ ] **Step 2: Implement collection and route registration using public Flow APIs**

`SpringAdminModuleAssembler` receives `List<AdminModule>`, creates one
`AdminModuleRegistry`, and exposes its permission catalog and message bundles.
`AdminModuleRouteRegistrar` must register every declared page in one atomic
`RouteConfiguration.update` operation, use the host layout from
`AdminHostLayout`, and reject an already occupied route before registration.

The registrar must not call Flow internal startup initializers. Its essential
operation is:

```java
RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(context))
        .update(() -> pages.forEach(page -> configuration.setRoute(
                page.route(), page.viewType(), hostLayout.layoutType())));
```

Wire it from a `VaadinServiceInitListener` owned by the Spring adapter, and
make the listener idempotent per Vaadin service.

- [ ] **Step 3: Write failing i18n tests**

Create core and external test bundles with the same locale. Assert resolution
uses the selected bundle, falls back to `zh-CN`, formats parameters, and
returns `!en-US: orders.missing!` while logging a warning for a missing key.
Assert `getProvidedLocales()` returns only `Locale.forLanguageTag("zh-CN")`
and `Locale.forLanguageTag("en-US")` in deterministic order.

Run: `./mvnw -B -ntp -pl admin-spring-flow test -Dtest=CompositeAdminI18NProviderTest`

Expected: FAIL because the provider does not exist.

- [ ] **Step 4: Implement the aggregated `I18NProvider` and locale policy**

Use each `AdminMessageBundle` base name as a distinct Java `ResourceBundle`.
Require the module-key prefix before resolving, select the matching bundle,
and then attempt selected locale followed by `zh-CN`. Do not merge resource
files with a shared base name.

Expose supported locales and a small session-scoped locale preference helper:

```java
public Locale selectInitialLocale(Locale browserLocale) {
    return providedLocales.contains(browserLocale) ? browserLocale : ZH_CN;
}

public void select(UI ui, Locale locale) {
    requireSupported(locale);
    VaadinSession.getCurrent().setAttribute(LOCALE_SESSION_KEY, locale);
    ui.setLocale(locale);
}
```

Auto-configuration must publish the `I18NProvider`, registry, and route
registrar only when the host supplies `AdminHostLayout`.

- [ ] **Step 5: Verify the adapter and commit it**

Run: `./mvnw -B -ntp -pl admin-spring-flow test`

Expected: PASS.

```bash
git add admin-spring/admin-spring-flow
git commit -m "feat: assemble Flow administration modules in Spring Boot"
```

### Task 4: Convert Built-In Navigation To Host And Module Descriptors

**Files:**
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/modules/ReferenceAdminModules.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/ApplicationConfiguration.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/HomeView.java`
- Delete: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/SecuredView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/PermissionsView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/AuditView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationContextIT.java`
- Modify: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/auth/SecurityContextCurrentUserProvider.java`

- [ ] **Step 1: Change the context integration test to expect assembled modules**

Replace the old `PageRegistry` assertion with `AdminModuleRegistry` assertions:

```java
assertThat(registry.pagesVisibleTo(administrator, new SpringAuthorizationService()))
        .extracting(AdminPage::route)
        .containsExactly("users", "roles", "permissions", "audit", "customers", "orders");
assertThat(registry.permissionCatalog())
        .contains(PermissionCode.of("orders:read"));
```

Run: `./mvnw -B -ntp -pl admin-reference-app -am test -Dtest=ApplicationContextIT`

Expected: FAIL because built-in pages still use the hand-built catalog and
page registry.

- [ ] **Step 2: Create built-in descriptors and migrate the catalog**

Define one descriptor for system administration and one for customer
administration. Use message keys, not Chinese labels, for groups, titles,
intents, and page titles. Have `ApplicationConfiguration` contribute only
host-specific services; the permission catalog must be created from the
assembled registry:

```java
@Bean
PermissionCatalog permissionCatalog(AdminModuleRegistry modules) {
    return new PermissionCatalog(modules.permissionCatalog());
}

@Bean
AdminHostLayout adminHostLayout() {
    return AdminHostLayout.of(MainLayout.class);
}
```

Remove the manual `List.of(new PageDefinition(...))` construction.

- [ ] **Step 3: Render navigation and workplace from metadata**

Make `MainLayout` render `groupsVisibleTo` and pages in each group, translate
each `titleKey`, and derive the current location from the active route rather
than a `switch`. Make `HomeView` render title and intent from `AdminPage` and
remove its page-ID `switch` methods.

The route views must no longer use `@Route(... layout = MainLayout.class)`;
the adapter owns their registration. Preserve `@PermitAll` and `SecuredView`
guards so direct-navigation authorization remains enforced. Replace the
reference application's package-private `SecuredView` with the public
`PermissionProtectedView`, and make `SecurityContextCurrentUserProvider`
implement `CurrentUserProvider` so host and external views only inject the
contract type. Mark every built-in programmatically registered page with
Spring's `@Component`, for example:

```java
@Component
@PermitAll
public final class UsersView extends PermissionProtectedView { ... }
```

This ensures constructor injection remains available after removing `@Route`.

- [ ] **Step 4: Verify built-in migration**

Run: `./mvnw -B -ntp -pl admin-reference-app -am test -Dtest=ApplicationContextIT,ArchitectureTest`

Expected: PASS, including the customer module and bootstrap administrator tests.

- [ ] **Step 5: Commit the host conversion**

```bash
git add admin-reference-app/src/main admin-reference-app/src/test/java/io/github/vaadinadminstarter/app
git commit -m "feat: assemble reference navigation from modules"
```

### Task 5: Localize Shared Flow Surfaces And Reference Views

**Files:**
- Create: `admin-reference-app/src/main/resources/i18n/reference_zh_CN.properties`
- Create: `admin-reference-app/src/main/resources/i18n/reference_en_US.properties`
- Create: `admin-flow/src/main/resources/i18n/flow_zh_CN.properties`
- Create: `admin-flow/src/main/resources/i18n/flow_en_US.properties`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageHeader.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageToolbar.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialog.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DetailDialog.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/EditorDialog.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/OperationFeedback.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/error/AccessDeniedView.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/error/SystemFailureView.java`
- Modify: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/ui/LoginView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/*.java`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/i18n/ReferenceLocaleIT.java`

- [ ] **Step 1: Write a failing locale-change integration test**

Build a view with a `PageHeader`, a confirmation dialog, and a translated
navigation label. Set the UI to `zh-CN`, then `en-US`, and assert the same
component instances expose translated text after each change. Use
`LocaleChangeObserver` rather than recreating the session.

```java
ui.setLocale(Locale.forLanguageTag("en-US"));
view.localeChange(new LocaleChangeEvent(ui, ui.getLocale()));
assertThat(header.getTitle().getText()).isEqualTo("Users");
```

Run: `./mvnw -B -ntp -pl admin-reference-app -am test -Dtest=ReferenceLocaleIT`

Expected: FAIL because text is currently hard-coded.

- [ ] **Step 2: Add key-based text support to shared patterns**

Add overloads that accept `String translationKey, Object... parameters` and
make visible long-lived components implement `LocaleChangeObserver`. Existing
literal-taking constructors may remain temporarily for source compatibility,
but reference views must no longer call them with user-visible literals.

Use this update shape:

```java
private void updateText() {
    title.setText(getTranslation(titleKey, titleParameters));
    description.setText(getTranslation(descriptionKey, descriptionParameters));
}

@Override
public void localeChange(LocaleChangeEvent event) {
    updateText();
}
```

- [ ] **Step 3: Replace reference literals and add two complete bundles**

Move every user-facing Chinese string in the shell, workplace, users, roles,
permissions, audit, customers, login, access-denied, and system-failure
surfaces to `reference_zh_CN.properties`, with equal English keys in
`reference_en_US.properties`. Register the `flow` and `reference` bundle
descriptors with the host. Keep domain values such as usernames and permission
codes untranslated.

- [ ] **Step 4: Add the shell language control**

Use a labelled Flow `Select<Locale>` or menu action in `MainLayout`. Its item
labels come from the currently selected locale, it appears only when more than
one supported locale exists, and its value is bound to the session locale
helper. Retain the existing theme menu behavior.

- [ ] **Step 5: Verify the locale work and commit it**

Run: `./mvnw -B -ntp -pl admin-flow,admin-spring-flow,admin-reference-app -am test`

Expected: PASS.

```bash
git add admin-flow admin-spring/admin-spring-flow admin-spring/admin-spring-security admin-reference-app
git commit -m "feat: localize Flow administration surfaces"
```

### Task 6: Publish And Enforce The Theme-Token Contract

**Files:**
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`
- Create: `docs/en/theme-tokens.md`

- [ ] **Step 1: Write a failing token-contract test**

Load `styles.css` as UTF-8 and assert every required semantic token is declared
in the root and the dark selector: `surface`, `surface-raised`, `text-primary`,
`text-secondary`, `border`, `accent`, `success`, `warning`, `danger`, `focus`,
`space-sm`, `space-md`, `radius-control`, and `elevation-raised`.

Run: `./mvnw -B -ntp -pl admin-reference-app -am test -Dtest=AdminThemeTokenTest`

Expected: FAIL because the legacy names do not yet provide the documented
semantic contract.

- [ ] **Step 2: Normalize tokens without changing the established visual language**

Retain legacy aliases where existing CSS needs them, but define canonical
semantic variables and map Lumo values only through them:

```css
:root {
  --admin-surface: #ffffff;
  --admin-text-primary: #18212b;
  --admin-accent: #146c94;
  --admin-space-md: 1rem;
  --admin-radius-control: 0.375rem;
  --lumo-primary-color: var(--admin-accent);
}
```

Mirror the same names in the dark selector. Replace page-pattern CSS literal
colors and dimensions only where a documented token already expresses the
meaning; do not redesign unrelated visual details.

- [ ] **Step 3: Document host and module responsibilities**

Document the token table, light/dark override examples, and the rule that
modules consume tokens and scoped component classes but do not declare a
global `@Theme` or override Lumo globally. Add the English source document only;
the separately paused documentation-internationalization work owns translation
of this new content.

- [ ] **Step 4: Verify and commit the token contract**

Run: `./mvnw -B -ntp -pl admin-reference-app -am test -Dtest=AdminThemeTokenTest`

Expected: PASS.

```bash
git add admin-reference-app/src/main/frontend/themes/admin-theme/styles.css \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java \
  docs/en/theme-tokens.md
git commit -m "feat: publish Flow admin theme tokens"
```

### Task 7: Build The Independent Orders Example Artifact

**Files:**
- Create: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersAdminModule.java`
- Create: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersAutoConfiguration.java`
- Create: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java`
- Create: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrderRow.java`
- Create: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrderQueryService.java`
- Create: `admin-examples/admin-example-orders/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `admin-examples/admin-example-orders/src/main/resources/orders/i18n/messages_zh_CN.properties`
- Create: `admin-examples/admin-example-orders/src/main/resources/orders/i18n/messages_en_US.properties`
- Create: `admin-examples/admin-example-orders/src/test/java/com/example/orders/admin/OrdersAdminModuleTest.java`

- [ ] **Step 1: Write failing descriptor and view tests**

Assert the descriptor has module ID `orders`, group key `orders.nav.group`,
route `orders`, permission `orders:read`, external view type, and two message
bundle locales. Assert the deterministic query service returns rows that the
view can display through `DataWorkspace`.

Run: `./mvnw -B -ntp -pl admin-example-orders -am test -Dtest=OrdersAdminModuleTest`

Expected: FAIL because the example module does not exist.

- [ ] **Step 2: Implement the minimal complete example**

Keep the module's application boundary explicit:

```java
@AutoConfiguration
public class OrdersAutoConfiguration {
    @Bean AdminModule ordersAdminModule() { return new OrdersAdminModule(); }
    @Bean OrderQueryService orderQueryService() { return OrderQueryService.demo(); }
    @Bean OrdersView ordersView(OrderQueryService service) { return new OrdersView(service); }
}
```

`OrdersView` must extend `PermissionProtectedView`, inject only
`CurrentUserProvider`, `AuthorizationService`, and its own query service,
return `PermissionCode.of("orders:read")` from `requiredPermission()`, render a
read-only grid and detail action, and implement locale changes for visible
text. It must use
`admin-*` CSS tokens only through existing pattern class names and contain no
global stylesheet.

- [ ] **Step 3: Verify independent packaging**

Run: `./mvnw -B -ntp -pl admin-example-orders -am test`

Expected: PASS. Also run:

```bash
./mvnw -B -ntp -pl admin-example-orders dependency:tree \
  -Dincludes=io.github.vaadinadminstarter:admin-reference-app
```

Expected: no `admin-reference-app` dependency in the output.

- [ ] **Step 4: Commit the example artifact**

```bash
git add admin-examples
git commit -m "feat: add independent orders administration example"
```

### Task 8: Add End-To-End, Architecture, And Public Adoption Coverage

**Files:**
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationContextIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`
- Modify: `docs/en/extension-guide.md`
- Modify: `README.md`

- [ ] **Step 1: Add failing browser scenarios**

Add tests that sign in as an orders-only user and prove the module appears in
the business navigation group and workplace, then navigate directly to
`/orders`. Add a locale test that switches to English and proves the shell,
orders group, orders heading, and an operation label change. Add a dark-mode
test that checks the orders workspace is visible and its computed background
and foreground differ from the light-mode values without overlap.

Run: `./mvnw -B -ntp -pl admin-reference-app -am verify -DskipTests=false`

Expected: FAIL until the external module, i18n, and tokens are integrated.

- [ ] **Step 2: Strengthen architecture boundaries**

Add ArchUnit rules that prohibit `admin-example-orders` from depending on
`..app..`, forbid `admin-flow` from importing Spring, and constrain
`..springflow..` dependencies to the new `admin-spring-flow` module. Keep the
existing core/Spring/JPA boundary rule intact.

- [ ] **Step 3: Document the supported adoption path**

Update the English extension guide with one complete Maven dependency block,
`AdminModule` descriptor example, translation resource layout, host-layout
bean, collision rules, and theme-token rules. State clearly that this is
compile-time Maven composition, not runtime plugin installation, and that
Spring Boot is the only supported host runtime. Leave the Chinese counterpart
unchanged until the paused documentation-internationalization work resumes.

- [ ] **Step 4: Run focused verification**

Run: `./mvnw -B -ntp -pl admin-reference-app -am verify`

Expected: PASS, including Testcontainers and Playwright browser tests.

- [ ] **Step 5: Commit coverage and adoption documentation**

```bash
git add admin-reference-app/src/test docs/en/extension-guide.md \
  README.md
git commit -m "test: verify Flow module adoption workflow"
```

### Task 9: Run Full Release Verification And Record The Phase Outcome

**Files:**
- Modify: `docs/en/architecture.md`
- Modify: `docs/superpowers/specs/2026-08-09-product-roadmap-design.md`

- [ ] **Step 1: Run the complete normal verification suite**

Run: `./mvnw -B -ntp verify`

Expected: PASS for all reactor modules, including the independent example and
reference-app integration/browser tests.

- [ ] **Step 2: Run the production verification suite**

Run: `./mvnw -B -ntp -Pproduction verify`

Expected: PASS after Vaadin production frontend build completes.

- [ ] **Step 3: Run Compose validation**

Run: `docker compose config -q`

Expected: exit code 0.

- [ ] **Step 4: Update architecture and roadmap outcome**

Document the final extension boundaries, Spring-specific adapter location,
supported UI locales, and host-controlled theme tokens in the English source
architecture document. Change the roadmap's Phase 3 entry only after the
preceding commands pass; do not advance Phase 4 or add a second runtime.

- [ ] **Step 5: Commit the verified phase documentation**

```bash
git add docs/en/architecture.md \
  docs/superpowers/specs/2026-08-09-product-roadmap-design.md
git commit -m "docs: record extensibility foundation outcome"
```
