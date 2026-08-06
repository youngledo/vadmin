# Flow Admin Design System Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add reusable Flow interaction patterns and use them to complete the reference application's administration workflows.

**Architecture:** `admin-flow` owns small Java-only detail, confirmation, and feedback patterns. `admin-reference-app` composes them around unchanged secured views and application services. Browser tests drive visible user actions through the existing Spring Boot runtime.

**Tech Stack:** Java 25, Vaadin Flow 25.2.5, Spring Boot 4.1, JUnit, Testcontainers, Playwright Browser E2E.

---

### Task 1: Add Reusable Detail, Confirmation, And Feedback Patterns

**Files:**
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/DetailDialog.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialog.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/OperationFeedback.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/DetailDialogTest.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/ConfirmationDialogTest.java`
- Create: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/patterns/OperationFeedbackTest.java`

- [ ] Write focused failing tests for detail field composition, explicit confirmation/cancellation, busy close protection, and success/failure feedback.
- [ ] Run `./mvnw -B -ntp -pl :admin-flow -Dtest=DetailDialogTest,ConfirmationDialogTest,OperationFeedbackTest test`; expect failures because the pattern classes do not exist.
- [ ] Implement the smallest Java-only Flow components. `ConfirmationDialog` must place cancel then confirm actions in a real wrapping footer, preserve close policies around busy state, and call its confirm callback only on explicit confirmation. `OperationFeedback` must expose success presentation without swallowing non-validation exceptions.
- [ ] Run `./mvnw -B -ntp -pl :admin-flow clean test` and `git diff --check`; commit `feat: add Flow interaction patterns`.

### Task 2: Apply Interaction Patterns To Administration Views

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/HomeView.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] Add E2E assertions for readable user/role/customer details, cancellation before mutation, confirmed user status change or customer deletion, post-command success feedback, and permission-filtered workplace operation entries.
- [ ] Run the new scenarios before view changes; expect the missing detail/confirmation/feedback controls to fail.
- [ ] Compose `DetailDialog`, `ConfirmationDialog`, and `OperationFeedback` into the four views. Preserve every current service call, `PagedGrid` provider, attachment flow, validation branch, authorization failure rethrow, audit-producing command, and route guard.
- [ ] Replace workplace text-only links with compact permission-filtered entry components that contain target title and intent, without counts, charts, alerts, or global controls.
- [ ] Run `./mvnw -B -ntp -pl :admin-reference-app -am -DskipTests package`, then `./mvnw -B -ntp -pl :admin-reference-app -Dit.test=BrowserE2EIT verify`; commit `feat: complete Flow administration workflows`.

### Task 3: Verify Phase 2 Boundaries And Delivery

**Files:**
- Modify if needed: `docs/en/architecture.md`, `docs/zh-CN/architecture.md`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java`

- [ ] Document only new reusable interaction-pattern responsibilities and explicitly retain the Spring-free `admin-flow` boundary; update both language variants together if public architecture text changes.
- [ ] Run `./mvnw -B -ntp verify`, `./mvnw -B -ntp -Pproduction verify`, and `docker compose --env-file .env.example config --quiet`.
- [ ] Run `./mvnw -B -ntp -pl :admin-reference-app test -Dtest=ArchitectureTest`, `git diff --check`, and inspect `git status --short` for generated artifacts.
- [ ] Request fresh specification and quality review; commit final corrections as `docs: document Flow admin design system phase 2`.
