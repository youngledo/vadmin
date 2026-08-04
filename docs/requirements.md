# Vaadin Admin Starter - Requirements

Status: Draft 0.1  
Date: 2026-08-04

## 1. Product Definition

Vaadin Admin Starter is a production-oriented starter for Java teams that
build internal business web applications. It is based on Vaadin Flow and offers
a consistent application shell, security baseline, common business interaction
patterns, operational defaults, and a runnable reference application.

The first release runs on Spring Boot, but its reusable core must not expose
Spring APIs. Framework-specific functionality belongs to adapter modules so
future Quarkus, Helidon, Jakarta EE, or servlet-container integrations remain
possible.

## 2. Target Users

- Java teams building internal systems such as CRM, operations consoles,
  approval workbenches, inventory, customer service, or data operations tools.
- Developers who prefer a server-side Java UI with Vaadin Flow rather than a
  separate TypeScript frontend application.
- Teams that require a durable project baseline, not a collection of visual
  demo pages.

The project does not target consumer-facing marketing sites or frontend-only
development teams.

## 3. Goals

1. A developer can start the reference application locally with a documented
   command and replace the example business module without rebuilding shared
   infrastructure.
2. The starter provides coherent, secure-by-default patterns for authentication,
   authorization, navigation, auditing, CRUD, error handling, and deployment.
3. Reusable modules have Java and Vaadin Flow dependencies only, except where a
   deliberately defined adapter boundary is required.
4. The default Spring Boot integration is tested as a real deployable
   application, rather than only as isolated UI samples.
5. The project is understandable to a Java team: its extension points,
   conventions, data flow, and production operation are documented in Chinese.

## 4. Release 1 Scope

### 4.1 Technical Baseline

- Java 25.
- Vaadin Flow 25.x.
- Spring Boot 4.x integration as the only supported runtime in release 1.
- PostgreSQL as the default production database for the reference application.
- Flyway with versioned SQL migrations.
- Maven build, reproducible local development, and a production build profile.
- Docker image and Docker Compose development environment.

### 4.2 Application Shell

- Responsive main layout with header, collapsible side navigation, breadcrumb
  or equivalent location indication, user menu, and content area.
- Navigation entries declared through a framework-neutral model and filtered by
  the current user's permissions.
- Light/dark theme support and one documented customization path for branding.
- Chinese-first UI text; internationalization must be extensible, but a complete
  multi-language product is out of scope.

### 4.3 Identity and Authorization

- Login, logout, authenticated session handling, and an anonymous login view.
- Local username/password accounts are the only release-1 authentication
  mechanism.
- Persistent users, roles, and permissions using a many-to-many RBAC model.
- Permissions use stable permission codes as the single authorization vocabulary;
  roles are configurable collections of permissions rather than UI-facing
  authorization rules.
- Page/menu access and action-level access use the same permission-code
  vocabulary.
- Page registrations declare a stable page identifier and required permission;
  persisted menu configuration may reference the page identifier but may not
  supply arbitrary Java classes or routes.
- A framework-neutral `CurrentUser` abstraction and permission evaluation
  interface are consumed by reusable modules.
- Spring Security is an adapter concern; no Spring Security types appear in the
  framework-neutral core API.
- Sample management screens for users, roles, and permissions demonstrate the
  intended extension pattern.

### 4.4 Reusable Business Patterns

- A standard list page pattern: server-side pagination, sort, filter, empty,
  loading, and failure states.
- A standard create/edit pattern with binding, validation, confirmation for
  destructive operations, and clear server-side error feedback.
- A table bulk-action pattern with permission checks for each action.
- A basic file upload/download extension point. Specific object-storage vendors
  are not part of release 1.
- One example domain module, separate from platform administration, proving how
  a team adds views, permissions, navigation, persistence, and tests.

### 4.5 Audit, Reliability, and Operations

- Record login and platform-administration operations with actor, action,
  timestamp, target reference, result, and correlation identifier when present.
- Central error handling with a user-safe message and server-side diagnostic
  logging. Stack traces and sensitive values must never be shown in the UI.
- Vaadin Flow interactions use Flow-specific error views, notifications, and
  field validation feedback. Custom HTTP APIs use RFC 9457 Problem Details;
  the framework-neutral core does not expose Spring `ProblemDetail` types.
- Problem Details include a stable error code and correlation identifier, but
  never include stack traces, tokens, passwords, SQL, or sensitive metadata.
- Configuration by environment variables and externalized configuration files;
  development defaults must not be production credentials.
- Spring Boot health endpoint, structured application logs, and documented log
  correlation. Metrics/tracing exporters are extension points, not mandatory
  release-1 integrations.
- Database migrations run deterministically for an empty environment and during
  upgrades.

### 4.6 Quality and Delivery

- Unit tests for core authorization and audit abstractions.
- Integration tests for the Spring adapter, authentication, authorization, and
  schema migration.
- Browser-level tests for the login-to-authorized-operation path and the sample
  CRUD flow.
- CI verifies formatting, tests, production build, and container image build.
- Documentation includes quick start, architecture overview, extension tutorial,
  security configuration, deployment, upgrade policy, and contribution guide.

## 5. Explicitly Out of Scope for Release 1

- Multi-tenancy, organization hierarchy, and row/data-scope authorization.
- OIDC, SAML, LDAP, MFA, and external identity-provider integrations.
- Quarkus, Helidon, Jakarta EE, OSGi, or plain servlet runtime adapters.
- Hilla, React, TypeScript, and any browser-side UI adapter; the release-1 UI
  programming model is Vaadin Flow only.
- A low-code page builder, dynamic schema/form designer, or workflow engine.
- A large catalog of dashboard, chart, ecommerce, and marketing demo pages.
- A universal data-access abstraction that attempts to replace an application's
  domain model or chosen persistence stack.

## 6. Acceptance Criteria

1. A clean checkout starts locally with the documented command; an administrator
   can sign in and reach only allowed navigation entries and operations.
2. Removing a permission prevents both navigation access and the protected
   action, including direct-route access.
3. User, role, and permission changes are persisted and audit records are
   created for the defined administrative operations.
4. The example domain module can be removed without changing core security,
   navigation, audit, or application-shell code.
5. Production build, automated tests, and container build pass in CI.
6. No framework-neutral module imports Spring Boot, Spring Framework, Spring
   Security, Spring Data, or Spring transaction APIs.

## 7. Decisions Recorded

- Audience: Java development teams; frontend developers are not a target user.
- UI programming model: Vaadin Flow.
- Release-1 UI boundary: Vaadin Flow only; a Hilla adapter is a possible future
  extension, not a dependency of this starter.
- Release-1 runtime: Spring Boot only.
- Reference-application production database: PostgreSQL.
- Database migration: Flyway with versioned SQL migrations.
- Supported baseline: Java 25, Vaadin Flow 25.x, and Spring Boot 4.x. Patch and
  compatible minor updates are tested before release; major upgrades are
  delivered through dedicated upgrade work.
- License: Apache-2.0.
- Release-1 identity source: local username/password accounts only; external
  identity-provider integrations are deferred.
- Architectural rule: reusable core is runtime-framework neutral.
- Authorization model: RBAC first; organization, tenant, and data permission
  capabilities are deferred as future optional modules.
- Permission model: permission-code first; roles are permission collections;
  route, menu, action, and use-case checks share one vocabulary.
- Product quality bar: production-oriented starter, not a visual showcase.

## 8. Architecture Readiness

All release-1 architecture decisions are recorded above.
