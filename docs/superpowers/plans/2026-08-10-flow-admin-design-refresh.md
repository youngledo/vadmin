# Flow Admin Design Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the Flow reference application into a coherent, compact, professional administration workbench while preserving native Vaadin components and all existing module/security contracts.

**Architecture:** The reference application owns shell composition and theme CSS; `admin-flow` owns narrowly reusable page-pattern composition. The work proceeds from shell and semantic tokens to shared patterns, then applies those patterns to built-in and external reference pages. Browser E2E verifies the visual contract at the application boundary.

**Tech Stack:** Java 25, Vaadin Flow 25.2.5, Spring Boot 4.1, CSS custom properties, Playwright Java, JUnit 5, Maven 4.

---

### Task 1: Establish Shell Utility Controls And Visual Tokens

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/main/resources/i18n/system/messages_zh_CN.properties`
- Modify: `admin-reference-app/src/main/resources/i18n/system/messages_en_US.properties`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationShellTest.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] **Step 1: Write shell contract tests before changing the shell**

Add tests proving the authenticated shell exposes dedicated, accessible language
and appearance utility controls and that the account menu does not own those
preferences. Add a browser test that opens the language icon control, switches
locale, and confirms the compact shell remains usable on a narrow viewport.

```java
assertThat(MainLayout.class.getDeclaredFields())
        .extracting(Field::getName)
        .contains("languageMenu", "appearanceMenu");
```

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test -Dtest=ApplicationShellTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because the shell still owns a `Select<Locale>` control.

- [ ] **Step 2: Implement icon-triggered preference controls**

Replace the language `Select` with a `MenuBar`/icon trigger using
`VaadinIcon.GLOBE` and an accessible label. Present locale choices in its
submenu with a checked current selection. Move theme selection to its own
appearance icon trigger using the existing session-backed theme code. Retain
the avatar menu for account context only. Preserve `LocaleChangeObserver` and
all existing `AdminLocalePreference` calls.

```java
var languageButton = new Button(VaadinIcon.GLOBE.create());
languageButton.setAriaLabel(text("system.shell.language"));
languageButton.setTooltipText(text("system.shell.language"));
```

- [ ] **Step 3: Refine shell CSS only through semantic tokens**

Add compact utility-control, active-navigation, drawer-group, and content
canvas rules to `styles.css`. Keep `--admin-*` semantic tokens as the source of
all new colors, spacing, radius, and elevation. Mirror any new token required
by both themes in the dark selector and extend `AdminThemeTokenTest` if the
public token contract changes.

- [ ] **Step 4: Verify shell behavior**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify`

Expected: PASS, including language/theme/narrow-shell browser coverage.

- [ ] **Step 5: Commit the shell refresh**

```bash
git add admin-reference-app/src/main admin-reference-app/src/test
git commit -m "feat: refine Flow administration shell"
```

### Task 2: Add Shared Workbench Composition Classes

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/AdminPageFrame.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageHeader.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageToolbar.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspace.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/AdminPageFrameTest.java`
- Modify: existing pattern tests under `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/`

- [ ] **Step 1: Write failing composition tests**

Define `AdminPageFrame` as a Spring-free Flow component accepting a
`PageHeader`, optional `PageToolbar`, and one main work component. Test that it
has stable semantic class names, keeps the header/controls/workspace order,
and does not introduce a card around the full page.

```java
var frame = new AdminPageFrame(header, toolbar, workspace);
assertThat(frame.getElement().getClassList()).contains("admin-page-frame");
assertThat(frame.getComponentAt(0)).isSameAs(header);
```

Run: `./mvnw -B -ntp -pl :admin-flow test -Dtest=AdminPageFrameTest`

Expected: FAIL because `AdminPageFrame` does not exist.

- [ ] **Step 2: Implement the minimal page frame**

Create a `VerticalLayout` with `setPadding(false)`, `setSpacing(false)`, and
stable `admin-page-frame`, `admin-page-header`, `admin-page-controls`, and
`admin-page-workspace` classes. Do not add Spring, module, authorization, or
reference-application types.

- [ ] **Step 3: Refine existing patterns without API expansion**

Give `PageHeader`, `PageToolbar`, and `DataWorkspace` stable component classes
for the theme. Preserve their public behavior and locale contracts. Add only
layout classes needed by the page frame; do not add universal component
wrappers or new business actions.

- [ ] **Step 4: Verify reusable patterns**

Run: `./mvnw -B -ntp -pl :admin-flow test`

Expected: PASS with all existing pattern tests and the new frame test.

- [ ] **Step 5: Commit the shared workbench API**

```bash
git add admin-flow
git commit -m "feat: add Flow administration page frame"
```

### Task 3: Adopt The Shared Frame In Reference And External Pages

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/AuditView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/PermissionsView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/HomeView.java`
- Modify: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] **Step 1: Add failing representative page assertions**

