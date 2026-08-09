# Flow Admin Design Refresh

## Status

Completed on 2026-08-10. This design follows the completed extensibility
foundation and applies only to the Flow administration experience.

## Goal

Make the reference application feel like a coherent, professional internal
workbench while retaining Vaadin Flow's native component language and the
project's Java-first operating model. The goal is not to reproduce Ant Design
Pro or introduce a custom frontend framework.

## Product Principles

- Prefer stable information hierarchy over decorative styling.
- Keep routine administration work dense, calm, and easy to scan.
- Reserve visual emphasis for location, primary commands, selection, errors,
  and consequential actions.
- Make the same task look and behave the same across built-in and external
  modules.
- Preserve keyboard access, locale refresh, dark mode, narrow-screen access,
  and permission-filtered navigation.

## Scope

### Application Shell

The shell becomes a compact operational frame:

- The top bar contains navigation access, product identity, current location,
  and a right-aligned utility area.
- Language and light/dark mode are low-frequency interface preferences. They
  use icon-triggered menus in the utility area, with accessible labels and
  visible current state; they are not full-width selects in the primary bar.
- The user menu contains account-specific context only. Interface preferences
  do not compete with the account control.
- The drawer presents permission-filtered groups once, uses consistent active
  navigation treatment, and has a predictable narrow-screen open/close state.
- The content canvas has a stable maximum readable working width, responsive
  padding, and a clear relationship to the shell rather than a card around the
  whole page.

### Page Composition

Every administration page follows a shared vertical rhythm:

1. Page header: title, concise purpose, optional context, and page-level
   commands.
2. Work controls: search, filters, reset, secondary actions, and the single
   primary command when authorized.
3. Data workspace: selection context, grid or content surface, pagination,
   loading, empty, failure, and permission-safe actions.
4. Local overlays: detail, edit, confirmation, and operation feedback use the
   existing Flow patterns and do not create a second interaction system.

`PageHeader`, `PageToolbar`, `DataWorkspace`, `DetailDialog`, `EditorDialog`,
`ConfirmationDialog`, `EmptyState`, and `OperationFeedback` remain the
reusable Java API. The refresh may add narrowly scoped options or component
classes, but must not turn `admin-flow` into a wrapper for every Vaadin
component.

### Visual System

The existing semantic `admin-*` token contract remains public. The refresh
extends it only when a repeated semantic role is currently represented by a
literal or inconsistent rule. Tokens cover canvas/surfaces, text hierarchy,
borders, accent and state colors, focus, density/spacing, typography, radius,
and elevation in light and dark modes.

Rules:

- Vaadin and Lumo remain the component foundation; no frontend framework,
  custom icon set, gradients, or product-marketing treatment is introduced.
- Layout uses bands and unframed content areas. Cards are limited to repeated
  work items, dialogs, and genuinely framed tools.
- Type hierarchy is restrained: compact headings in work panels, no negative
  tracking, and no viewport-scaled fonts.
- Page patterns consume semantic tokens; module CSS stays component-scoped.

### Interaction States

The same visual and behavioral rules apply to built-in and external modules:

- Loading preserves layout dimensions and communicates progress without
  shifting controls.
- Empty states explain the current absence and expose only permitted recovery
  actions.
- Failures keep context and provide a local retry or safe recovery path where
  available; global Flow errors retain their current ownership.
- Selection and bulk operations make affected count, availability, and
  destructive confirmation unambiguous.
- Locale and theme changes refresh visible shell and page content in place.

## Delivery Sequence

### Stage 1: Shell and Token Refinement

Refine the top bar, utility controls, user menu boundary, navigation hierarchy,
canvas spacing, and any missing semantic tokens. Validate desktop and narrow
layouts in both themes.

### Stage 2: Shared Page Patterns

Refine header, toolbar, workspace, status, and overlay composition. Add only
reusable behavior needed by at least two pages.

### Stage 3: Reference-Page Adoption

Apply the shared patterns to Users, Roles, Customers, Audit, and Orders.
These pages are the visual regression set, not a source of page-specific
design systems.

### Stage 4: Acceptance and Documentation

Add browser coverage for layout geometry, keyboard-accessible utility controls,
locale/theme refresh, narrow screens, and operational states. Update English
theme and extension documentation only where public contracts change.

## Architecture Boundaries

- `admin-flow` remains Spring-free and owns reusable Flow page patterns.
- `admin-spring-flow` remains assembly, routing, i18n, and session integration;
  it does not own presentation styling.
- The reference application owns `@Theme`, shell composition, static
  production `@Uses` anchors, and product identity.
- Business modules consume patterns and tokens; they do not depend on the
  reference application or implement global themes.
- Existing authorization, auditing, module metadata, Problem Details, and
  persistence boundaries are unchanged.

## Acceptance Criteria

- The shell has a clearly separated navigation, location, utility, and account
  model at desktop and narrow widths.
- Common administration pages share recognizable header, controls, workspace,
  and state composition without duplicated page-local CSS.
- Language and theme preferences are reachable by accessible icon controls and
  update the current UI without navigation.
- Light and dark themes preserve readable contrast, stable geometry, and no
  incoherent overlap.
- Orders proves that an external module receives the same shell and pattern
  quality as built-in pages.
- Browser tests cover the refreshed shell and representative read-only and
  mutating workflows; production verification remains green.

## Implementation Outcome

- `MainLayout` now separates navigation, location, icon-triggered language and
  appearance utilities, and account context. Language and appearance menus
  expose the selected state and update the active UI in place.
- `admin-flow` owns `AdminPageFrame`, which composes the shared page header,
  optional controls, and workspace without importing Spring or host types.
  The host theme retains ownership of global shell and pattern styling.
- Built-in administration pages and the independently packaged orders module
  adopt the same page frame and host visual system.
- `./mvnw -B -ntp -pl :admin-reference-app -am verify` succeeded with Browser
  E2E 31/31, `ApplicationContextIT` 8/8, and `ReferenceLocaleIT` 1/1. The
  full production reactor verification, `./mvnw -B -ntp -Pproduction verify`,
  also succeeded.

## Non-Goals

- Reproducing Ant Design Pro's component library or visual identity.
- Adding dashboards, fabricated analytics, third-party charting, Hilla,
  TypeScript, React, or runtime themes.
- Redesigning RBAC, module assembly, database schema, or authentication.
- A broad public documentation translation pass.
