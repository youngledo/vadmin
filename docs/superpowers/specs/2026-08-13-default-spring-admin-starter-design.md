# Default Spring Admin Starter Design

**Status:** Approved
**Date:** 2026-08-13

## Purpose

Make Vaadin Admin Starter directly useful to a Java team creating a Spring Boot
application. A consumer that adds one starter dependency and configures its
database receives a complete Flow administration shell and baseline system
administration. It then contributes its own business modules in Java.

This is not a component catalogue that asks each consumer to assemble an
`AppLayout`, navigation, theme, or administration pages. It is a Flow-native,
Java-only administration baseline comparable in adoption shape to Ant Design
Pro, while retaining Vaadin Flow as the only UI programming model.

## Decisions

1. Create `admin-spring/admin-spring-starter` as the primary public Spring Boot
   dependency.
2. The starter owns the default shell, theme, appearance settings, home page,
   system-administration module descriptors, Users/Roles/Permissions/Audit
   views, their application services, and their translations.
3. `admin-reference-app` becomes a thin, real consumer and E2E acceptance
   application. It owns only bootstrapping, deployment-safe configuration, test
   data, and tests that prove the starter works from the consumer boundary.
4. Delete `admin-example-orders` and the `admin-examples` reactor aggregate.
   They are fictional domain code and should not be distributed as baseline
   product surface.
5. Delete the customer sample, local file-storage implementation, customer
   migrations, translations, routes, and customer-specific tests from the
   reference application. The reference application validates system
   administration, not a second fictional business domain.
6. Preserve `AdminModule`, `AdminModuleRegistry`, `AdminPage`,
   `AdminNavigationGroup`, `AdminMessageBundle`, and `admin-spring-flow` as the
   external business-module contract. A real consumer contributes an
   `AdminModule` bean; it does not declare `@Route`, compose the shell, or
   register a host permission catalog.
7. Preserve the Spring-free boundary of `admin-contracts`, `admin-platform`,
   and `admin-flow`. Spring Boot remains the only supported runtime in this
   release.

## Product Shape

```text
Consumer Spring Boot application
  -> admin-spring-starter
       -> security, JPA/Flyway, Problem Details, Flow module assembly
       -> default Flow shell and theme
       -> system administration: users, roles, permissions, audit
  -> consumer AdminModule beans
       -> consumer permissions, pages, translations, view beans
```

The default shell is the `AdminHostLayout` supplied by the starter. It renders
the permission-filtered projection of `AdminModuleRegistry`, account actions,
locale selection, visual-language profile, colour mode, density, current page,
and the production `@Uses` anchors needed for dynamically registered views.
The starter supplies the `admin-theme` assets and its `AppShellConfigurator`.

The home page is also starter-owned. It uses the same registry and
`I18NProvider` as the shell, so external module metadata is displayed in the
chosen locale rather than as raw message keys.

## Override Boundary

The default experience must be useful without configuration, but a host may
intentionally replace it.

- The starter supplies its `AdminHostLayout`, shell application class, and
  appearance properties only when the consumer has not supplied equivalents.
- A host that replaces the layout must explicitly provide an `AdminHostLayout`
  and own its `@Theme`/`AppShellConfigurator` configuration. It continues to
  use `AdminModuleRegistry`, the module assembler, permission catalog, and
  composite i18n provider.
- A host may configure branding and supported appearance defaults through the
  documented starter properties. Arbitrary end-user theme editing, a visual
  page builder, and per-tenant branding are outside this phase.
- Starter-owned system administration cannot be partially replaced page by
  page. A host either uses the coherent default administration module or
  replaces the complete shell/module composition deliberately.

This makes the normal path simple and makes the escape hatch explicit rather
than creating accidental mixed ownership of routes, themes, or navigation.

## Module Ownership

| Module | After this phase |
| --- | --- |
| `admin-contracts` | Framework-free identity, authorization, audit, errors, and file contracts. |
| `admin-platform` | Framework-free RBAC use cases and ports. |
| `admin-flow` | Spring-free Flow patterns, route guards, and module metadata contract. |
| `admin-spring-security` | Local authentication and optional standards-based OIDC adapter. |
| `admin-spring-jpa` | JPA/Flyway access control and audit adapters. |
| `admin-spring-boot` | Correlation and Problem Details configuration. |
| `admin-spring-flow` | Module assembly, dynamic routes, composite translations, and locale preference. |
| `admin-spring-starter` | Default shell/theme, system administration UI/services/translations, and consumer-facing dependency composition. |
| `admin-reference-app` | Starter consumer, launch configuration, seed data, and browser acceptance tests. |

## Public Adoption Contract

The documented initial consumer path is:

1. Depend on `admin-spring-starter`.
2. Configure a PostgreSQL datasource and Flyway migration location.
3. Start the application to receive local login, shell, Users, Roles,
   Permissions, Audit, locale, appearance, and the selected Flow visual
   language.
4. Add business functionality as Spring view beans and `AdminModule` beans
   with `AdminPage`, declared permissions, and two message bundles.

The external module contract continues to validate duplicate module IDs, page
IDs, routes, permission declarations, navigation groups, and message bundles
at startup. Its visible pages are filtered by existing authorization in both
navigation and direct-route guards.

## Non-goals

- React, Vue, Hilla, or embedded Ant Design/Naive UI/Element UI components.
- A second Spring runtime, multi-tenancy, data-scope authorization, automatic
  identity provisioning, or provider-specific identity SDKs.
- Fictional orders/customer domains distributed as example business modules.
- Multi-tab workspace or persisted per-account UI preferences.
- Replacing the Vaadin baseline visual profile; the Vaadin and Ant-inspired
  Flow profiles remain parallel theme choices.

## Acceptance Criteria

1. A reference application uses `admin-spring-starter` rather than copied
   shell/theme/system-administration source and provides the complete default
   experience after startup.
2. System administration routes are provided by the starter, function through
   the existing RBAC use cases, and appear in the permission-filtered shell.
3. Starter and third-party module titles/intents resolve through the composite
   `I18NProvider` for `zh-CN` and `en-US`.
4. An external `AdminModule` can appear in shell navigation and home shortcuts
   with translated text, its declared icon, and permission filtering.
5. No orders or customer production routes, migrations, translations, module
   dependencies, or documentation references remain outside historical plans
   and specifications.
6. Focused unit/architecture tests, the normal reactor build, production
   build, and browser acceptance coverage pass.

## Migration and Documentation

The architecture, quick-start, extension, and release guides will describe the
starter-first path. Extension examples will use a generic business module,
not a shipped fictional domain. Historical plans and specifications are kept
unchanged as records of past work; the current roadmap records the new product
direction.
