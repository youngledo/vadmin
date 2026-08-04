# Vaadin Admin Starter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Apache-2.0, production-oriented Vaadin Flow business-workbench starter with framework-neutral reusable core modules and a Spring Boot 4.1.0 reference application.

**Architecture:** A Maven modular monolith separates Java-only contracts, framework-neutral platform use cases, Vaadin Flow UI patterns, and Spring Security/JPA/Boot adapters. The reference application is the only composition root. Permissions are code-first, RBAC is persisted in PostgreSQL, and every administrative mutation is authorized and audited.

**Tech Stack:** Java 25, Maven 4.0.0-rc-6, Vaadin Flow 25.2.5, Spring Boot 4.1.0, Spring Security, Spring Data JPA, PostgreSQL, Flyway 13.1.0, JUnit 5, ArchUnit, Testcontainers 2.0.5, Playwright, Docker, GitHub Actions.

---

## File Structure

```text
vaadin-admin-starter/
  pom.xml
  .mvn/wrapper/maven-wrapper.properties
  admin-contracts/
  admin-platform/
  admin-flow/
  admin-spring-security/
  admin-spring-jpa/
  admin-spring-boot/
  admin-reference-app/
  docs/
  docker-compose.yml
  Dockerfile
  .github/workflows/verify.yml
```

The project is organized by runtime boundary first. Within `admin-platform`, organize user, role, permission, and audit code by capability; do not create a cross-module `common` or `utils` package.

## Shared Implementation Contract

Use `UUID` for persistent identity and `Instant` for timestamps. The canonical
authorization types are `PermissionCode`, `CurrentUser`,
`AuthorizationService`, and `PermissionCatalog`; neither a `String` permission
nor a Spring Security `Authentication` may cross into the contracts or platform
modules. `PermissionCatalog` contains the release-1 system and customer
permissions, validates their `domain:resource:action` format, and is
synchronized into the `permissions` table as system-managed data at startup.
`CurrentUser` is `CurrentUser(UUID userId, String username, Set<PermissionCode>
permissions, long authVersion)` and `GrantPermissionCommand` is
`GrantPermissionCommand(String roleCode, PermissionCode permissionCode)`.

Use `AuditEvent(UUID actorUserId, String actionCode, String targetType, String
targetId, AuditOutcome outcome, Instant occurredAt, String correlationId,
Map<String, String> metadata)`, where `AuditOutcome` is `SUCCESS`, `DENIED`, or
`FAILURE`. A central redaction policy discards metadata keys matching `password`,
`secret`, `token`, `authorization`, `cookie`, `sql`, or `stack` before an event
reaches `AuditSink`.

The standard Flow list pattern is `PagedQuery`/`PagedResult<T>` with page
number, page size, sort field, ascending flag, and filter map. The Flow module
supplies a `PagedGrid<T>` helper and an `ActionDefinition` that evaluates an
associated `PermissionCode` independently for every bulk or row action. The
file boundary is `FileStorage` with `store`, `open`, and `delete` operations
and a `StoredFile` value; release 1 provides only a local filesystem development
adapter and no object-storage implementation.

### Task 1: Establish Repository Governance and Maven 4 Build

**Files:**
- Create: `README.md`, `LICENSE`, `.gitignore`, `.editorconfig`, `.gitattributes`
- Create: `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd`, `pom.xml`
- Create: `admin-contracts/pom.xml`, `admin-platform/pom.xml`, `admin-flow/pom.xml`, `admin-spring-security/pom.xml`, `admin-spring-jpa/pom.xml`, `admin-spring-boot/pom.xml`, `admin-reference-app/pom.xml`
- Create: `docs/requirements.md`, `docs/architecture.md`

- [ ] **Step 1: Initialize the repository and retain approved documentation**

```bash
cd /Users/huangxiao/Workspace/mine/vaadin-admin-starter
git init -b main
find docs -maxdepth 2 -type f | sort
```

Expected: approved requirements, architecture, diagrams, and implementation
plan are already present under `docs/`; `git status --short` lists only new
project files after initialization.

- [ ] **Step 2: Add governance files and Apache-2.0 license**

Create `.gitignore`:

