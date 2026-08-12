# Ant Flow Profile Phase A Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the existing `ant` profile visibly distinct in the application shell and repeated action icons without changing Flow business behavior or the parallel `vaadin` profile.

**Architecture:** Add a closed, profile-neutral icon vocabulary and `AdminIcon` Flow component in `admin-flow`. It renders a Vaadin fallback SVG plus stable `data-admin-icon` semantics; the host-owned `admin-theme` replaces its visible glyph with a small vendored neutral SVG-mask set only under the `ant` root attribute. The same host theme receives scoped shell rules; reference and external Orders views use the facade rather than directly selecting Vaadin icons.

**Tech Stack:** Java 25, Vaadin Flow 25.2, Spring Boot 4.1, CSS custom properties and `::part`, vendored SVG masks with license notice, JUnit 5, AssertJ, Playwright, Testcontainers PostgreSQL, Maven 4.

---

## File Structure

- Create `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconName.java`: closed semantic icon vocabulary, CSS value, and Vaadin fallback.
- Create `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIcon.java`: profile-neutral Flow wrapper with stable DOM semantics.
- Modify `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconCatalog.java`: map module navigation keys to `AdminIconName` while preserving `create(String)` compatibility.
- Create `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminIconTest.java`: semantic component and catalog tests.
- Modify `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`: use profile-neutral icons in shell and navigation.
- Modify `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`, `CustomersView.java`, `RolesView.java`, and `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java`: replace repeated direct `VaadinIcon` action use with `AdminIcon`.
- Create `admin-reference-app/src/main/frontend/themes/admin-theme/icons/*.svg`: small neutral line-icon mask source set.
- Create `admin-reference-app/src/main/frontend/themes/admin-theme/icons/LICENSE`: upstream icon license notice and asset provenance.
- Modify `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`: Ant-only icon mask, shell, navigation, and narrow-screen rules.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`: CSS icon/shell contract coverage.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java` and `AntCompactVisualLanguageE2EIT.java`: browser assertions for semantic icon rendering, shell geometry, and accessibility.
- Modify `docs/en/appearance-profiles.md`, `docs/zh-CN/appearance-profiles.md`, `docs/en/theme-tokens.md`, and `docs/zh-CN/extension-guide.md`: document icon consumer rules and Phase A scope.

### Task 1: Add The Profile-Neutral Icon Facade

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconName.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIcon.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconCatalog.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminIconTest.java`
- Modify: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminModuleRegistryTest.java`

- [ ] **Step 1: Write failing semantic-icon tests**

```java
@Test
void rendersAStableSemanticWrapperAroundTheVaadinFallback() {
    var icon = AdminIcon.of(AdminIconName.EYE);

    assertThat(icon.getClassNames()).contains("admin-icon");
    assertThat(icon.getElement().getAttribute("data-admin-icon")).isEqualTo("eye");
    assertThat(icon.getElement().getAttribute("aria-hidden")).isEqualTo("true");
    assertThat(icon.getChildren()).hasSize(1);
}

@Test
void resolvesEverySupportedNavigationKeyToAStableIconName() {
    assertThat(AdminIconCatalog.iconName("shopping-cart")).isEqualTo(AdminIconName.SHOPPING_CART);
    assertThat(AdminIconCatalog.iconName("users")).isEqualTo(AdminIconName.USERS);
    assertThatThrownBy(() -> AdminIconCatalog.iconName("untrusted"))
            .isInstanceOf(IllegalArgumentException.class);
}
```

Extend the existing registry test to retain its compatibility assertion:

```java
assertThat(AdminIconCatalog.create("shopping-cart")).isNotNull();
assertThat(AdminIconCatalog.createAdminIcon("shopping-cart")
        .getElement().getAttribute("data-admin-icon")).isEqualTo("shopping-cart");
```

- [ ] **Step 2: Run the focused tests and verify they fail**

Run:

