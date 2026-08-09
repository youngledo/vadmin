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

## Forward Roadmap

### Phase 3: Extensibility And Delivery Maturity

**Goal:** Make the starter easier to adapt, package, operate, and release
without adding a second runtime.

**Likely outcomes:** documented extension points for navigation and page
patterns; release/versioning policy; dependency and compatibility checks;
improved local demo-data/bootstrap experience; structured operational guidance;
CI delivery hardening.

**Non-goals:** non-Spring runtime, multi-tenancy, external identity provider,
new business vertical, dashboard analytics.

**Entry criteria:** Phase 2 verification remains green and the public extension
surface is reviewed for real consumer use.

**Exit criteria:** a new Java team can adapt navigation, theme, and a sample
module from documented extension points; release verification is repeatable.

### Phase 4: Identity And Enterprise Integration Readiness

**Goal:** Establish carefully scoped seams for enterprise identity and account
administration while retaining local authentication as a supported baseline.

**Likely outcomes:** authentication-provider abstraction review; account
administration improvements; migration-safe identity linkage; OIDC discovery
and configuration design if it remains justified by real adopters.

**Non-goals:** unconditional OIDC/SAML/LDAP implementation, MFA, SCIM,
organization hierarchy, data-scope authorization.

**Entry criteria:** Phase 3 extension and release contracts are stable; target
identity providers and security requirements are defined.

**Exit criteria:** an approved identity integration specification and a tested,
backward-compatible implementation or explicit decision to defer.

### Phase 5: Ecosystem Adaptation And Long-Term Compatibility

**Goal:** Evaluate additional runtime or UI-adjacent adapters only after the
Spring Boot baseline has stabilized in real use.

**Likely outcomes:** compatibility matrix; adapter feasibility spike; a
documented decision on a second runtime such as Helidon or Quarkus; framework
upgrade policy refinement.

**Non-goals:** speculative ports, duplicated reference applications, or changes
that weaken the Java-only contracts/platform/Flow boundaries.

**Entry criteria:** stable Spring Boot release baseline, Phase 4 decision, and
concrete adopter requirements.

**Exit criteria:** an approved adapter roadmap with an explicit scope, or a
documented decision to retain Spring Boot only.

## Governance

- Start only one forward phase at a time.
- Create and approve a phase specification before its implementation plan.
- Validate each implementation with normal and production builds, architecture
  checks, browser tests where relevant, and a final independent review.
- Reassess Phase 4 and Phase 5 when Phase 3 completes; their goals remain
  directional until then.