```gitignore
target/
.idea/
.vscode/
*.iml
.DS_Store
.env
playwright-report/
test-results/
```

Create `.editorconfig` with UTF-8, LF, four-space indentation for Java/XML, and two-space indentation for YAML. Add the complete Apache-2.0 text to `LICENSE`. In `README.md`, state that Maven 4.0.0-rc-6 is intentionally required and may change before Maven 4 GA.

- [ ] **Step 3: Write the root Maven reactor POM**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"
         root="true">
  <modelVersion>4.1.0</modelVersion>
  <groupId>io.github.vaadinadminstarter</groupId>
  <artifactId>vaadin-admin-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <properties>
    <java.version>25</java.version>
    <spring-boot.version>4.1.0</spring-boot.version>
    <vaadin.version>25.2.5</vaadin.version>
    <flyway.version>13.1.0</flyway.version>
    <testcontainers.version>2.0.5</testcontainers.version>
  </properties>
  <subprojects>
    <subproject>admin-contracts</subproject>
    <subproject>admin-platform</subproject>
    <subproject>admin-flow</subproject>
    <subproject>admin-spring-security</subproject>
    <subproject>admin-spring-jpa</subproject>
    <subproject>admin-spring-boot</subproject>
    <subproject>admin-reference-app</subproject>
  </subprojects>
</project>
```

Use `spring-boot-starter-parent` for Spring Boot dependency/plugin management, import the Testcontainers BOM in `dependencyManagement`, and declare the Vaadin starter version in the Flow module. Configure compiler release 25, Surefire/Failsafe, and a `production` profile in plugin management. Use Maven 4's `subprojects` element, rather than its deprecated `modules` alias. Create each child POM with the root as its parent and no dependencies yet, so the reactor validates before module implementation begins.

- [ ] **Step 4: Pin Maven Wrapper to Maven 4 RC6**

```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/4.0.0-rc-6/apache-maven-4.0.0-rc-6-bin.zip
distributionSha256Sum=e7a17cac56ac3693ca5c8a67b4101b74fcb52df6ccd93319d5acb27bf3c27cb6
```

Generate wrapper scripts with Wrapper 3.3.4, mark `mvnw` executable, then run:

```bash
./mvnw -v
./mvnw -B -ntp validate
```

Expected: Maven reports `4.0.0-rc-6`, Java reports 25, and `validate` succeeds.

- [ ] **Step 5: Commit the build baseline**

```bash
git add README.md LICENSE .gitignore .editorconfig .gitattributes .mvn mvnw mvnw.cmd pom.xml docs
git commit -m "build: establish Maven 4 modular reactor"
```

### Task 2: Create Contract Types and Error Semantics

**Files:**
- Create: `admin-contracts/pom.xml`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/auth/PermissionCode.java`, `CurrentUser.java`, `AuthorizationService.java`, `PermissionCatalog.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/audit/AuditEvent.java`, `AuditOutcome.java`, `AuditSink.java`, `AuditMetadataRedactor.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/error/ErrorCode.java`, `BusinessFailure.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/navigation/NavigationEntry.java`, `PagedQuery.java`, `PagedResult.java`, `ActionDefinition.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/file/FileStorage.java`, `StoredFile.java`
- Test: `admin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/auth/PermissionCodeTest.java`

- [ ] **Step 1: Write failing permission-code tests**

```java
@Test
void accepts_three_segment_permission_code() {
  assertThat(PermissionCode.of("system:user:read").value()).isEqualTo("system:user:read");
}

@Test
void rejects_missing_action_segment() {
  assertThatThrownBy(() -> PermissionCode.of("system:user"))
      .isInstanceOf(IllegalArgumentException.class);
}
```

Run: `./mvnw -pl admin-contracts test`  
Expected: FAIL because `PermissionCode` does not exist.

- [ ] **Step 2: Implement minimal framework-neutral contracts**

Create the immutable Java records/interfaces listed above. `PermissionCode.of`
accepts exactly three lowercase ASCII segments separated by `:`; each segment
matches `[a-z][a-z0-9-]*`. `PermissionCatalog.requireKnown` rejects an
unregistered value with `validation.failed`.

