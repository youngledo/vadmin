# Ant Flow Profile Phase C Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the Ant Flow profile's dense data-workspace and operational-state presentation without changing Flow behavior, Java business code, or the parallel Vaadin profile.

**Architecture:** `admin-flow` exposes a profile-neutral current-state hook from `DataWorkspace`, a footer slot, and a small `PaginationBar` composed by the existing `PagedGrid`; the reference host maps the existing page patterns and Vaadin public Grid/paging parts to host-owned workspace tokens. Real pages keep their query, authorization, command, confirmation, and feedback ownership. Focused unit, CSS-contract, browser, and production checks prove the same workflows under both visual languages.

**Tech Stack:** Java 25, Vaadin Flow 25.2, Spring Boot 4.1, CSS custom properties, Vaadin public parts, JUnit 5, AssertJ, Playwright, Testcontainers PostgreSQL, Maven 4.

---

## File Structure

- Create `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PaginationBar.java`: a Spring-free, localizable Flow pager with result summary and previous/next commands.
- Modify `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PagedGrid.java`: make current-page loading, sort/filter refresh, and the pager use the existing `PagedQuery`/`PagedResult` contract.
- Modify `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/PagedGridTest.java`: prove query mapping, page transitions, result totals, disabled boundaries, sort/filter reset, and refresh.
- Modify `admin-flow/src/main/resources/i18n/flow_en_US.properties` and `admin-flow/src/main/resources/i18n/flow_zh_CN.properties`: add localized pager labels and result summaries.
- Modify `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspace.java`: publish `data-admin-workspace-state` for every existing state transition and provide a profile-neutral footer slot.
- Modify `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspaceTest.java`: prove the state hook, status semantics, selection behavior, state transitions, and footer visibility rules.
- Modify `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialog.java`: expose its destructive consequence through a stable semantic class without changing command behavior.
- Modify `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialogTest.java`: prove the stable consequence class and existing explicit-confirmation behavior.
- Modify `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`: define Ant workspace tokens and scoped Grid, state, toolbar, selection, pagination, and confirmation rules using public Vaadin parts.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`: enforce token declaration and Ant selector boundary.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AbstractVisualLanguageE2EIT.java`: add reusable computed-style, viewport, and screenshot helpers for public Grid parts.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`: prove desktop Users, Customers, Orders, Roles, Audit, selection, paging, row action, notification, and confirmation contracts.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`: prove compact dark narrow-screen containment for a dense workspace and its controls.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`: retain a Vaadin-baseline business-flow and pager regression alongside the Ant evidence.
- Modify `docs/en/theme-tokens.md`, `docs/en/appearance-profiles.md`, `docs/zh-CN/appearance-profiles.md`, `docs/en/extension-guide.md`, and `docs/zh-CN/extension-guide.md`: document workspace and pager ownership and module restrictions.

### Task 1: Add a Flow-native server-side pagination pattern

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PaginationBar.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PagedGrid.java`
- Modify: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/PagedGridTest.java`
- Modify: `admin-flow/src/main/resources/i18n/flow_en_US.properties`
- Modify: `admin-flow/src/main/resources/i18n/flow_zh_CN.properties`

- [x] **Step 1: Write failing pagination tests**

Add tests for a `PagedGrid<Row>` with 120 deterministic rows and a page size of 50. Assert that its initial query is page zero, the pager's localized summary exposes page 1 of 3 and total 120, previous is disabled, next is enabled, and activating next requests page one and displays its 50 rows. Add tests that next is disabled on the final page, the bar is hidden for a one-page result, filter/sort changes reset the current page to zero, and `refresh()` reloads the current query after clearing cached data.

- [x] **Step 2: Run the focused unit test and observe the missing pager API**

Run:

```bash
./mvnw -B -ntp -pl :admin-flow -Dtest=PagedGridTest test
```

Expected: the new tests fail because `PagedGrid` has no pagination component or current-page navigation behavior.

- [x] **Step 3: Implement the focused pagination boundary**

Create `PaginationBar` as a `HorizontalLayout` with a localized result summary, accessible previous and next `Button`, and a public `setPage(int pageIndex, int pageCount, long total)` method. It owns only presentation and callback wiring; it accepts no query-service, Spring, or profile type. Add `flow.pagination.previous`, `flow.pagination.next`, and `flow.pagination.summary` message keys to both Flow bundles; use `LocaleChangeObserver` so labels update with the active UI locale.

Refactor `PagedGrid` to own the current page index, latest `PagedResult`, current sort, and refresh. Keep the Grid backed by a callback data provider: its fetch callback loads the selected server page through the existing `PageLoader`, returns only that page's items, and its count callback reports that page's item count so virtual scrolling cannot request a different server page. The pager stores the full result total separately. Previous/next change the page index, refresh the provider, and restore the Grid scroll position to its first row. A Grid sort event resets the page index to zero; the existing `refresh()` likewise resets the page when filters change. Continue to create `PagedQuery(page, pageSize, sortField, ascending, filters.get())`; do not load all rows or introduce a client-side paging collection. Expose `getPaginationBar()`; do not expose mutable cache or profile values.

```java
public PaginationBar getPaginationBar() {
    return paginationBar;
}
```

- [x] **Step 4: Run the focused unit test and the complete Flow module test set**

Run:

```bash
./mvnw -B -ntp -pl :admin-flow -Dtest=PagedGridTest test
./mvnw -B -ntp -pl :admin-flow test
```

Expected: both commands succeed with zero failures.

### Task 2: Publish a profile-neutral workspace state and pager footer

**Files:**
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspace.java`
- Modify: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspaceTest.java`
- Modify: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialog.java`
- Modify: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialogTest.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/AuditView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/PermissionsView.java`
- Modify: `admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java`

- [x] **Step 1: Write failing workspace-state and footer tests**

Extend `DataWorkspaceTest` to assert `data-admin-workspace-state="ready"` at construction, then `busy`, `empty`, `failure`, and `ready` after the existing state API calls. Assert that a supplied footer is visible only in ready state, remains after state restoration, and that its component identity is unchanged. Assert the existing polite status role, Grid visibility, and bulk-action disabling behavior remain correct.

- [x] **Step 2: Run the focused test and observe the missing hook/footer**

Run:

```bash
./mvnw -B -ntp -pl :admin-flow -Dtest=DataWorkspaceTest test
```

Expected: the new assertions fail because no state attribute or footer-slot API exists.

- [x] **Step 3: Implement the semantic workspace ownership boundary**

Add `setFooter(Component footer)` and `getFooter()` to `DataWorkspace`; add the footer after the Grid and toggle it only as a consequence of the existing workspace state. Keep the footer independent of the Grid selection model, page totals, query services, Spring, and profile-specific types. In `ready`, the workspace makes the footer eligible for display; the contained component, not `DataWorkspace`, decides whether a single-page result hides itself.

Add the `data-admin-workspace-state` attribute from one private method using `state.name().toLowerCase(Locale.ROOT)` and call it at construction and every state transition. The `ready` state shows the Grid and footer; `busy`, `empty`, and `failure` hide the footer.

In `ConfirmationDialog`, add `admin-confirmation-consequence` to the existing consequence paragraph. Add a unit assertion for that class while retaining the current tests that prove commands execute only after explicit confirmation and busy mode disables closing/actions.

- [x] **Step 4: Compose the pager in every existing paged workspace**

Immediately after each view creates its `DataWorkspace`, install its existing `PagedGrid` pager:

```java
workspace.setFooter(pages.getPaginationBar());
```

For read-only views that currently create `PagedGrid` without retaining it, retain it in a private field before installing its pager. Do not alter permissions, query-service method signatures, page routes, business command handlers, or external module metadata.

- [x] **Step 5: Run Flow unit tests and the affected application/module unit tests**

Run:

```bash
./mvnw -B -ntp -pl :admin-flow -Dtest=DataWorkspaceTest test
./mvnw -B -ntp -pl :admin-reference-app,:admin-example-orders -am test
```

Expected: both commands succeed with zero failures.

### Task 3: Establish the Ant workspace token and public-selector contract

**Files:**
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`
- Modify: `docs/en/theme-tokens.md`
- Modify: `docs/en/appearance-profiles.md`
- Modify: `docs/zh-CN/appearance-profiles.md`