```bash
./mvnw -B -ntp -pl :admin-flow -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=AdminIconTest,AdminModuleRegistryTest test
```

Expected: compilation fails because `AdminIcon` and `AdminIconName` do not exist.

- [ ] **Step 3: Add the closed vocabulary and wrapper**

Create the enum with the names used by Phase A. Keep values semantic and
profile-neutral:

```java
public enum AdminIconName {
    ADD("add", VaadinIcon.PLUS),
    ATTACHMENT("attachment", VaadinIcon.PAPERCLIP),
    BRIEFCASE("briefcase", VaadinIcon.BRIEFCASE),
    CLOCK("clock", VaadinIcon.CLOCK),
    CUBE("cube", VaadinIcon.CUBE),
    DELETE("delete", VaadinIcon.TRASH),
    EDIT("edit", VaadinIcon.EDIT),
    EYE("eye", VaadinIcon.EYE),
    GLOBE("globe", VaadinIcon.GLOBE),
    HISTORY("history", VaadinIcon.TIME_BACKWARD),
    HOME("home", VaadinIcon.HOME),
    KEY("key", VaadinIcon.KEY),
    PALETTE("palette", VaadinIcon.PALETTE),
    PAUSE("pause", VaadinIcon.PAUSE),
    PLAY("play", VaadinIcon.PLAY),
    SHIELD("shield", VaadinIcon.SHIELD),
    SHOPPING_CART("shopping-cart", VaadinIcon.CART),
    USERS("users", VaadinIcon.USERS);
}
```

`AdminIcon.of(name)` must create a `Span` with class `admin-icon`,
`data-admin-icon` equal to `name.cssValue()`, `aria-hidden="true"`, and one
child from `name.vaadinIcon().create()`. Do not select the profile in Java.

Change the catalog to use a `Map<String, AdminIconName>`. Keep
`public static Icon create(String iconKey)` returning the original Vaadin
fallback for binary/source compatibility. Add
`public static AdminIcon createAdminIcon(String iconKey)` and
`public static AdminIconName iconName(String iconKey)` for new consumers.

- [ ] **Step 4: Run the focused tests and verify they pass**

Run the command from Step 2.

Expected: `AdminIconTest` and `AdminModuleRegistryTest` pass without adding
Spring dependencies to `admin-flow`.

- [ ] **Step 5: Commit the icon contract**

```bash
git add admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconName.java \
  admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIcon.java \
  admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/AdminIconCatalog.java \
  admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminIconTest.java \
  admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/AdminModuleRegistryTest.java
git commit -m "feat: add profile-neutral Flow icons"
```

### Task 2: Migrate The Reference Workflows To Semantic Icons

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationShellTest.java`

- [ ] **Step 1: Add failing source-contract tests**

Extend `ApplicationShellTest` to require that the host shell no longer imports
`VaadinIcon` and instead imports `AdminIcon`:

```java
var source = Files.readString(Path.of("src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java"));
assertThat(source).contains("import io.github.vaadinadminstarter.flow.navigation.AdminIcon;");
assertThat(source).doesNotContain("import com.vaadin.flow.component.icon.VaadinIcon;");
assertThat(source).contains("AdminIcon.of(AdminIconName.GLOBE)");
assertThat(source).contains("AdminIconCatalog.createAdminIcon(page.iconKey())");
```

Add equivalent narrow assertions for `UsersView`, `CustomersView`, and
`OrdersView` that require `AdminIcon.of(...)` and reject direct
`VaadinIcon` imports. Do not reject Vaadin icon use in the `admin-flow`
fallback implementation.

- [ ] **Step 2: Run the focused tests and verify they fail**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=ApplicationShellTest test
```

Expected: the source-contract assertions fail while direct icon imports remain.

- [ ] **Step 3: Replace direct icon selection without changing behavior**

Replace each direct icon constructor with the facade:

```java
var details = new Button(AdminIcon.of(AdminIconName.EYE), event -> showDetails(row));
var create = new Button(AdminIcon.of(AdminIconName.ADD), event -> create());
```

