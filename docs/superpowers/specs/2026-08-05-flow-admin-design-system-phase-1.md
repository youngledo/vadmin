# Flow Admin Design System - Phase 1

**Status:** Proposed for review
**Date:** 2026-08-05

## Goal

Evolve Vaadin Admin Starter from a functionally complete reference application
into a reusable, professional Flow admin design system. Phase 1 establishes a
coherent application shell and the common page patterns needed by operational
Java applications; it does not attempt to recreate Ant Design Pro, React, or
the Ant Design component API.

## Product Positioning

The product remains a Java-first Vaadin Flow starter. Its value is a stable
combination of:

1. an application shell and theme that make every view feel like one product;
2. reusable Java-side page patterns for recurring administration workflows;
3. a reference application that proves those patterns against authorization,
   audit, forms, tables, and real persistence.

`admin-flow` owns reusable Flow UI infrastructure. `admin-reference-app` owns
the example users, roles, permissions, audit, and customer screens. The design
system must not require Spring types, JavaScript application code, Hilla, React,
or TypeScript authoring from consumers.

## Inspiration And Boundaries

Ant Design Pro is useful as a product model: a shared layout, design language,
reusable page templates, common state pages, and a runnable application that
demonstrates each template. This phase adopts that layering, not its React/Umi
implementation or visual identity.

The system must remain distinct in the following ways:

- Use Vaadin Flow components and Java composition, not React components or
  client-side state patterns.
- Expose patterns as focused Java APIs and documented composition rules, not a
  large speculative component catalog.
- Keep authorization authoritative in platform use cases; UI visibility and
  disabled states remain experience controls only.
- Treat the reference application as a verified pattern gallery, not a set of
  decorative screens.

## Phase 1 Scope

### 1. Theme Foundation

Provide a named Vaadin theme with semantic tokens for color, typography,
spacing, elevation, borders, focus, and status. Tokens must distinguish at
least the following meanings rather than encode page-specific colors:

- application canvas, surface, raised surface, and border;
- primary action, neutral action, destructive action, and disabled action;
- success, warning, error, and information feedback;
- primary, secondary, and muted text;
- compact and standard density for repeated administration work.

Phase 1 ships polished light and dark schemes from the same semantic token
system. The current-user menu provides a session-scoped mode control; durable
per-user preference storage is deferred until the account-preferences feature
exists. Both schemes must be designed and tested before exposing the control.

### 2. Application Shell

Replace the minimal `MainLayout` presentation with a professional operational
shell:

- persistent, collapsible navigation on wide displays and a drawer on narrow
  displays;
- a product mark and product name that remain visible in the first viewport;
- navigation grouped by capability while retaining the permission-filtered
  `PageRegistry` projection;
- a top bar with drawer control, current location, and current-user menu;
- a predictable content canvas with a constrained maximum width for forms and
  a full-width option for data-heavy pages;
- clear active-route and keyboard-focus states.

The shell must not add a global search, notification center, or command palette
without a real backend capability behind it.

### 3. Reusable Page Patterns

`admin-flow` provides a small, composable pattern layer:

| Pattern | Responsibility |
|---|---|
| Page header | Title, optional description, breadcrumbs/location, and page-level actions. |
| Query toolbar | Compact filter fields, clear/reset affordance, primary creation action, and an action slot. |
| Data workspace | Stable Grid framing, loading/empty/failure presentation, selected-item count, and bulk-action region. |
| Editor surface | Responsive `FormLayout`, validation summary, save/cancel action placement, and mutation feedback. |
| Detail surface | Readable field groups, metadata, and contextual actions without treating every read view as a table. |
| Confirmation flow | Explicit destructive-action confirmation with a concise consequence and busy state. |
| State views | Empty, access-denied, and unexpected-failure surfaces consistent with the shell. |

These patterns may compose standard Vaadin components. They must not wrap every
Vaadin control or hide ordinary component APIs behind a proprietary abstraction.

### 4. Reference Application Modernization

Use existing, permission-protected business screens as acceptance examples:

- **Workplace:** replace the title-only home view with a restrained operational
  overview, quick links to permitted work, and useful system context. It must
  not invent analytics data.
- **Users:** page header, search/action toolbar, selectable table, bulk actions,
  and a consistent create editor.
- **Roles:** page header, assignment controls, and permission-grant feedback.
- **Permissions and audit:** dense, scan-friendly read-only data workspaces.
- **Customers:** reusable filtering, table actions, responsive editor, deletion
  confirmation, and attachment handling with consistent surfaces.