- [x] **Step 1: Write failing token and selector contract tests**

Extend `AdminThemeTokenTest` to require these token names in the English guide and both Ant light/dark CSS blocks:

```text
workspace-header-fill
workspace-header-text
workspace-row-hover
workspace-row-selected
workspace-divider
workspace-status-fill
workspace-danger-fill
```

Also require Ant-scoped selectors for `vaadin-grid::part(header-cell)`, a public Grid row/cell part, `.admin-page-workspace[data-admin-workspace-state]`, `.admin-pagination-bar`, `.admin-confirmation-consequence`, and `vaadin-dialog-overlay::part(overlay)`. The reference confirmation pattern extends `Dialog`, so destructive confirmation styling is scoped by its semantic consequence class and the public dialog overlay part. Do not assert private shadow-DOM node names or a nonexistent ConfirmDialog component.

- [x] **Step 2: Run the contract test and observe the missing workspace roles**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false -Dtest=AdminThemeTokenTest test
```

Expected: failure reporting missing workspace token names and/or selectors.

- [x] **Step 3: Add only host-owned workspace tokens and scoped rules**

Declare the seven workspace tokens in both `[data-admin-visual-language="ant"]` and its dark counterpart. Add Ant-only rules that:

- render Grid headers with `workspace-header-fill`, `workspace-header-text`, and `workspace-divider`;
- render row hover/selection using `workspace-row-hover` and `workspace-row-selected` without hiding native selection or focus;
- give `.admin-pagination-bar` footer controls a separator and stable surface;
- render selection summaries, busy status, `EmptyState`, and failure content within `workspace-status-fill` while retaining text and status roles;
- distinguish destructive-confirmation consequence content with `workspace-danger-fill` and `admin-danger` without changing confirm/cancel behavior;
- retain compact geometry via existing density tokens rather than adding profile-local pixel values.

Use only documented host properties and exported `::part(...)` selectors. If a public part differs from the preliminary test assumption, update the test to the Vaadin 25.2 documented public part and record that selector in the test contract.

- [x] **Step 4: Document the public boundary in English and Chinese**

Add the token table entries to `docs/en/theme-tokens.md`. State in both appearance guides and both extension guides that host themes own dense workspace, grid, pagination, state, and confirmation presentation; modules compose `DataWorkspace` with `PagedGrid.getPaginationBar()`, use normal Vaadin state APIs and `--admin-*` tokens only. Modules must not target Ant selectors, Grid internals, or profile attributes.

- [x] **Step 5: Re-run the contract and production bundle checks**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  -Dsurefire.failIfNoSpecifiedTests=false -Dtest=AdminThemeTokenTest test
./mvnw -B -ntp -Pproduction -pl :admin-reference-app -am package -DskipTests
```