```java
public interface AuthorizationService {
  boolean hasPermission(CurrentUser user, PermissionCode permission);
  void requirePermission(CurrentUser user, PermissionCode permission);
}
```

`BusinessFailure` contains only an error code, safe detail key, and field-error map; it imports no HTTP or Spring type.

- [ ] **Step 3: Verify contract tests and dependency purity**

```bash
./mvnw -pl admin-contracts test
jdeps --multi-release 25 admin-contracts/target/classes | grep -E 'org.springframework|jakarta.persistence|org.flywaydb' && exit 1 || true
```

Expected: tests pass and no forbidden package is reported.

- [ ] **Step 4: Commit the contracts module**

```bash
git add admin-contracts
git commit -m "feat: add framework-neutral authorization and error contracts"
```

### Task 3: Implement Platform RBAC Use Cases and Ports

**Files:**
- Create: `admin-platform/pom.xml`
- Create: `admin-platform/src/main/java/io/github/vaadinadminstarter/platform/{access,audit}/*.java`
- Test: `admin-platform/src/test/java/io/github/vaadinadminstarter/platform/access/GrantPermissionServiceTest.java`

- [ ] **Step 1: Write a failing grant-permission test**

```java
@Test
void granting_permission_requires_role_grant_permission_and_audits_success() {
  service.grant(new CurrentUser(UUID.fromString("00000000-0000-0000-0000-000000000001"),
          "admin", Set.of(PermissionCode.of("system:role:grant")), 1L),
      new GrantPermissionCommand("operator", PermissionCode.of("system:user:read")));
  assertThat(auditSink.events()).singleElement()
      .extracting(AuditEvent::outcome).isEqualTo(AuditOutcome.SUCCESS);
}
```

Run: `./mvnw -pl admin-platform test`  
Expected: FAIL because grant use case and ports do not exist.

- [ ] **Step 2: Define capability-owned models and secondary ports**

Create `UserAccount`, `Role`, `Permission`, `AccessControlRepository`,
`GrantPermissionUseCase`, `GrantPermissionCommand`, and
`GrantPermissionService`. The repository exposes `findRoleByCode`,
`findPermissionByCode`, `grantPermission(UUID roleId, UUID permissionId)`,
`assignRole(UUID userId, UUID roleId)`, `permissionsFor(UUID userId)`, and
`incrementAuthVersion(UUID userId)`. The service checks
`system:role:grant`, verifies role and catalog permission existence, persists
the relation, and emits a `SUCCESS` audit event in the same transaction. Role
assignment additionally increments the assigned user's `auth_version`.

- [ ] **Step 3: Add denial and validation tests**

Assert denied grants never call `AccessControlRepository` and emit a denied
audit outcome. Invalid codes produce `BusinessFailure` with
`validation.failed` and field key `permissionCode`.

- [ ] **Step 4: Verify and commit**

```bash
./mvnw -pl admin-platform test
git add admin-platform
git commit -m "feat: add RBAC platform use cases and audit ports"
```

### Task 4: Build the Vaadin Flow Application Shell and Permission Gates

