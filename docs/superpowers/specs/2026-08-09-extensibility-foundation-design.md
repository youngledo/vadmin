# Extensibility Foundation Design

**Status:** Proposed
**Date:** 2026-08-09

## Goal

Make the Spring Boot reference application a dependable host for independently
packaged Java administration modules, while establishing the internationalized
text and theme-token contracts those modules need to remain visually and
linguistically consistent.

This is the first deliverable of roadmap Phase 3. It turns the existing Flow
reference application into an adoptable foundation without turning it into a
dynamic plugin platform or introducing another runtime.

## Scope

### Module Contract

`admin-flow` defines a Spring-independent module descriptor, named
`AdminModule`, with immutable metadata for:

- a globally unique module identifier;
- one or more navigation groups;
- pages, their routes, menu metadata, order, required permissions, and Flow
  view types; and
- module-owned translation bundle base names.

An `AdminPage` belongs to exactly one `AdminNavigationGroup`. Module IDs, group
IDs, page IDs, routes, and translation-key namespaces are stable public
identifiers. A module must use its module ID as the prefix of its translation
keys, for example `orders.page.list.title`.

`admin-flow` and `admin-contracts` remain free of Spring Boot, Spring Security,
JPA, Flyway, and reference-application dependencies. A business module can use
those framework dependencies in its own Spring Boot adapter, but its
`AdminModule` descriptor cannot expose framework-specific services or
configuration types.

### Spring Boot Host Assembly

Spring Boot is the only supported host runtime in this phase. A business module
is delivered as a Maven dependency with a small Boot auto-configuration that
contributes its `AdminModule` descriptor and any module services or views.

`admin-spring` owns the runtime adapter that collects module descriptors and
builds the navigation registry and permission catalog. The host provides the
application's `RouterLayout` through a small Flow-level host-layout contract;
modules never refer to the reference application's `MainLayout` class.

The adapter registers module view types through Flow's public route
configuration API at application initialization. This avoids relying on
Spring Boot's default `@Route` package scan, which does not discover an
arbitrary external Maven package. The implementation must prove that external
views receive Spring-managed dependencies and use the host layout before this
route registration becomes a supported extension contract.

The assembled registry rejects, during startup, duplicate module IDs,
navigation-group IDs with incompatible metadata, page IDs, routes, permission
codes, and translation-bundle descriptors. The failure describes the colliding
values and both module IDs. Startup failures are configuration errors; they
must not be silently resolved by ordering.

### Navigation And Permission Contribution

The reference application's system and customer administration pages become
module declarations rather than manual lists in `ApplicationConfiguration` and
`MainLayout`. The shell renders every visible group and page from the assembled
registry, ordered by group and page order, after the existing authorization
check.

Each module contributes its required permission codes. The assembled
`PermissionCatalog` is the single catalog synchronized by the existing JPA
adapter and granted to the bootstrap administrator. Existing authorization,
audit, and local-account semantics do not change.

The workplace uses the same assembled, permission-filtered page metadata for
its operational entries. It continues to show navigation opportunities, not
invented KPIs or operational data.

### UI Internationalization

UI internationalization is part of this phase. It is distinct from the paused
documentation internationalization work.

The initial supported UI locales are `zh-CN` and `en-US`. When there is no
session preference, the host uses a supported browser locale when available and
falls back to `zh-CN`. A language selection is stored in the Vaadin session;
it changes the current UI locale immediately and does not persist to the user
database in this phase.

`admin-spring` supplies an aggregated Vaadin `I18NProvider` over the core,
reference-application, and enabled-module resource bundles. Resolution is
exact selected locale, then `zh-CN`, then an explicit, logged missing-key
marker. It never performs machine translation or silently falls back to an
unrelated module's bundle.

Public navigation metadata uses translation keys, not display strings. The
shell, workplace, shared Flow patterns, login/error surfaces, and reference
views move their user-visible text into resource bundles. Components whose
content remains visible during a locale change implement Flow's locale-change
contract so labels, headings, dialog commands, feedback, and navigation update
without requiring a new login.

### Theme Tokens

The host retains sole ownership of `@Theme("admin-theme")`; a module cannot
select or replace the application theme. The existing theme becomes the
reference implementation of a documented token contract covering semantic
surface, text, border, accent, success, warning, danger, focus, spacing,
typography, radius, and elevation values for both light and dark modes.

The token contract uses semantic CSS custom properties such as
`--admin-surface`, `--admin-text-primary`, `--admin-accent`, and
`--admin-space-md`. Host theme CSS maps them to the applicable Vaadin theme
variables. Shared patterns and the sample module consume these semantic tokens
or their documented component class names; they do not hard-code unrelated
colors, dimensions, or global overrides.

This phase supports host branding through token overrides and the existing
light/dark choice. It does not support runtime-installable themes, arbitrary
module-wide stylesheets, visual theme editing, or tenant-specific brands.

### Independent Orders Example

Add `admin-examples/admin-example-orders` as an independently packaged Maven
module. It models a small, deterministic order-administration example solely
to demonstrate the extension contract:

- Maven dependency plus Boot auto-configuration discovers the module;
- an `orders` descriptor contributes a business navigation group, a protected
  orders worklist, a detail view, permissions, and `zh-CN`/`en-US` messages;
- views reuse `DataWorkspace`, `DetailDialog`, `OperationFeedback`, and the
  host theme tokens; and
- it has no compile-time dependency on `admin-reference-app`, its layout, or
  its persistence implementation.

The example is not a new business vertical, order-management product, or
dashboard. It exists to provide a working template for an adopting Java team.

## Non-Goals

- A dynamic plugin marketplace, runtime installation or removal, scriptable
  modules, or hot deployment.
- A second runtime adapter, including Helidon, Quarkus, or a non-Spring Flow
  host.
- Multi-tenancy, per-user persisted language preferences, organization-specific
  branding, automatic translation, or a visual theme editor.
- OIDC, SAML, LDAP, MFA, SCIM, data-scope authorization, or changes to the
  existing Problem Details contract.
- Completing the separately paused public-documentation internationalization
  migration.

## Error Handling

Module metadata is validated before user interaction. Duplicate or malformed
identifiers, invalid route targets, missing mandatory translations in the
default locale, and unavailable host layout configuration fail application
startup with an actionable configuration error.

At runtime, a user without a required permission neither sees the page in the
shell or workplace nor gains access through direct navigation. Existing Flow
access-denied and unexpected-error handling remains responsible for direct
navigation failures and operational exceptions. Missing non-default locale
translations fall back deterministically to `zh-CN` and produce a warning for
the application operator.

## Verification

1. Unit tests cover descriptor validation, stable ordering, collision reporting,
   catalog aggregation, translation fallback, and token documentation examples.
2. A Spring Boot integration test loads the independent orders artifact from an
   external package, proves its route is registered with the host layout, and
   proves its view receives required Spring dependencies.
3. Browser E2E proves permission-filtered navigation, direct route access,
   locale switching of shell and orders text, and light/dark rendering of the
   sample module without layout or text overlap.
4. Architecture tests prove `admin-flow` and the module descriptor remain
   independent of Spring and the reference application, while Spring-specific
   discovery remains below `admin-spring`.
5. Normal and production Maven verification, Compose validation, and the
   existing test suite remain green.

## Adoption Outcome

After this phase, a Java team can package a business module as a Maven
dependency, add it to a Spring Boot host, and obtain permission-aware,
internationalized navigation and Flow pages that match the host's theme. The
team follows one documented public contract instead of copying and modifying
the reference application's internal layout and configuration code.