In `MainLayout`, use `AdminIcon.of` for the product mark, home, globe, and
palette. Use `AdminIconCatalog.createAdminIcon(page.iconKey())` for module
navigation. Preserve every existing tooltip, aria label, text label,
permission check, button theme, and route. The icon wrapper is decorative;
accessible names remain owned by the surrounding button/menu/navigation item.

- [ ] **Step 4: Run focused compilation and host tests**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=ApplicationShellTest,ArchitectureTest test
```

Expected: host and architecture tests pass, and the external Orders module
continues to have no dependency on the reference application.

- [ ] **Step 5: Commit the workflow migration**

```bash
git add admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java \
  admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java \
  admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java \
  admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java \
  admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationShellTest.java
git commit -m "refactor: use semantic administration icons"
```

### Task 3: Add Ant Icon Assets And Shell Rules

**Files:**
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/add.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/attachment.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/briefcase.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/clock.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/cube.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/delete.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/edit.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/eye.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/globe.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/history.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/home.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/key.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/palette.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/pause.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/play.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/shield.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/shopping-cart.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/users.svg`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/icons/LICENSE`
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`

- [ ] **Step 1: Write failing stylesheet and asset contract tests**

Add a test requiring an Ant-only icon rule and a license notice:

```java
assertThat(styles).contains("[data-admin-visual-language=\"ant\"] .admin-icon");
assertThat(styles).contains("mask-image: var(--admin-icon-mask);");
assertThat(styles).contains("[data-admin-icon=\"shopping-cart\"]");
assertThat(styles).contains(".admin-shell-header");
assertThat(styles).contains("vaadin-side-nav-item[current]::part(link)");
assertThat(Files.readString(Path.of("src/main/frontend/themes/admin-theme/icons/LICENSE")))
        .contains("ISC License");
```

For every `AdminIconName.cssValue()`, assert that a matching
`icons/<name>.svg` exists and that the SVG is a monochrome `viewBox` asset
safe for CSS masks. Keep the test in `AdminThemeTokenTest` because the host
owns the asset set.

- [ ] **Step 2: Run the stylesheet test and verify it fails**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=AdminThemeTokenTest test
```

Expected: failure reports the missing mask rule, assets, and license notice.

- [ ] **Step 3: Vendor the neutral mask set and implement scoped CSS**

Vendor the exact small icon subset from Lucide under its ISC license, preserve
the SVG `viewBox`, remove presentation colors, and record source and license
text in `icons/LICENSE`.

Add the Ant-only base rule:

```css
[data-admin-visual-language="ant"] .admin-icon {
  --admin-icon-mask: none;
  align-items: center;
  background: currentcolor;
  display: inline-flex;
  height: 1rem;
  justify-content: center;
  mask-image: var(--admin-icon-mask);
  mask-position: center;
  mask-repeat: no-repeat;
  mask-size: contain;
  width: 1rem;
}

[data-admin-visual-language="ant"] .admin-icon > vaadin-icon {
  display: none;
}

[data-admin-visual-language="ant"] .admin-icon[data-admin-icon="eye"] {
  --admin-icon-mask: url("./icons/eye.svg");
}
```

Add one selector for every semantic icon name. Keep default Vaadin icon nodes
visible outside the Ant profile.

Refine only Ant shell rules: a 48px top bar, 208px desktop drawer, 6px
selection radius, blue-tinted active link, quiet hover surface, non-card page
canvas, icon-only utility targets, and a 390px narrow layout that keeps the
drawer toggle, globe, palette, and account control reachable. Use only
`admin-*` tokens. Do not add gradients, profile-specific Java classes, or
global component resets.

- [ ] **Step 4: Run stylesheet tests and production frontend build**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=AdminThemeTokenTest test
./mvnw -B -ntp -Pproduction -pl :admin-reference-app -am package -DskipTests
```