**Files:**
- Create: `admin-flow/pom.xml`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/navigation/PageDefinition.java`, `PageRegistry.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/security/PermissionGate.java`, `PermissionRouteGuard.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/layout/MainLayout.java`, `ThemePreference.java`, `WorkbenchI18NProvider.java`
- Create: `admin-flow/src/main/java/io/github/vaadinadminstarter/flow/patterns/PagedGrid.java`, `ConfirmingAction.java`, `FlowFileUpload.java`
- Test: `admin-flow/src/test/java/io/github/vaadinadminstarter/flow/navigation/PageRegistryTest.java`

- [ ] **Step 1: Write a failing page-registry test**

```java
@Test
void rejects_duplicate_page_ids() {
  var registry = new PageRegistry(List.of(page("system-users"), page("system-users")));
  assertThatThrownBy(registry::validate).isInstanceOf(IllegalStateException.class);
}
```

- [ ] **Step 2: Implement static page registration and menu projection**

`PageDefinition` contains page ID, Chinese-first title key, icon key, order,
route target, and required `PermissionCode`. `PageRegistry` validates unique
IDs, routes, and required catalog permissions, then filters entries through
`AuthorizationService`. `MainLayout` renders only filtered entries, offers a
persistent light/dark theme choice, and shows a breadcrumb.
`WorkbenchI18NProvider` loads Chinese messages from resource bundles and permits
later locale bundles. No database field stores a Flow class name or raw route.

- [ ] **Step 3: Implement route and action guards**

`PermissionGate` receives an `AuthorizationService`, `CurrentUser`, and
permission code; it controls visibility or enabled state. A
`BeforeEnterObserver` guard redirects unauthenticated users to login and
denies unauthorized direct routes. Mutation views call platform use cases, which
perform their own authorization.

- [ ] **Step 4: Implement reusable business interaction patterns**

Implement `PagedGrid` on the contracts `PagedQuery`/`PagedResult` types with
fixed page sizes of 20, 50, and 100; loading, empty, and failure presentations;
and server-side sort/filter callbacks. `ConfirmingAction` requires explicit
confirmation before delete or bulk-delete execution. `FlowFileUpload` checks an
action permission before calling `FileStorage`, then stores only returned
`StoredFile` metadata and never exposes a server filesystem path.

- [ ] **Step 5: Verify and commit**

```bash
./mvnw -pl admin-flow test
git add admin-flow
git commit -m "feat: add Flow navigation guards and permission gates"
```

### Task 5: Add PostgreSQL, Flyway, and JPA Secondary Adapters

**Files:**
- Create: `admin-spring-jpa/pom.xml`
- Create: `admin-spring-jpa/src/main/resources/db/migration/V1__create_access_control.sql`
- Create: `admin-spring-jpa/src/main/java/io/github/vaadinadminstarter/springjpa/access/*.java`, `admin-spring-jpa/src/main/java/io/github/vaadinadminstarter/springjpa/audit/*.java`
- Test: `admin-spring-jpa/src/test/java/io/github/vaadinadminstarter/springjpa/AccessControlRepositoryIT.java`

- [ ] **Step 1: Write a failing Testcontainers migration test**

```java
@Testcontainers
class AccessControlRepositoryIT {
  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  @Test void flyway_creates_users_roles_permissions_and_audit_tables() {
    assertThat(jdbcTemplate.queryForObject(
        "select count(*) from information_schema.tables where table_name = 'users'",
        Integer.class)).isEqualTo(1);
  }
}
```

Run: `./mvnw -pl admin-spring-jpa verify`  
Expected: FAIL because migration and adapter do not exist.

- [ ] **Step 2: Write V1 migration and adapters**

Create PostgreSQL tables `users`, `roles`, `permissions`, `user_roles`,
`role_permissions`, and `audit_entries`; use UUID primary keys, unique codes,
foreign keys, and indexes for username, permission code, and audit occurrence
time. `users` contains `username`, `password_hash`, `enabled`, and
`auth_version`; `permissions` contains `code` and `system_managed`; and
`audit_entries` contains every `AuditEvent` field from the shared contract.
Implement `AccessControlRepository` and `AuditSink` with JPA entities kept
entirely inside this module. Add a transactional `PermissionCatalogSynchronizer`
that inserts missing catalog permissions and rejects attempts to change a
system-managed code through an administration view.

- [ ] **Step 3: Test transactional audit behavior**

Add integration tests for successful role grants, denied grants, redacted audit
metadata, and migration reruns. Use PostgreSQL, not H2.

- [ ] **Step 4: Verify and commit**

```bash
./mvnw -pl admin-spring-jpa -am verify
git add admin-spring-jpa
git commit -m "feat: add PostgreSQL RBAC and audit adapters"
```

### Task 6: Add Spring Security and Spring Boot Error Adapters

**Files:**
- Create: `admin-spring-security/pom.xml`
- Create: `admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/**/*.java`
- Create: `admin-spring-boot/pom.xml`
- Create: `admin-spring-boot/src/main/java/io/github/vaadinadminstarter/springboot/error/*.java`
- Test: `admin-spring-boot/src/test/java/io/github/vaadinadminstarter/springboot/error/ProblemDetailMapperTest.java`

- [ ] **Step 1: Write a failing RFC 9457 mapper test**

```java
@Test
void maps_authorization_denied_to_rfc9457_problem_detail() {
  ProblemDetail detail = mapper.map(new BusinessFailure(
      ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of()));
  assertThat(detail.getStatus()).isEqualTo(403);
  assertThat(detail.getProperties()).containsEntry("errorCode", "authorization.denied");
}
```

- [ ] **Step 2: Implement local authentication and current-user bridge**

Configure Spring Security with Vaadin's security configurer, an anonymous
`LoginView`, BCrypt/Delegating password encoding, and a bridge that maps the
authenticated principal to `CurrentUser`. A request/session validation filter
compares the principal's stored authentication version with `users.auth_version`
and logs the user out when they differ. Password, account-state, and user-role
changes increment `auth_version` in their authorized platform use case.

- [ ] **Step 3: Implement separate Flow and HTTP error mappers**

`ProblemDetailMapper` is used only by custom MVC/REST exception handling and
returns `application/problem+json` with `type`, `title`, `status`,
`detail`, `errorCode`, and `correlationId`. `FlowErrorMapper` maps the
same failure to field messages, notifications, or 403/500 views. Do not apply
MVC advice to Vaadin internal protocol requests.

Add a `CorrelationIdFilter` that accepts a valid inbound `X-Correlation-Id` or
generates a UUID, puts it in the MDC, returns it in the response header, and
makes it available to audit factories and `ProblemDetailMapper`. Add Spring
Boot Actuator health configuration and JSON structured logging for `prod`; do
not add a metrics or tracing exporter.

- [ ] **Step 4: Verify and commit**

```bash
./mvnw -pl admin-spring-security,admin-spring-boot -am verify
git add admin-spring-security admin-spring-boot
git commit -m "feat: add Spring authentication and error adapters"
```

### Task 7: Assemble the Reference Application and Administration Views

**Files:**
- Create: `admin-reference-app/pom.xml`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/Application.java`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/**/*.java`
- Create: `admin-reference-app/src/main/resources/application.yaml`
- Test: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationContextIT.java`

