# Ant Flow Profile Maturity

## Status

Approved implementation direction. This specification deepens the existing
Flow-native `ant` profile from an Ant-inspired token layer into a coherent,
high-use administration visual language. It does not add React, Vue, Hilla,
Ant Design runtime packages, or an alternative frontend programming model.

## Problem

The current profile correctly changes semantic colors, spacing, density, and a
small set of Vaadin component parts. It still exposes Vaadin's visual identity
in high-salience places: navigation and action icons, the shell, controls,
menus, dialogs, and table mechanics. The result reads as a Vaadin application
with an Ant-like palette rather than a mature Java-native administration
baseline.

## Goal

Make the `ant` profile visibly distinct and professionally coherent across the
reference application's common operational workflows, while the `vaadin`
profile remains a parallel baseline. Business Java code, routes, permissions,
module metadata, and translations must remain profile-neutral.

## Product Boundary

The target is Ant Design's information hierarchy and operational feel, not a
pixel-for-pixel copy or a Java port of Ant Design.

The work covers the surfaces users see repeatedly:

- A compact product shell: brand mark, navigation, current location, utility
  controls, account menu, and narrow-screen drawer.
- A consistent neutral line-icon language for navigation and common actions.
- Buttons, fields, selects, date controls, menus, dialogs, notifications,
  grid headers/cells, pagination, loading, empty, validation, and destructive
  states.
- The existing page frame, headers, toolbars, data workspaces, detail/editor
  dialogs, and confirmation flows.

It deliberately excludes uncommon Vaadin controls, charts, dashboard artwork,
arbitrary branding tools, and per-user visual-language switching.

## Architecture

### Profiles And Ownership

The existing host-owned `admin-theme` remains the only theme. The UI root
attributes continue to choose visual language and density; the session-owned
`theme="dark"` attribute continues to choose color mode.

```text
data-admin-visual-language="vaadin" | "ant"
data-admin-density="comfortable" | "compact"
theme="dark"                         (current UI session)
```

The `vaadin` branch keeps Vaadin's existing icon rendering and component
appearance. The `ant` branch applies its rules only through root-attribute
selectors and documented semantic tokens. A profile must never alter routing,
authorization, data mutation, dialog semantics, or localization.

### Icon Boundary

Introduce a profile-neutral Flow `AdminIcon` facade and a closed
`AdminIconName` vocabulary in `admin-flow`. It renders both a Vaadin fallback
glyph and a semantic CSS hook. Under the `vaadin` profile the fallback stays
visible; under the `ant` profile the theme displays an owned, neutral,
Lucide-derived SVG mask for the same semantic name.

The project vendors only the small required SVG subset under the host theme,
with its license notice. It does not import `@ant-design/icons`, React, or a
JavaScript icon runtime. `AdminIconCatalog` remains the validation point for
module navigation keys. Reference and Orders views replace direct
`VaadinIcon` action use with the facade, so profile selection is never exposed
to business code.

### Component Skin Boundary

The `ant` profile extends CSS custom properties with only repeated semantic
roles: control border/fill, hover/focus/disabled treatment, overlay surface,
table header, and navigation selection. Scoped `::part(...)` rules skin the
components actually used by page patterns. No global resets, wildcard
selectors, synthetic component library, or profile-specific Java layout is
introduced.

Each rule must use public `admin-*` semantic tokens. A dark counterpart is
provided when a component uses a light-only surface or shadow. Compact density
changes geometry only; it cannot change the selected profile or color mode.

## Delivery Phases

### Phase A: Icon Language And Shell

Provide the profile-neutral icon facade, vendor the required neutral SVG mask
assets, and migrate shell/navigation/reference action icons. Refine the Ant
shell's navigation hierarchy, utility targets, selected state, and narrow
layout without changing its DOM-level accessibility semantics.

**Exit condition:** Users can recognize the Ant profile immediately from the
shell and repeated action icons, while the Vaadin profile renders its original
glyph family.

### Phase B: High-Use Control And Overlay Skins

Skin buttons, text/select/date fields, menu controls, dialogs, notifications,
and validation/disabled/focus states for all Ant light/dark and
comfortable/compact combinations.

**Exit condition:** Forms and mutation flows retain Vaadin behavior but no
longer look like unmodified Lumo controls under the Ant profile.

### Phase C: Data Workspace And Operational States

Complete Grid, pagination, toolbar, loading, empty, failure, row action, and
confirmation presentation. Validate representative Users, Customers, Orders,
Roles, and Audit workflows at desktop and narrow viewports.

**Exit condition:** Dense operational pages have a stable, coherent visual
language rather than isolated component styling.

## Accessibility And Compatibility

- Icon-only controls retain accessible names supplied by their owning button
  or menu item; decorative icon nodes are hidden from assistive technology.
- Every profile/mode retains a visible keyboard focus indicator and WCAG-aware
  text/state contrast.
- Button and field changes preserve native focus, validation, disabled, and
  pointer/keyboard behavior.
- The suite verifies both development and production builds, because CSS
  minification may normalize otherwise equivalent computed token strings.
- `admin-flow` remains Spring-free. `admin-spring-flow` does not select or
  style a profile. Host theme assets and CSS stay in the reference host.

## Verification

Each phase adds focused unit/CSS-contract tests and browser evidence. Browser
coverage uses the existing Ant comfortable and compact suites plus the baseline
suite to prove that the same views and permissions behave identically. The
acceptance checks include icon switching by profile, keyboard focus, dialogs,
validation, notifications, Grid geometry, narrow-screen shell behavior, and
light/dark contrast. Every phase ends with ordinary and production Maven
verification and a visual screenshot review at desktop and narrow viewports.

## Completion Criteria

The completed `ant` profile is recognizably a polished, Ant-inspired Java
administration experience in its shell, icons, forms, data workspaces, and
mutation flows. It remains a Vaadin Flow application with a stable Java-only
consumer API and retains the original `vaadin` profile as a supported option.