Expected: asset/CSS contracts pass and Vaadin resolves all theme assets in the
production frontend build.

- [ ] **Step 5: Commit the Ant shell skin**

```bash
git add admin-reference-app/src/main/frontend/themes/admin-theme \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java
git commit -m "feat: add Ant Flow icon and shell skin"
```

### Task 4: Prove The Two Profiles And Update Consumer Guidance

**Files:**
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`
- Modify: `docs/en/appearance-profiles.md`
- Modify: `docs/zh-CN/appearance-profiles.md`
- Modify: `docs/en/theme-tokens.md`
- Modify: `docs/zh-CN/extension-guide.md`

- [ ] **Step 1: Write failing profile-specific browser checks**

Add an Ant test after administrator sign-in:

```java
var navigationIcon = page.locator("[data-admin-visual-language=ant] "
        + ".admin-icon[data-admin-icon=users]");
assertThat(navigationIcon).isVisible();
org.assertj.core.api.Assertions.assertThat((String) navigationIcon.evaluate(
        "element => getComputedStyle(element).maskImage"))
        .contains("users.svg");
org.assertj.core.api.Assertions.assertThat(navigationIcon.locator("vaadin-icon")).isHidden();
```

Add a baseline `BrowserE2EIT` check that the same semantic wrapper contains a
visible Vaadin fallback when `data-admin-visual-language=vaadin`. In the
compact Ant suite, assert utility control bounding boxes still fit the narrow
header after mask icons are active. Retain the existing focus-ring and
permission/navigation assertions.

- [ ] **Step 2: Run browser tests and verify they fail before the skin exists**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dit.test=AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT,BrowserE2EIT verify
```

Expected: assertions fail until semantic wrappers, SVG masks, and shell rules
are active.

- [ ] **Step 3: Document the stable consumer contract**

Update both appearance profile guides to state:

- `ant` is now a Flow-native icon and shell profile, not merely a palette.
- modules declare existing validated navigation icon keys and can use
  `AdminIcon.of(AdminIconName)` for common action icons;
- modules must not import host SVG assets, use CSS mask selectors, or select a
  visual language;
- `vaadin` remains a supported parallel profile with fallback glyphs.

Update `theme-tokens.md` and the Chinese extension guide to repeat the same
boundary, direct module authors to semantic icon names, and prohibit direct
dependencies on `admin-theme/icons`.

- [ ] **Step 4: Run focused browser acceptance**

Run the command from Step 2.

Expected: baseline and both Ant density suites pass, proving profile-specific
rendering without functional drift.

- [ ] **Step 5: Run full normal and production verification**

Run:

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
git diff --check
```

Expected: all module, architecture, browser, OIDC, and production frontend
verification succeeds with a clean diff check.

- [ ] **Step 6: Commit the acceptance and documentation**

```bash
git add admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e \
  docs/en/appearance-profiles.md docs/zh-CN/appearance-profiles.md \
  docs/en/theme-tokens.md docs/zh-CN/extension-guide.md
git commit -m "test: verify Ant Flow icon profile"
```

## Plan Self-Review

- **Spec coverage:** Task 1 establishes a stable Java icon boundary; Task 2
  migrates high-salience shell and repeated actions; Task 3 owns Ant SVG assets
  and shell styling; Task 4 validates both profiles, documents module rules,
  and performs normal/production verification. Phase B and Phase C remain
  separate follow-up plans as required by the approved maturity specification.
- **Scope:** The plan does not introduce a frontend runtime, general component
  wrapper, profile-specific business view, or styling in `admin-spring-flow`.
- **Consistency:** `AdminIconName`, `AdminIcon`, `AdminIconCatalog.iconName`,
  and `AdminIconCatalog.createAdminIcon` are defined in Task 1 before every
  consuming task. All host assets use the enum CSS values.
- **Placeholder scan:** No `TODO`, `TBD`, or deferred implementation wording
  remains in task steps.