Extend browser tests for Users, Customers, Audit, and Orders to require one
`admin-page-frame` with header, controls where applicable, and workspace. Add
an assertion that the external Orders page receives the same frame classes.

```java
assertThat(page.locator(".admin-page-frame")).hasCount(1);
assertThat(page.locator(".admin-page-frame .admin-page-workspace")).isVisible();
```

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify -DskipTests=false`

Expected: FAIL because pages still add header, toolbar, and workspace as loose
siblings.

- [ ] **Step 2: Migrate built-in pages**

Replace each page's loose `add(header, toolbar, workspace)` composition with
`AdminPageFrame`. Keep the current service calls, permissions, data test IDs,
dialogs, and locale observers unchanged. Use `AdminPageFrame(header, null,
workspace)` for read-only pages.

- [ ] **Step 3: Migrate the external Orders page**

Use the same Spring-free frame in `OrdersView`. Do not add a dependency on
`admin-reference-app`, `MainLayout`, global CSS, or `@Route`.

- [ ] **Step 4: Verify adoption and independence**

Run:
`./mvnw -B -ntp -pl :admin-example-orders -am test`

Run:
`./mvnw -B -ntp -pl :admin-reference-app -am verify`

Expected: PASS; Orders still appears through module metadata and all existing
browser workflows remain functional.

- [ ] **Step 5: Commit reference-page adoption**

```bash
git add admin-reference-app admin-examples/admin-example-orders
git commit -m "feat: adopt Flow workbench page composition"
```

### Task 4: Apply The Professional Workbench Theme And Verify Visual States

**Files:**
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`
- Modify: `docs/en/theme-tokens.md` only if the public token table changes

- [ ] **Step 1: Write failing geometry and state tests**

Add browser assertions for the refreshed Users and Orders pages: header,
toolbar, and workspace have non-overlapping vertical geometry; a dark workspace
uses distinct computed foreground/background values; loading/empty/failure
surfaces retain the frame width. Extend source-token tests for any newly public
semantic variable.

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify -DskipTests=false`

Expected: FAIL until the page-frame classes receive layout rules.

- [ ] **Step 2: Implement scoped page-pattern CSS**

Style only shell and `.admin-page-*` pattern classes. Use a compact vertical
rhythm, surface separation for data work, controlled borders/elevation, and
responsive control wrapping. Do not add gradients, oversized headings,
decorative cards, or literal colors already represented by tokens.

- [ ] **Step 3: Validate themes and responsiveness**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify`

Expected: PASS, including dark, locale, narrow-shell, geometry, and Orders
browser cases.

- [ ] **Step 4: Run production verification**

Run: `./mvnw -B -ntp -Pproduction verify`

Expected: PASS. Dynamic view `@Uses` anchors remain intact so every refreshed
component is available in the optimized frontend bundle.

- [ ] **Step 5: Commit visual-system refinement**

```bash
git add admin-reference-app/src/main admin-reference-app/src/test docs/en/theme-tokens.md
git commit -m "feat: refine Flow administration workbench visual system"
```

### Task 5: Record The Design Refresh Outcome

**Files:**
- Modify: `docs/en/architecture.md`
- Modify: `docs/superpowers/specs/2026-08-10-flow-admin-design-refresh-design.md`

- [ ] **Step 1: Record implemented boundaries and evidence**

Document the shell utility model, page-frame ownership, external-module
adoption, and the exact normal/production verification evidence. Change the
design status from proposed to completed only after Task 4 passes.

- [ ] **Step 2: Check documentation consistency**

Run: `git diff --check`

Expected: PASS with no whitespace errors.

- [ ] **Step 3: Commit the outcome**

```bash
git add docs/en/architecture.md docs/superpowers/specs/2026-08-10-flow-admin-design-refresh-design.md
git commit -m "docs: record Flow admin design refresh outcome"
```