The pages continue to call existing application services and retain all route
guards, permission checks, audit behavior, and Problem Details semantics.

## Package And Dependency Boundaries

Phase 1 adds reusable classes and theme assets under `admin-flow` only when
they are independent of the reference domain. Business-specific screen code,
demo data, and routes remain in `admin-reference-app`.

```text
admin-reference-app views
  -> admin-flow shell, theme, page patterns, error presentation
  -> admin-platform use cases and contracts

admin-flow
  -> Vaadin Flow, admin-contracts, admin-platform
  -/-> Spring, JPA, Flyway, reference-app classes
```

The existing `admin-flow` authorization/navigation code is extended rather
than bypassed. A Flow view still never accesses JPA repositories directly.

## Interaction And Accessibility Rules

- Use icons from Vaadin's established icon set for compact familiar actions;
  icon-only controls require accessible labels and tooltips.
- Make destructive actions visually distinct and require confirmation.
- Keep table actions stable in width; avoid layout shifts when rows are hovered
  or selected.
- Preserve keyboard navigation, visible focus, semantic labels, and readable
  color contrast.
- On small screens, collapse the shell navigation and stack form actions;
  data grids retain horizontal access rather than silently discarding columns.
- Text, errors, and empty states must describe the current operational state,
  not the implementation or framework.

## Visual Direction

The visual style is quiet and information-dense: neutral surfaces, deliberate
contrast, one restrained accent color, clear typography, and compact but not
cramped spacing. It should support repeated administrative work and scanning,
not resemble a marketing landing page. Sections are layout bands rather than
nested decorative cards; cards are reserved for repeated work items and truly
framed tools.

## Delivery Sequence

1. Create theme tokens and shell primitives, then migrate `MainLayout`.
2. Add page header, toolbar, workspace, editor, confirmation, and state-view
   patterns with focused component tests.
3. Modernize workplace, users, roles, permissions, audit, and customers in
   dependency order, preserving behavior before expanding presentation.
4. Add responsive browser E2E coverage, visual regression checks where the
   toolchain supports them, extension documentation, and a pattern gallery.

Each increment must leave the reference application buildable and usable.

## Non-Goals

Phase 1 explicitly excludes:

- a React/Hilla implementation or frontend-developer workflow;
- copying Ant Design Pro assets, CSS, component APIs, or brand identity;
- charts, monitoring dashboards, generated mock data, or invented KPIs;
- a general low-code page builder or schema-driven UI engine;
- global search, notifications, or command palettes without backend behavior;
- arbitrary branding configuration beyond the documented token customization
  path;
- changing Spring adapter structure, platform contracts, authorization rules,
  database schema, or audit semantics.

## Acceptance Criteria

1. The reference application has a named light/dark theme and a consistent
   shell across all authenticated views.
2. Existing navigation remains permission-filtered and direct-route protection
   remains enforced.
3. Every modernized business view uses the common page header and the relevant
   toolbar, workspace, editor, detail, confirmation, or state pattern instead
   of ad hoc layout composition.
4. Users, roles, permissions, audit, and customers retain their current
   functional, authorization, and audit behavior.
5. Desktop and narrow-browser E2E tests cover shell navigation, one list/edit
   workflow, and feedback for empty, denied, and failure states.
6. `admin-flow` remains free of Spring, JPA, Flyway, and reference-application
   dependencies.
7. Documentation shows Java-only examples for adding a page and selecting a
   pattern.

## Architecture Guidance Result

- **Phase:** Architecture Guidance
- **Status:** completed
- **Inputs:** existing modular-monolith constraints, Flow-only product
  direction, Ant Design Pro product patterns, and confirmed Phase 1 scope.
- **Summary:** add a reusable Flow presentation layer in `admin-flow` and use
  the reference application as a behaviorally real pattern gallery.
- **Assumptions:** existing view behavior remains the source of truth; Phase 1
  will not add chart data or a second UI runtime.
- **Decisions:** page patterns are composable Flow infrastructure, the shell is
  framework-neutral within the Flow boundary, and the reference app supplies
  concrete usage examples.
- **Constraints:** no Spring types in `admin-flow`; existing platform-level
  authorization remains authoritative; no speculative component wrappers.
- **Evidence:** existing requirements and architecture documents, the current
  Flow views, and Ant Design Pro's official template structure.
- **Open Questions:** exact brand name, final accent color, and light/dark
  visual direction are resolved through visual design before implementation.
- **Artifacts:** this Phase 1 design specification.
- **Recommended Next Step:** approve the specification, then create a staged
  implementation plan with visual mockups for shell and page-pattern decisions.
