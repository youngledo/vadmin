# Product Roadmap Design

**Status:** Proposed  
**Date:** 2026-08-09

## Purpose

Provide a stable sequence of product outcomes after the completed Flow design
system phases, while keeping detailed implementation planning close to the
phase that will actually be built.

## Planning Model

The project uses two documentation levels:

1. This roadmap records phase goals, dependencies, non-goals, and entry/exit
   criteria for the foreseeable product horizon.
2. Each phase receives its own approved specification and implementation plan
   immediately before implementation. A future phase must not start from this
   roadmap alone.

Changes to a completed phase do not silently rewrite its historical
specification or plan. The roadmap is updated when priorities or dependencies
change.

## Completed Foundations

| Phase | Outcome |
| --- | --- |
| Foundation | Java 25, Spring Boot 4, Vaadin Flow 25, contracts/platform/adapters, RBAC, audit, Problem Details, Testcontainers, and reference application. |
| Flow Design System Phase 1 | Theme, shell, Java-only page patterns, responsive administration views, and automated browser coverage. |
| Flow Design System Phase 2 | Detail, confirmation, and operation-feedback patterns; completed administration workflows and permission-filtered workplace entries. |
| Phase 3: Extensibility And Delivery Maturity | Completed: Spring Flow module assembly, module i18n/theme-token contracts, module adoption coverage, and normal and production verification. The former fictional orders example is superseded by the default-starter direction below. |
| Phase 4: Identity And Enterprise Integration Readiness | Completed: opt-in, provider-neutral Spring OIDC discovery and authorization-code login; Spring-free external-identity mapper contract; explicit existing-local-account mapping; Keycloak test fixture; normal and production verification. |
| Ant Flow Profile Maturity (Phases A-C) | Completed: profile-neutral icons and an Ant-inspired shell; Flow-native control and overlay skins; dense data workspaces, server-side pagination, state feedback, and narrow-screen verification. The Vaadin visual profile remains a parallel supported baseline. |

## Forward Roadmap

### Phase 3: Extensibility And Delivery Maturity (Completed)

**Goal:** Make the starter easier to adapt, package, operate, and release
without adding a second runtime.

**Delivered outcomes:** documented extension points for independently packaged
navigation and page modules; `zh-CN` and `en-US` UI contracts; host-owned
light/dark theme tokens; Spring Flow module assembly; architecture and browser
adoption coverage; and normal/production verification. The delivery remains
Spring Boot only. The independently packaged orders example delivered during
this phase is deliberately retired by the default-starter phase: it proved the
module contract but is not product baseline.

**Non-goals:** non-Spring runtime, multi-tenancy, external identity provider,
new business vertical, dashboard analytics.

**Entry criteria:** Phase 2 verification remains green and the public extension
surface is reviewed for real consumer use.

**Exit criteria:** a new Java team can adapt navigation, UI language, theme,
and a sample module from documented extension points; release verification is
repeatable.

### Phase 4: Identity And Enterprise Integration Readiness (Completed)

**Goal:** Establish carefully scoped seams for enterprise identity and account
administration while retaining local authentication as a supported baseline.

**Delivered outcomes:** a Spring-free `ExternalIdentityMapper` contract; an
opt-in Spring Security OIDC authorization-code adapter through issuer
discovery; explicit mapping only to existing enabled local accounts; preserved
local-password login and local authorization semantics; and Keycloak-backed
integration coverage without a Keycloak runtime dependency. Any compliant
mainland-China, global, or self-hosted issuer follows the same standard OIDC
configuration path.

**Non-goals:** provider SDKs, automatic provisioning or deprovisioning,
group-to-role synchronization, SAML/LDAP implementation, MFA, SCIM,
organization hierarchy, tenant selection, and data-scope authorization.

**Entry criteria:** Phase 3 extension and release contracts are stable; target
identity providers and security requirements are defined.

**Exit criteria:** completed. Normal and production reactor verification passed
with OIDC disabled by default; Keycloak remains a test-only interoperability
fixture.

### Phase 5: Default Spring Admin Starter

**Goal:** Make the Spring Boot baseline directly consumable as a complete Flow
administration application rather than as reusable pieces that every adopter
must compose.

**Planned outcomes:** `admin-spring-starter` as the consumer-facing dependency;
a default shell, theme, home page, and system administration for users, roles,
permissions, and audit; a thin reference application that consumes that
starter; and explicit host override rules. Remove the fictional orders and
customer domains from the shipping baseline while retaining the `AdminModule`
contract for genuine consumer-owned business modules.

**Non-goals:** an additional runtime, Hilla/React/Vue, new fictional business
verticals, multi-tabs, low-code menu configuration, or arbitrary end-user
theme editors.

**Entry criteria:** completed Flow design-system, module assembly, and identity
integration foundations; a reviewed default-starter design specification.

**Exit criteria:** a new Spring Boot application obtains a coherent,
permission-filtered shell and baseline system administration by depending on
one starter artifact; its own `AdminModule` contributions appear with working
translations and authorization; normal, production, and browser verification
pass.

### Phase 6: Ecosystem Adaptation And Long-Term Compatibility

**Goal:** Evaluate additional runtime or UI-adjacent adapters only after the
Spring Boot baseline has stabilized in real use.

**Likely outcomes:** compatibility matrix; adapter feasibility spike; a
documented decision on a second runtime such as Helidon or Quarkus; framework
upgrade policy refinement; and an approved Flow visual-language adaptation
roadmap.

**Visual-language adaptation:** Keep Vaadin Flow and Java as the sole UI
programming model. The starter may provide Flow-native visual-language profiles
inspired by established administration design systems; it must not embed React
or Vue component libraries. Ant Design-inspired visual and interaction
patterns were the first candidate and are complete through shell, controls,
overlays, and operational data workspaces. Naive UI-inspired and Element
UI-inspired profiles remain later candidates, evaluated only when concrete
adopter needs justify them.

Visual language, color mode, and information density are independent choices:

| Dimension | Initial direction |
| --- | --- |
| Visual language | Ant Design-inspired Flow profile, with the Vaadin baseline retained as a host choice |
| Color mode | Light, dark, or system preference |
| Information density | Comfortable or compact |

Profiles must be implemented through the starter's semantic design tokens and
Flow page patterns, so business Java code, routes, permissions, modules, and
i18n remain unchanged when a host selects another profile. End-user arbitrary
color editors are outside the initial scope; host-owned brand customization is
an extension concern.

**Non-goals:** speculative ports, duplicated reference applications, embedding
Ant Design React, Naive UI, or Element UI as runtime dependencies, or changes
that weaken the Java-only contracts/platform/Flow boundaries.

**Entry criteria:** stable Spring Boot release baseline, completed default
starter phase, Phase 4 decision, and
concrete adopter requirements.

**Exit criteria:** an approved adapter roadmap with an explicit scope, or a
documented decision to retain Spring Boot only.

## Release Readiness

The next practical milestone is the `0.1.0` release-candidate preparation,
not a second runtime adapter. It records the tested compatibility baseline,
versioning policy, upgrade notes, release gates, and the distinction between
what is verified in this repository and what must be configured before public
artifact publication. See the [release guide](../../en/release-guide.md).

## Governance

- Start only one forward phase at a time.
- Create and approve a phase specification before its implementation plan.
- Validate each implementation with normal and production builds, architecture
  checks, browser tests where relevant, and a final independent review.
- Reassess Phase 4 and Phase 5 when Phase 3 completes; their goals remain
  directional until then.
