# Default Spring Admin Starter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide a single Spring Boot starter dependency that supplies a complete Vaadin Flow administration shell and baseline system administration.

**Architecture:** Move the proven shell/theme and generic administration implementation from the reference application into a new Spring starter. `admin-spring-flow` continues to assemble starter and consumer `AdminModule` beans; the reference application becomes a thin consumer and browser acceptance fixture.

**Tech Stack:** Java 25, Maven 4 RC6, Spring Boot 4.1.0, Vaadin Flow 25.2.5, Spring Security, JPA/Flyway, PostgreSQL, Playwright.

**Spec:** `docs/superpowers/specs/2026-08-13-default-spring-admin-starter-design.md`

## Global Constraints

- Keep `admin-contracts`, `admin-platform`, and `admin-flow` free of Spring, JPA, Flyway, and reference-application imports.
- Spring Boot is the only runtime. Do not add Hilla, React, Vue, or a second runtime.
- The starter owns a coherent shell/theme/system-administration baseline; consumers contribute business pages through `AdminModule` without `@Route`.
- Support `zh-CN` and `en-US`; resolve all module metadata through the composite `I18NProvider`.
- Preserve Vaadin and Ant-inspired Flow profiles, light/dark mode, and comfortable/compact density.
- Use test-first changes and commit each independently verifiable task.

---

### Task 1: Introduce The Consumer-Facing Starter Module

**Files:**
- Modify: `pom.xml`, `admin-spring/pom.xml`
- Create: `admin-spring/admin-spring-starter/pom.xml`
- Create: `admin-spring/admin-spring-starter/src/test/java/io/github/vaadinadminstarter/starter/StarterDependencyTest.java`

**Interfaces:** The `admin-spring-starter` POM composes `admin-spring-security`, `admin-spring-jpa`, `admin-spring-boot`, and `admin-spring-flow`; it must not depend on reference app or orders example.

- [ ] **Step 1: Write the failing dependency-contract test**

Use `MavenXpp3Reader` to read the starter POM. Assert the four adapter artifacts and `spring-boot-starter` are present; `vaadin-dev` is optional; PostgreSQL is runtime; no dependency is `admin-reference-app` or `admin-example-orders`.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-spring-starter test`

Expected: Maven cannot select `admin-spring-starter`.

- [ ] **Step 3: Implement the reactor and POM composition**

Add the module to `admin-spring/pom.xml`. Create its POM with the asserted dependencies and `spring-boot-starter-test` for starter tests. Do not remove `admin-examples` yet because the reference app still depends on it.

- [ ] **Step 4: Verify green**

Run: `./mvnw -B -ntp -pl :admin-spring-starter test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run: `git add pom.xml admin-spring/pom.xml admin-spring/admin-spring-starter && git commit -m "feat: add Spring admin starter module"`

### Task 2: Move System Administration Into The Starter

**Files:**
- Create under `admin-spring-starter`: `administration/AdministrationQueryService.java`, `administration/UserAdministrationService.java`, `views/UsersView.java`, `views/RolesView.java`, `views/PermissionsView.java`, `views/AuditView.java`, `modules/SystemAdministrationModuleConfiguration.java`, and `resources/i18n/system_{zh_CN,en_US}.properties`
- Create starter equivalents of `UserAdministrationServiceTest` and `ReferenceAdminModulesTest`
- Delete after relocation: corresponding `admin-reference-app/app/administration`, four system views, module descriptor, and system bundles

**Interfaces:** `SystemAdministrationModuleConfiguration.systemAdministration()` returns `AdminModule` ID `system`, routes `users`, `roles`, `permissions`, `audit`, all system permissions, and bundle `i18n.system`.

- [ ] **Step 1: Write failing service/module tests**

Port existing service tests to starter packages. Add a module test asserting routes, required permissions, group `system`, and `i18n.system`. Keep all existing authorization/audit expectations.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-spring-starter -am test -Dtest=UserAdministrationServiceTest,SystemAdministrationModuleConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: compile failure because starter administration types are absent.

- [ ] **Step 3: Move production code and register auto-configuration**

Relocate the services, views, module descriptor, and system messages without changing use-case calls, permission constants, page-pattern composition, or view failure behavior. Make the descriptor `@AutoConfiguration`, place it in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, and expose one module bean.

- [ ] **Step 4: Verify green**

Run: `./mvnw -B -ntp -pl :admin-spring-starter,:admin-spring-flow -am test`

Expected: PASS; assembled permissions derive from starter metadata.

- [ ] **Step 5: Commit**

