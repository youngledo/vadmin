# Flow Admin Design System Phase 2

**Status:** Proposed  
**Date:** 2026-08-06

## Goal

Complete the reusable interaction patterns needed for real administration work
in the existing Spring Boot and Vaadin Flow reference application. Phase 2
improves operational confidence and task completion without adding a second
runtime adapter or changing authorization boundaries.

## Scope

### Shared Interaction Patterns

`admin-flow` adds three small, composable Flow patterns:

| Pattern | Responsibility |
| --- | --- |
| Detail surface | Presents read-only entity fields, metadata, and contextual actions in a consistent dialog or side surface. |
| Confirmation dialog | Requires explicit confirmation for destructive or state-changing commands; exposes consequence text, cancel, confirm, busy, and failure states. |
| Operation feedback | Provides a consistent success notification and safe failure presentation without replacing Flow's global handling for authorization or unexpected failures. |

Patterns compose standard Flow components. They remain free of Spring, JPA,
Flyway, reference-application, and business-domain dependencies.

### Reference Application

- **Users:** support readable user details and explicit confirmation before
  enabling or disabling accounts. Existing command, authorization, audit, and
  validation behavior remains authoritative.
- **Roles:** show readable role/permission details and make grant feedback
  explicit. The existing grant use case remains unchanged.
- **Customers:** support detail inspection and use one consistent destructive
  confirmation before deletion. Attachment authorization, storage, download,
  and cleanup behavior remain unchanged.
- **Workplace:** replace plain permission-filtered text links with compact,
  permission-filtered operational entry items. Each item identifies its target
  and intent but does not invent KPIs, charts, counts, alerts, or search.

## Interaction Rules

1. A confirmation dialog names the consequence and never executes its command
   until the user explicitly confirms.
2. While a confirmed mutation is pending, its controls and escape/outside close
   behavior follow the existing busy-state protection.
3. `VALIDATION_FAILED` stays local to an editor or command surface. Authorization
   denial, resource-not-found, and unexpected failures continue to Flow's
   existing global error handling.
4. A success notification is emitted only after the existing application command
   returns successfully.
5. Details expose only data the existing view can already read; they never form
   a new authorization route or service query path.

## Deferred Work

- No persisted filter, column, density, or account-preference model.
- No dashboard KPI, chart, notification center, global search, or command palette.
- No multi-tenancy, OIDC/SAML/LDAP/MFA, data-scope authorization, or second
  runtime adapter.
- No changes to Spring Boot module boundaries, services, Problem Details, or
  permission catalog semantics.

## Acceptance Criteria

1. `admin-flow` supplies tested Java-only detail, confirmation, and feedback
   patterns.
2. Users, roles, and customers use the patterns while preserving all existing
   service calls, guards, permission checks, audit writes, and attachment paths.
3. The workplace shows only permission-filtered operational entry items and no
   fabricated operational data.
4. Browser E2E covers detail display, confirmation cancellation and execution,
   validation/error behavior, and permission-protected mutation paths.
5. Normal and production Maven verification, architecture tests, and Compose
   configuration validation succeed.
