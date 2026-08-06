# Vaadin Admin Starter - Architecture Design

Status: Draft 0.1  
Date: 2026-08-04

## 1. Scope and Baseline

Vaadin Admin Starter is an Apache-2.0, production-oriented starter for
Java teams building internal business applications. Release 1 supports Java 25,
Vaadin Flow 25.x, Spring Boot 4.x, PostgreSQL, and Flyway SQL migrations.

Vaadin Flow is the sole release-1 UI programming model. Hilla, React, and
TypeScript do not enter the build or reusable-module dependency graph. A future
Hilla adapter may reuse contracts and platform use cases, but is not part of
this starter's compatibility baseline.

Spring Boot is the sole release-1 runtime. Reusable modules must not import
Spring Framework, Spring Boot, Spring Security, Spring Data, JPA, or Flyway
types. Those integrations are deliberately isolated so a future runtime can
implement the same contracts without rewriting the Flow UI or RBAC use cases.

This design uses a modular monolith with selected ports and adapters. It does
not introduce CQRS, Event Sourcing, a command bus, or interfaces without a
meaningful runtime, persistence, security, or testing boundary.

## 2. Architecture Decisions

### 2.1 Dependency Direction

```text
Reference application -> Spring adapters -> Flow/platform/contracts
Flow/platform -> contracts
Spring adapters -> Flow/platform/contracts
Contracts -> Java only
```

The reference application is the composition root. It selects Spring Boot,
installs adapters, and contains the example business module. No reusable module
depends on it.

### 2.2 Modules

| Module | Responsibility | Allowed dependencies |
|---|---|---|
| `admin-contracts` | `CurrentUser`, authorization, audit, navigation, file contracts and error semantics | Java 25 |
| `admin-platform` | RBAC administration use cases, application models, persistence and audit ports | contracts |
| `admin-flow` | route guards, permission gates, Java page patterns, Flow-specific error presentation | contracts, platform, Vaadin Flow |
| `admin-spring` | Maven parent and reactor aggregator for Spring-specific adapters; no runtime code | root POM inheritance only |
| `admin-spring-security` | local account authentication and `CurrentUser`/authorization adapters | Spring Security, contracts, platform |
| `admin-spring-jpa` | PostgreSQL JPA mappings, RBAC/audit port implementations, Flyway integration | Spring/JPA/Flyway, contracts, platform |
| `admin-spring-boot` | auto-configuration, HTTP error mapping, logging and observability wiring | Spring Boot, all adapter-facing modules |
| `admin-reference-app` | Spring Boot launcher, deployment configuration, sample domain module | reusable modules |

`admin-spring` is an organizational Maven parent and reactor aggregator; it has no runtime code or runtime dependency role. Consumers continue to depend on the three leaf artifacts directly.

`admin-flow` is a primary adapter for user actions. JPA, Spring Security,
file storage, and HTTP APIs are secondary or protocol-specific adapters. A Flow
View never talks directly to JPA repositories.

### 2.3 Flow Design System

Phase 1 keeps reusable Flow patterns in `admin-flow`, and keeps theme assets
and application composition in `admin-reference-app`:

```text
ApplicationShell (@Theme("admin-theme"))
  -> admin-theme/theme.json + styles.css
  -> MainLayout (AppLayout)
       -> PageRegistry.visibleTo(currentUser, authorization)
       -> protected Flow views
            -> PageHeader / PageToolbar / DataWorkspace / EditorDialog
```

`ApplicationShell` is the Flow `AppShellConfigurator`; it is the required
place for the application-wide `@Theme` annotation. `MainLayout` is an
application composition class, not a reusable Spring adapter. It obtains the
permission-filtered page projection from `PageRegistry`, supplies the product
header and grouped navigation, and updates the current route title after
navigation.

The named `admin-theme` lives at
`admin-reference-app/src/main/frontend/themes/admin-theme/`. Its CSS owns
semantic light and dark color tokens, density, focus, shell, canvas, and narrow
viewport rules. The current-user menu stores the selected light/dark mode in
the Vaadin session and applies it to the Flow UI root. It is intentionally not
a persisted account preference in Phase 1. A consuming application can create
its own app shell and named theme without modifying `admin-flow`.

`admin-flow` remains Spring-free: it may depend on Vaadin Flow,
`admin-contracts`, and `admin-platform`, but must not import Spring Framework,
Spring Boot, Spring Security, JPA, Flyway, the reference application, or its
business types. ArchUnit enforces the framework-import portion of this
boundary; module dependency direction keeps the reference application outside
the reusable modules.

## 3. Authorization and Navigation

### 3.1 Permission Model

Permissions are the authorization vocabulary; roles are configurable groups of
permissions. Permission codes use `domain:resource:action`, for example:

```text
system:user:read
system:user:create
system:role:grant
```

The initial model is many-to-many:

```text
users <-> user_roles <-> roles <-> role_permissions <-> permissions
```

The permission catalog is defined in code and synchronized into the database as
system-managed records. Administrators grant catalog permissions to roles, but
cannot invent arbitrary permission codes in the UI. This keeps page declarations,
actions, tests, and documentation aligned.

### 3.2 Page Registry and Menu Projection

Each Flow page contributes a stable `pageId`, required permission code, view
class, and default navigation metadata. Navigation is a projection of the page
registry filtered by `AuthorizationService`. Direct route entry is checked by a
Flow route guard before a view is constructed.