- [ ] **Step 1: Write a failing application-context test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationContextIT {
  @Test void starts_with_the_postgresql_adapter() {}
}
```

- [ ] **Step 2: Implement composition root and bootstrap data**

Create `Application`, import Spring Boot adapter configuration, define local
PostgreSQL configuration, and seed a documented bootstrap administrator only on
an empty development database. Require an environment-provided bootstrap password
outside development. The seed creates role assignments from `PermissionCatalog`,
hashes the password with the configured encoder, and writes a bootstrap audit
event without password metadata.

- [ ] **Step 3: Implement administration views**

Register users, roles, permissions, and audit pages. Each list has server-side
pagination/filtering, empty/loading/error states, and destructive-action
confirmation. Role assignment and permission grants call platform use cases,
never repositories. Use Chinese resource keys for visible labels and validation
messages. The users list supports row and bulk enable/disable only when
`system:user:update` is granted; the roles page supports permission grants only
when `system:role:grant` is granted. Catalog permissions are read-only.

- [ ] **Step 4: Add one independent sample business module**

Create a small `customer` module with list and edit views. It proves a team can
add a page, catalog permission, navigation entry, JPA persistence, use case,
audit action, file upload/download extension point, and test without modifying
the RBAC platform module.

- [ ] **Step 5: Verify and commit**

```bash
./mvnw -pl admin-reference-app -am verify
git add admin-reference-app
git commit -m "feat: add reference administration and customer module"
```

### Task 8: Enforce Architecture and Add Browser E2E Tests

**Files:**
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/*.java`
- Modify: root `pom.xml`

- [ ] **Step 1: Write failing ArchUnit rules**

```java
@ArchTest
static final ArchRule core_does_not_depend_on_spring = noClasses()
    .that().resideInAnyPackage("..contracts..", "..platform..", "..flow..")
    .should().dependOnClassesThat().resideInAnyPackage(
        "org.springframework..", "jakarta.persistence..", "org.flywaydb..", "..springjpa..");
```

