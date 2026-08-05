# Flow Admin Design System Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a Java-first Flow admin application shell, reusable page patterns, and modernized reference views without changing authorization, audit, or persistence behavior.

**Architecture:** Theme assets live in the reference application's Flow theme for Phase 1, while Java page-pattern components live in `admin-flow` and stay free of Spring. `MainLayout` composes the generic Flow patterns with the existing permission-filtered `PageRegistry`; business views retain their application-service dependencies and use the common presentation patterns.

**Tech Stack:** Java 25, Vaadin Flow 25.2.5, Spring Boot 4.1.0, Maven 4 RC6, Playwright E2E, Testcontainers PostgreSQL.

---

### Task 1: Establish Theme And Shell

**Files:**
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/theme.json`
- Create: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/Application.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] Add a named Flow theme with semantic light/dark token groups for surfaces, text, actions, status, focus, spacing, and density; register it through Flow's `@Theme` application annotation.
- [ ] Add an E2E assertion that an authenticated user sees the product shell, current-user control, and active navigation item; run it before the shell refactor to establish the missing behavior.
- [ ] Rebuild `MainLayout` as a responsive AppLayout: product mark, capability-grouped permission-filtered navigation, current location, and accessible user menu with session-scoped theme mode control.
- [ ] Add CSS rules for wide and narrow navigation, content canvas widths, keyboard focus, and both theme schemes. Do not add decorative dashboard cards or backend-free controls.
- [ ] Run `./mvnw -B -ntp -pl :admin-reference-app -am verify` and commit `feat: add Flow admin shell`.

### Task 2: Add Reusable Administration Page Patterns

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageHeader.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PageToolbar.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspace.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/EditorDialog.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/EmptyState.java`
- Test: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/PageHeaderTest.java`
- Test: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/PageToolbarTest.java`
- Test: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/DataWorkspaceTest.java`
- Test: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/EditorDialogTest.java`

- [ ] Write focused tests for page heading/action composition, toolbar action placement, workspace selection state, and editor primary/cancel behavior.
- [ ] Implement the smallest composable Flow components that satisfy the tests. They may compose native `Grid`, `FormLayout`, `Dialog`, and buttons but must not introduce Spring or reference-domain dependencies.
- [ ] Provide standard busy, empty, and failure presentation hooks rather than assuming every page always has data.
- [ ] Run `./mvnw -B -ntp -pl :admin-flow test`, then the reference-app test slice, and commit `feat: add Flow admin page patterns`.

### Task 3: Modernize Workplace And Read-Only Workspaces

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/HomeView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/PermissionsView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/AuditView.java`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] Extend browser tests to assert the workplace renders permitted quick links and the permissions/audit pages render the shared header/workspace structure.
- [ ] Replace the title-only home page with an operational workplace that contains only permitted navigation shortcuts and existing system context; do not invent KPIs or chart data.
- [ ] Apply the page header and dense read-only data workspace to permissions and audit while retaining the existing paged query services and route permissions.
- [ ] Run the affected E2E scenarios and commit `feat: modernize Flow admin workspaces`.

### Task 4: Modernize Mutating Administration Pages

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] Add E2E coverage for the common toolbar, selected-row bulk actions, form validation feedback, destructive confirmation, and customer attachment workflow.
- [ ] Refactor users and customers to use the header, toolbar, workspace, and editor patterns; retain all existing permission checks, command calls, grid queries, and audit-producing paths.
- [ ] Refactor roles to use the shared header and editor/action surface while preserving the existing grant command and audit behavior.
- [ ] Run `./mvnw -B -ntp -pl :admin-reference-app -am verify` and commit `feat: modernize administration views`.

### Task 5: Document And Verify The Design System

**Files:**
- Modify: `docs/architecture.md`
- Modify: `docs/extension-guide.md`
- Modify: `docs/quick-start.md`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] Document the Flow theme customization path, shell composition, Java-only page-pattern APIs, and the rule that `admin-flow` remains Spring-free.
- [ ] Add desktop and narrow-browser E2E checks for shell navigation, light/dark mode, a list/edit workflow, and empty/denied/failure presentation.
- [ ] Run `./mvnw -B -ntp verify`, `./mvnw -B -ntp -Pproduction verify`, and `docker compose --env-file .env.example config`.
- [ ] Inspect `git diff --check` and the architecture test, then commit `docs: document Flow admin design system`.