Run: `git add admin-spring/admin-spring-starter admin-reference-app && git commit -m "feat: move system administration into starter"`

### Task 3: Move Default Shell, Theme, And Home Page Into The Starter

**Files:**
- Create under `admin-spring-starter`: `DefaultApplicationShell.java`, `DefaultAdminHostLayoutConfiguration.java`, `theme/AdminAppearanceProperties.java`, `theme/AdminVisualLanguage.java`, `theme/AdminDensity.java`, `views/DefaultMainLayout.java`, `views/DefaultHomeView.java`, and `frontend/themes/admin-theme/` assets/icons/license
- Create: tests for host backoff, appearance properties, and translated external module metadata
- Delete after relocation: `admin-reference-app` application shell, theme package, `MainLayout`, `HomeView`, theme assets

**Interfaces:** `DefaultAdminHostLayoutConfiguration.adminHostLayout()` returns `new AdminHostLayout(DefaultMainLayout.class)` only when `AdminHostLayout` is absent. `DefaultMainLayout` uses `getTranslation(page.titleKey())`; `DefaultHomeView` uses `getTranslation(page.titleKey())` and `getTranslation(page.intentKey())`.

- [ ] **Step 1: Write failing host and i18n tests**

Create a context test that asserts the default host exists when no host bean exists and backs off for a test host. Create a shell/home test that contributes an external module message bundle and asserts its title/intent render translated values rather than literal keys.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-spring-starter -am test -Dtest=DefaultAdminHostLayoutConfigurationTest,DefaultHomeViewTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: failure because default shell types do not exist.

- [ ] **Step 3: Relocate shell/theme and implement override semantics**

Move existing responsive shell behavior: permission-filtered navigation, location, logout, language and appearance icon menus, session colour mode, visual profile, density, and production `@Uses` anchors for the four system views. Copy CSS/icons/license unchanged. Register appearance configuration and default host with `@ConditionalOnMissingBean(AdminHostLayout.class)`. A consumer supplying host layout owns a custom app shell/theme completely; do not implement partial replacement.

- [ ] **Step 4: Verify green and production compilation**

Run: `./mvnw -B -ntp -pl :admin-spring-starter -am test`

Run: `./mvnw -B -ntp -Pproduction -pl :admin-spring-starter -am package -DskipTests`

Expected: both PASS.

- [ ] **Step 5: Commit**

Run: `git add admin-spring/admin-spring-starter admin-reference-app && git commit -m "feat: provide default Flow administration shell"`

### Task 4: Convert The Reference Application To A Starter Consumer

**Files:**
- Modify: `admin-reference-app/pom.xml`, `ApplicationConfiguration.java`, launcher/properties/context/architecture tests
- Delete: all relocated system source and assets from Task 2/3

**Interfaces:** Reference app depends on `admin-spring-starter`, receives `AdminHostLayout`, `AdminModuleRegistry`, system module, and `I18NProvider` through auto-configuration, and only keeps launch/seed/acceptance-specific code.

- [ ] **Step 1: Write failing consumer-boundary tests**

Update `ApplicationContextIT` to assert the starter supplies host, registry, system module, and i18n. Update `ArchitectureTest` to reject reference application source ownership of shell, theme, system views/services, or system module configuration.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test -Dtest=ApplicationContextIT,ArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: failure against old ownership.

- [ ] **Step 3: Implement thin consumer composition**

Replace direct baseline adapter dependencies with `admin-spring-starter`; retain only test dependencies needed for tests. Remove host layout, appearance/theme, and system module registration from `ApplicationConfiguration`. Delete moved sources/assets and update test imports to starter behavior.

- [ ] **Step 4: Verify green**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run: `git add admin-reference-app && git commit -m "refactor: make reference app consume starter"`

### Task 5: Remove The Customer Sample

**Files:**
- Delete: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/customer/`, `app/file/`, `views/CustomersView.java`, `i18n/customers_*`, customer migrations, customer/file tests
- Modify: application properties, Compose/.env configuration, E2E tests, `ApplicationConfiguration.java`

**Interfaces:** Framework-neutral `FileStorage` remains a core contract. The reference application contains no customer route/module/migration/file-storage setting.

- [ ] **Step 1: Update E2E coverage first**

Replace customer assertions with Users, Roles, Permissions, and Audit coverage while retaining navigation filtering, locale, visual profile, density, light/dark, dialog/feedback, and narrow-viewport coverage.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test -Dtest=BrowserE2EIT -Dsurefire.failIfNoSpecifiedTests=false`

Expected: stale customer production/test references fail until removal is complete.

- [ ] **Step 3: Delete customer production/configuration surface**