Expected: both commands succeed.

### Task 4: Prove desktop data-workspace workflows through the Ant profile

**Files:**
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AbstractVisualLanguageE2EIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [x] **Step 1: Add failing browser contracts for representative workspaces and pagers**

In `AntVisualLanguageE2EIT`, add focused tests that sign in and assert:

- Users: toolbar and workspace are visible; selecting a row exposes the existing selection summary and enabled bulk action; Grid header public part uses the workspace header surface; a row action opens the existing disable confirmation. Do not claim a multi-page Users fixture unless the test seeds at least 51 users through a supported test setup.
- Customers: toolbar filter, primary action, Grid header, pager, delete confirmation, and success notification remain visible in one workspace flow.
- Orders: the independently packaged `/orders` page uses the same Grid header/pager treatment and opens its row-detail dialog. Because its deterministic three-row fixture has one page, assert the pager is hidden rather than fabricate a paging interaction.
- Roles: the grant command retains disabled-until-selection behavior inside its dialog and its workspace has the shared Grid treatment.
- Audit: the existing read-only Grid is visible and has the same header/divider treatment. Add a supported test seed with more than 50 audit entries before asserting next-page navigation, or leave paging navigation to `PagedGridTest`.

Add one `DataWorkspace` component-level test in `admin-flow` if browser navigation cannot trigger busy/empty/failure deterministically. That test must call existing state APIs rather than invent a reference-app-only state route.

