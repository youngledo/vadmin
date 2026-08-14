# VAdmin - Architecture

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

English | [简体中文](../zh-CN/architecture.md)

## Scope

VAdmin is a production-oriented baseline for Java internal
applications. Release 1 supports Java 25, Spring Boot 4.x, Vaadin Flow 25.x,
PostgreSQL, and Flyway SQL migrations. Spring Boot is the only runtime and
Vaadin Flow is the only UI programming model.

The reusable core remains independent of Spring, JPA, Flyway, and application
business types. The starter composes the Spring adapters into a useful default
experience; a consuming application owns its business domain and deployment
configuration.

## Module Ownership

| Module | Responsibility |
| --- | --- |
| `admin-contracts` | Framework-free identity, authorization, audit, error, and file contracts. |
| `admin-platform` | Framework-free RBAC use cases and ports. |
| `admin-flow` | Spring-free Flow patterns, route guards, and module metadata. |
| `admin-spring-security` | Local authentication and optional standards-based OIDC adapter. |
| `admin-spring-jpa` | JPA/Flyway RBAC and audit adapters. |
| `admin-spring-boot` | Correlation and Problem Details configuration. |
| `admin-spring-flow` | Module assembly, dynamic routes, composite translations, and locale preference. |
| `vadmin-spring-boot-starter` | Default shell and theme, system administration UI and translations, and consumer-facing dependency composition. |
| `vadmin-reference-app` | Thin starter consumer, launch configuration, seed data, and browser acceptance coverage. |

`vadmin-spring-boot-starter` supplies the shell, theme, home page, and the Users,
Roles, Permissions, and Audit administration module. `vadmin-reference-app`
does not own a copied baseline; it proves that a normal application can depend
on the starter and contribute only its own functionality.

## Default Consumer Path

```text
Consumer application
  -> vadmin-spring-boot-starter
       -> Spring security, JPA, Boot, and Flow adapters
       -> default shell/theme and system administration
  -> consumer AdminModule beans and Flow view beans
```

A normal consumer configures its datasource and Flyway migration location,
depends on `vadmin-spring-boot-starter`, and starts the application. It receives
local login, the permission-filtered shell, Users, Roles, Permissions, Audit,
`zh-CN`/`en-US` translations, light/dark mode, and Vaadin or Ant-inspired
appearance profiles without defining a layout, theme, or system pages.

Business features are declared as `AdminModule` beans. Each page has stable
metadata, a required permission, a route, an icon key, a Flow view type, and
two message bundles. At startup `admin-spring-flow` validates module IDs, page
IDs, routes, permissions, navigation groups, and translation resources, then
assembles one permission catalog, `AdminModuleRegistry`, composite
`I18NProvider`, and dynamic route registration.

Navigation is a projection of assembled pages filtered by authorization. A
route guard also checks direct navigation before the view is constructed; use
cases authorize mutations again. Navigation controls improve the user
experience, while the use-case check is the authoritative boundary.

## Themes And Production Builds

The starter's default application shell registers `admin-theme`. Its semantic
`--admin-*` token contract supports light/dark mode, `vaadin` and `ant` visual
languages, and comfortable/compact density. Modules consume the public tokens
and shared Flow patterns; they do not register a global theme or modify global
Lumo variables.

Dynamic routes need a static production frontend anchor. A consumer that adds
a dynamic business view declares `@Uses(ThatView.class)` once on a host-owned
application configuration type. The view itself has no `@Route`: the assembled
module metadata is the source of route registration.

## Intentional Full Replacement

The default path is all-or-nothing baseline ownership. A consumer that needs a
different shell may deliberately provide `AdminHostLayout` and own its
`AppShellConfigurator` and `@Theme` configuration. That is a complete shell
replacement, not a way to replace selected default pages or styles.

The consumer still uses `AdminModuleRegistry`, module assembly, permissions,
route guards, and the composite translation provider. It must supply a coherent
layout and production anchors for all dynamic views it composes. Consumers that
do not need this boundary should use the starter default unchanged.

## Operations And Evolution

Flyway runs before JPA-backed adapters. Local-password login is the baseline;
OIDC is an optional standard authorization-code adapter that maps an external
identity to an existing enabled local account. It does not provision accounts,
assign roles, synchronize groups, or expose provider tokens to Flow modules.

The starter records audit outcomes for authentication and administration,
propagates correlation IDs, and maps HTTP failures to RFC 9457 Problem Details.
Deferred areas include multi-tenancy, data-scope authorization, SAML/LDAP/MFA,
SCIM, non-Spring runtimes, a workflow engine, low-code UI construction, and
runtime loading of arbitrary views.