Delete customer entities/services/attachments/downloads, local storage, customer view/module, migrations, bundles, and settings. Do not delete contracts or reusable Flow upload components.

- [ ] **Step 4: Verify green**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run: `git add -A admin-reference-app && git commit -m "refactor: remove customer sample from reference app"`

### Task 6: Retire The Orders Example And Aggregate

**Files:**
- Delete: `admin-examples/admin-example-orders/`, `admin-examples/pom.xml`
- Modify: root `pom.xml`, reference-app POM/source/tests, and Flow assembly fixtures/resources

**Interfaces:** Retain contract tests with a synthetic `sample` module but do not ship a business-domain artifact.

- [ ] **Step 1: Generalize Flow assembly fixtures before deletion**

Rename orders-only test view/resource/module identifiers in `admin-spring-flow` tests to `sample`. Assert synthetic metadata yields route registration, translated navigation/home labels, declared icon, and permission guard.

- [ ] **Step 2: Verify generic fixtures**

Run: `./mvnw -B -ntp -pl :admin-spring-flow -am test`

Expected: PASS without `admin-example-orders` dependency.

- [ ] **Step 3: Delete the artifact and all composition references**

Remove the aggregate from root reactor, reference app POM/imports/`@Uses`/routes/i18n/E2E, then delete the module tree.

- [ ] **Step 4: Verify reactor package**

Run: `./mvnw -B -ntp package -DskipTests`

Expected: PASS and no production coordinate named `admin-example-orders`.

- [ ] **Step 5: Commit**

Run: `git add -A && git commit -m "refactor: retire orders example module"`

### Task 7: Rewrite Current Bilingual Adoption Documentation

**Files:**
- Modify: `README.md`, `docs/{en,zh-CN}/{architecture,quick-start,extension-guide,release-guide}.md`
- Create: a focused current-documentation guard test under starter or reference-app test source

**Interfaces:** Guides use `admin-spring-starter`; the generic `inventory` example uses `AdminModule`, `AdminPage`, a permission, a prototype view bean without `@Route`, two message bundles, and host `@Uses(InventoryView.class)` only for a consumer dynamic view.

- [ ] **Step 1: Write the failing current-documentation guard**

Scan current guides only, excluding `docs/superpowers/**`. Fail on `admin-example-orders`, `CustomersView`, `/orders`, `/customers`, or claims that normal consumers must create `MainLayout`/a default `AdminHostLayout`.

- [ ] **Step 2: Verify red**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test -Dtest=CurrentDocumentationTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL until the guides are migrated.

- [ ] **Step 3: Rewrite adoption documentation**

Update ownership tables, quick-start commands, extension examples, and release checklists. State that starter supplies shell/theme/system administration; only a deliberate full shell replacement needs a consumer `AdminHostLayout` and `AppShellConfigurator`. Preserve historical plans/specifications unchanged.

- [ ] **Step 4: Verify green**

Run: `./mvnw -B -ntp -pl :admin-reference-app,:admin-spring-starter -am test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run: `git add README.md docs admin-reference-app/src/test && git commit -m "docs: describe starter-first adoption"`

### Task 8: Complete Consumer Acceptance And Release Verification

**Files:**
- Modify: reference-app browser/i18n/architecture tests and both release guides

**Interfaces:** The browser proves that a starter consumer reaches complete system administration, retains visual settings/i18n/direct-route authorization, and renders an external synthetic `AdminModule` correctly.

- [ ] **Step 1: Add acceptance assertions**

Assert local login reaches starter shell; system pages appear only with permission; locale translates starter and synthetic external metadata; Vaadin/Ant visual profile, density, light/dark, dialogs, notification, and narrow layout work; direct-route denial still holds.

- [ ] **Step 2: Run focused browser verification**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify -DskipTests=false -Dit.test=BrowserE2EIT,AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT`

Expected: PASS with PostgreSQL/Testcontainers and Chromium available.

- [ ] **Step 3: Run full normal verification**

Run: `./mvnw -B -ntp verify`

Expected: PASS.

- [ ] **Step 4: Run full production verification**

Run: `./mvnw -B -ntp -Pproduction verify`

Expected: PASS; frontend generation includes starter shell and dynamic system views.

- [ ] **Step 5: Inspect scope and commit**

Run: `git status --short && rg -n "admin-example-orders|CustomersView|/orders|/customers" --glob '!docs/superpowers/**' .`

Expected: no production/current-guide references; historical records may remain under `docs/superpowers`.

Run: `git add admin-reference-app docs && git commit -m "test: verify default starter consumer experience"`