- [ ] **Step 2: Implement browser E2E coverage**

Use Playwright against the reference application and create:
`adminCanAssignUserReadPermission`, `unassignedUserCannotOpenUsersRoute`,
`unassignedUserCannotSeeCreateUserAction`, and
`roleGrantCreatesAuditEntry`. Add `customerCrudWithAttachment` to verify the
sample CRUD path and file extension point. Use isolated PostgreSQL data for each
suite and verify a protected direct route resolves to the Flow access-denied
view rather than an MVC Problem Details response.

- [ ] **Step 3: Run quality gates and commit**

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
git add pom.xml admin-reference-app/src/test
git commit -m "test: enforce module boundaries and authorization flows"
```

Expected: unit, architecture, integration, E2E, and production-build checks pass.

### Task 9: Package, Containerize, and Document Operation

**Files:**
- Create: `Dockerfile`, `docker-compose.yml`, `.dockerignore`, `.env.example`
- Create: `docs/quick-start.md`, `docs/security.md`, `docs/extension-guide.md`, `docs/deployment.md`
- Modify: `README.md`

- [ ] **Step 1: Build a non-root production image**

Use a Java 25 JRE final stage, copy only the executable artifact, run as a
non-root UID, expose port 8080, and set `SPRING_PROFILES_ACTIVE=prod`.

- [ ] **Step 2: Add Docker Compose PostgreSQL development stack**

Define PostgreSQL 18 with a named volume, health check, development credentials
from `.env`, and an application service waiting for database health.
`.env.example` contains non-secret local values only.

- [ ] **Step 3: Document operation and extension**

Document startup, bootstrap-account setup, schema migration, backup boundaries,
permission-catalog extension, Flow page registration, new business modules,
file-storage extension, Chinese message/brand customization, Problem Details,
correlation IDs, and version upgrades. Create `docs/contributing.md` with the
required Java 25/Maven 4 baseline, test commands, formatting policy, and the
rule that a new adapter requires an architecture decision.

- [ ] **Step 4: Verify and commit**

```bash
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:local .
git add Dockerfile docker-compose.yml .dockerignore .env.example docs README.md
git commit -m "docs: add deployment and extension guidance"
```

### Task 10: Add CI, Release Metadata, and Final Verification

**Files:**
- Create: `.github/workflows/verify.yml`, `.github/dependabot.yml`, `CHANGELOG.md`
- Modify: `README.md`

- [ ] **Step 1: Add Maven 4 RC6 GitHub Actions verification**

The workflow installs JDK 25, runs `./mvnw -v`, enables Docker for
Testcontainers, runs `./mvnw -B -ntp verify`, then
`./mvnw -B -ntp -Pproduction verify`. Cache `.m2/repository` using all
`pom.xml` files and wrapper properties.

- [ ] **Step 2: Add dependency-update and release policy**

Configure Dependabot for Maven and GitHub Actions. Document Maven 4 RC6, Java
25, Spring Boot 4.1.0, and Vaadin 25.2.5 as the tested compatibility baseline.
A version upgrade requires a dedicated PR and complete CI verification.

- [ ] **Step 3: Run final checks and commit**

```bash
git status --short
./mvnw -B -ntp verify
docker compose --env-file .env.example config
git log --oneline --max-count=10
git add .github CHANGELOG.md README.md
git commit -m "ci: verify Maven 4 build and release baseline"
```

Expected: the worktree is clean after commit, Maven verification passes, Compose
configuration is valid, and each task has one focused commit.

## Plan Self-Review

Coverage mapping: Tasks 2-4 implement contracts, file extension boundaries,
standard CRUD interactions, Flow shell, Chinese-first localization, theme
selection, navigation, RBAC rules, and framework-neutral boundaries. Tasks 5-7
implement PostgreSQL, Flyway, catalog synchronization, authentication,
auth-version invalidation, audit, correlation IDs, Problem Details, and the
reference application. Tasks 8-10 implement architecture checks, browser tests,
Docker, documentation, CI, and compatibility policy.

The plan contains no deferred implementation placeholders. Module names,
permission codes, use-case names, and error-mapper names match the approved
architecture document.