Release 1 does not store arbitrary routes or Java class names in PostgreSQL. A
future menu-configuration module may persist ordering, titles, or visibility,
but it can only reference existing `pageId` values.

### 3.3 Authorization Boundaries

1. Navigation excludes pages the current user cannot access.
2. Route guards deny direct access to protected pages.
3. `PermissionGate` hides or disables unavailable actions for a coherent UI.
4. Platform use cases authorize the action again before mutation.

The first three are user-experience controls. The fourth is the authoritative
security boundary.

## 4. Data and Audit

`users` stores a unique username, password hash, enabled state, and
`auth_version`. A password change, disablement, or role change increments
`auth_version` so active sessions can be invalidated or refreshed.

`audit_entries` records authentication outcomes and platform-administration
operations. Its fields are occurrence time, actor user ID when known, action
code, target type and ID, outcome, correlation ID, and redacted metadata.

Successful administrative mutations write their audit record in the same
database transaction. Authentication failures and authorization denials record
an outcome when an actor or requested account can be safely identified. Password
hashes, raw passwords, tokens, SQL, and stack traces are never audited.

## 5. Runtime, Errors, and HTTP Semantics

Spring Boot starts Flyway before JPA-backed adapters are available. Spring
Security authenticates local accounts and supplies a framework-neutral
`CurrentUser`. Flow route guards and platform use cases call the same
authorization contract.

The core defines error codes and structured failure information, for example
`authorization.denied`, `validation.failed`, `resource.not_found`,
`conflict.version`, and `internal.error`. It does not depend on web response
types.

Flow interactions are rendered through `FlowErrorMapper` as field validation,
safe notifications, or dedicated 403/500 views. Custom REST/MVC APIs are
rendered through `ProblemDetailMapper` as RFC 9457 `application/problem+json`.
API responses include a stable problem type, HTTP status, safe detail, error
code, and correlation ID. API 401/403 responses use Problem Details; browser
Flow navigation instead uses the login view or an access-denied view.

## 6. Reliability and Operations

Every request receives a correlation identifier that is included in structured
logs, audit entries where applicable, and Problem Details. Unknown failures are
logged with diagnostic context and exposed only as a safe error ID.

The reference application uses externalized configuration and environment
variables. Development credentials are isolated from production profiles.
Spring Boot health checks, structured logging, and container-friendly shutdown
are release-1 requirements. Metrics and tracing exporters remain extension
points.

## 7. Testing and Delivery

| Test layer | Scope |
|---|---|
| Unit | contracts and platform authorization, RBAC invariants, audit event construction |
| Architecture | ArchUnit checks that core modules do not import Spring, JPA, Flyway, or the Spring JPA adapter package |
| Integration | Testcontainers PostgreSQL validates Flyway, JPA ports, authentication adapters, transactions, and audit writes |
| Browser E2E | login, desktop and narrow shell navigation, session theme switching, menu filtering, direct route denial, validation and failure presentation, permission change, and sample CRUD path |

CI runs formatting and static checks, all test layers, a production build, and a
Docker image build. Releases publish the compatibility baseline and treat a new
Java, Spring Boot, or Vaadin major version as dedicated upgrade work.

## 8. Evolution Boundaries

The following are explicitly deferred: multi-tenancy, organization hierarchy,
data-scope authorization, OIDC/SAML/LDAP/MFA, non-Spring runtimes, a workflow
engine, a low-code designer, and dynamic loading of arbitrary views.

A future Quarkus, Helidon, Jakarta EE, or servlet adapter implements the
contracts and owns its runtime composition. It does not modify the permission
catalog, platform use cases, or Flow UI patterns.

## 9. Validation Rules

The following are project policies and are enforced with tests:

1. Core modules do not import Spring, JPA, Flyway, or the Spring JPA adapter package.
2. Flow views call platform use cases, never JPA repositories.
3. Every protected page and action uses a catalog permission code.
4. Every platform mutation checks authorization and produces an audit outcome.
5. Persisted menu configuration may reference only known page IDs.
6. HTTP APIs return RFC 9457 Problem Details for mapped failures; Flow protocol
   requests are handled by Flow-specific error handling.

## 10. Architecture Guidance Result

- **Phase**: Architecture Guidance
- **Status**: completed
- **Inputs**: confirmed requirements, modular-monolith choice, and Spring-first
  runtime with a framework-neutral reusable core.
- **Summary**: selected meaningful ports/adapters for security, persistence,
  audit, and presentation while retaining a simple modular monolith.
- **Assumptions**: release 1 remains an internal-business-application starter;
  local accounts are sufficient; PostgreSQL is the only production database.
- **Decisions**: framework independence is a project policy enforced by module
  dependencies and ArchUnit; CQRS and Event Sourcing are not justified.
- **Constraints**: Flow is a primary adapter, the core owns use cases and
  contracts, and Spring/JPA implementations remain outside the core.
- **Evidence**: the requirements document, architecture diagrams, Spring's
  RFC 9457 `ProblemDetail` support, and Testcontainers PostgreSQL support.
- **Open Questions**: none that block implementation planning.
- **Artifacts**: requirements document and architecture diagrams in `outputs/`.
- **Recommended Next Step**: produce a staged implementation plan.
- **Handoff Notes**: package names are project conventions; the enforced rules
  are the documented dependency and security boundaries, not a claim that every
  Java business application requires this architecture.