- [x] **Step 2: Run the targeted visual suite and observe the missing visual contracts**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  '-Dit.test=AntVisualLanguageE2EIT' \
  -Dfailsafe.failIfNoSpecifiedTests=false verify
```

Expected: the new computed-style or state assertions fail before the Task 3 skin is complete.

- [x] **Step 3: Add reusable public-part test helpers only when duplication appears**

In `AbstractVisualLanguageE2EIT`, add helpers that read computed values from an explicitly named exported part and assert a bounding box remains in its parent/viewport. Helpers must fail clearly when the public part is absent; they must never pierce undocumented nested nodes. Use the helpers from the new tests rather than duplicating JavaScript snippets.

- [x] **Step 4: Complete the minimum host CSS needed for the failing workflows**

Adjust only the Task 3 Ant-scoped workspace rules until every targeted flow has the expected header, row, state, pager, confirmation, and feedback presentation. Do not alter page routes, business services, command handlers, permission checks, or external orders-module source.

- [x] **Step 5: Run the focused visual and baseline business-flow regressions**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  '-Dit.test=AntVisualLanguageE2EIT,BrowserE2EIT#customerCrudWithAttachment' \
  -Dfailsafe.failIfNoSpecifiedTests=false verify
```

Expected: all selected integration tests pass.

### Task 5: Prove compact dark containment and complete Phase C verification

**Files:**
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`
- Modify: `docs/superpowers/specs/2026-08-12-ant-flow-profile-phase-c-design.md` only if verification uncovers a material scope correction.

- [x] **Step 1: Add failing compact dark narrow-workspace tests**

At the existing 390px viewport, assert that an Ant compact dark Users or Audit workspace, its toolbar controls, Grid scroll region, selection/row action, and confirmation actions stay within the viewport or their intentionally scrollable Grid region. Assert the Ant dark workspace tokens resolve to dark values and no command overlaps or clips.

- [x] **Step 2: Run the compact visual suite and observe the geometry failure**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  '-Dit.test=AntCompactVisualLanguageE2EIT' \
  -Dfailsafe.failIfNoSpecifiedTests=false verify
```

Expected: the new geometry assertions fail before responsive workspace rules are complete.

- [x] **Step 3: Add the smallest responsive host-theme correction**

Adjust only media-query rules in `styles.css` required to preserve canvas, toolbar, workspace, Grid scroll containment, and action visibility at the supported narrow viewport. Keep horizontal scrolling inside the Grid if its columns cannot fit; do not hide data columns or reimplement pagination in JavaScript.

- [x] **Step 4: Capture desktop and narrow screenshot evidence**

Run the Ant comfortable desktop Users/Customers/Orders workflows and Ant compact dark narrow workspace workflow with the existing browser test harness. Store only intentional, reviewable screenshot artifacts outside source directories or in the established test-artifact location; do not add generated screenshots to Git unless the repository already tracks that exact artifact class.

- [x] **Step 5: Run the full visual subset**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am \
  '-Dit.test=BrowserE2EIT,AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT' \
  -Dfailsafe.failIfNoSpecifiedTests=false verify
```

Expected: all selected browser integration tests pass.

- [x] **Step 6: Run normal, production, and diff verification**

Run serially:

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
git diff --check
```

Expected: both reactor builds succeed with zero test failures and `git diff --check` has no output.

## Plan Self-Review

The plan maps every Phase C specification requirement to a test-first task: the
Flow-native server-side pager is covered by Task 1; the profile-neutral state
hook and footer composition by Task 2; semantic token ownership and public
selector boundary by Task 3; all five representative workspaces and real
confirmation/feedback flows by Task 4; narrow compact geometry, screenshot
review, and normal/production verification by Task 5. It excludes new business
flows, a frontend runtime, and module imports of host styling.
