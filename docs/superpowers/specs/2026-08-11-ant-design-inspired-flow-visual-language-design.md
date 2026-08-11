# Ant Design-Inspired Flow Visual Language

## Status

Approved design. This specification defines the first Flow-native visual
language profile. It precedes implementation planning and does not introduce
Ant Design, React, Vue, Hilla, or other frontend runtime dependencies.

## Goal

Offer an Ant Design-inspired visual and interaction language for the existing
Vaadin Flow administration baseline, while retaining the Vaadin baseline as a
parallel host choice. The same Java views, page patterns, routes, permissions,
module metadata, and translations must work unchanged under either profile.

The profile is inspired by Ant Design's operational hierarchy, density, and
interaction clarity. It is not a pixel-for-pixel clone or a replacement
component library.

## Product Model

Visual language, color mode, and information density are independent axes.

| Axis | Initial values | Owner |
| --- | --- | --- |
| Visual language | `vaadin`, `ant` | Host application configuration |
| Color mode | `light`, `dark`, later `system` | Current UI/session preference |
| Density | `comfortable`, `compact` | Host application configuration |

The first release exposes visual language and density as host choices, not as
ordinary end-user preferences. A product may later provide a user selector
without changing the CSS contract, but that is deliberately outside this
scope. The reference application continues to provide its existing light/dark
appearance control.

## Scope

The `ant` profile covers the administration workflows already provided by the
reference application and external modules:

- Application shell: top bar, navigation drawer, current location, utility
  controls, user menu, and narrow-screen drawer behavior.
- Page patterns: `AdminPageFrame`, page headers, toolbars, data workspaces,
  tables, pagination, forms, detail dialogs, editor dialogs, and confirmation
  dialogs.
- Operational feedback: loading, empty, error, success, disabled, selection,
  danger confirmation, and keyboard focus states.
- The commonly used Vaadin controls within those patterns: buttons, fields,
  selects, date controls, tags, menus, and `Grid`.

The visual direction uses clear surface hierarchy, an 8px-based spacing
rhythm, compact but readable operations, restrained radii and shadows,
well-defined primary and secondary actions, and dense, scannable tables.

## Non-Goals

- Embedding or adapting Ant Design React, its CSS distribution, or its icons.
- A broad reset or a wrapper around every Vaadin component.
- Covering charts, dashboard-specific graphics, uncommon Vaadin controls, or
  a general-purpose form-layout engine.
- Arbitrary color editors, customer branding tooling, or end-user profile
  switching.
- Naive UI- or Element UI-inspired profiles. Those remain candidates only
  after a concrete adopter need.
- Changes to the existing security, RBAC, module, i18n, routing, persistence,
  audit, or Problem Details contracts.

## Architecture

### Theme Ownership

There remains one host-owned Vaadin theme, currently `admin-theme`. It owns
global shell composition and styling. A profile is selected by an attribute on
the current UI root element:

```text
data-admin-visual-language="vaadin" | "ant"
data-admin-density="comfortable" | "compact"
theme="dark"                         (existing Vaadin color mode)
```

The host resolves valid visual-language and density values from its
configuration and applies them when a UI is initialized. Invalid or missing
values resolve to `vaadin` and `comfortable`, respectively. Values are
explicitly whitelisted; they are never interpolated into selectors or copied
from an untrusted request parameter.

Color mode continues to use Vaadin's existing `theme` attribute and session
preference. Applying a profile or density must not navigate, recreate a
business view, change the authenticated principal, or alter permissions.

### Module Boundaries

`admin-flow` remains Spring-free and continues to own only reusable Flow page
patterns and their stable semantic CSS classes. It must not depend on a
profile enum, a Spring configuration property, or the reference application's
theme selectors.

`admin-spring-flow` remains responsible for Spring assembly, routing, i18n,
and session integration. It does not select a visual language or own visual
styling.

The reference application is the first host implementation. Future hosts may
use the same root-attribute convention and semantic token contract while
choosing their own configuration mechanism. Business modules use public
`admin-*` semantic tokens and Flow patterns only. They neither select a
profile nor import files from `admin-theme`.

## Token Contract And Cascade

The existing `admin-*` variables remain the public semantic design-token
contract. Their meaning is stable across profiles: canvas, surfaces, borders,
text hierarchy, accent, state colors, focus, spacing, typography, radii,
elevation, and controls.

Styles are organized in this order:

1. The base semantic layer defines safe Vaadin-baseline values and maps them
   to the relevant Lumo variables.
2. A visual-language profile layer selected by
   `data-admin-visual-language` assigns its token values and supplies only
   necessary, scoped Vaadin component-part rules.
3. A dark-mode layer adjusts colors and elevation for the active profile.
4. A density layer adjusts spacing, control heights, table row geometry, and
   related layout rhythm without changing color, typography family, or the
   active profile.

The `ant` profile may add semantic variables only for a repeated role that
cannot be represented by the existing contract. Literal colors, local spacing
scales, global wildcard selectors, and profile-specific module CSS are not
permitted. Existing Lumo mappings must keep custom and external module
controls coherent under both profiles.

## Interaction And Accessibility Rules

The profile changes visual expression, not application behavior. Permission
filtering, authorized action checks, navigation state, dialog semantics,
validation, feedback messages, localization, and keyboard behavior remain
identical across profiles.

Each profile and mode combination must preserve:

- Visible keyboard focus with sufficient contrast.
- Accessible names and selected-state semantics for utility controls and
  navigation.
- Stable control and workspace geometry during loading and feedback states.
- Clear primary, secondary, disabled, selected, destructive, empty, and error
  states.
- A usable narrow-screen drawer and no overlap or clipped controls at the
  supported narrow viewport.

## Reference Application Behavior

The reference application demonstrates both profiles over the same built-in
and independently packaged Orders module. Its appearance menu continues to
control only light/dark mode. The default visual language and density are
configured by the host. Documentation will show adopters where those defaults
are supplied and how a host can make a different product decision later.

No page, route, permission, translation key, module contribution, or Java
view may be duplicated solely for the `ant` profile.

## Verification

Implementation must add focused tests for:

- Host resolution of defaults and rejection/fallback of invalid visual
  language and density values.
- Correct root attributes for every valid host selection, without changing
  the existing dark-mode behavior.
- The semantic CSS contract and profile-specific token application where a
  targeted CSS assertion is practical.
- Browser coverage of Users, Customers, Orders, Roles, and Audit under the
  representative combinations below.

| Combination | Required coverage |
| --- | --- |
| `vaadin` + light + comfortable | Existing baseline regression |
| `ant` + light + comfortable | Shell, page patterns, table, dialog, feedback |
| `ant` + dark + comfortable | Contrast, surface hierarchy, focus, feedback |
| `ant` + light + compact | Table and toolbar density without clipped controls |
| `ant` + dark + compact | Narrow-screen shell and a representative mutation flow |

The browser suite must cover desktop and narrow-screen layouts, accessible
focus, locale refresh, permission-filtered navigation, empty/error states,
and a representative dialog-based mutation. Production verification remains
required after ordinary reactor tests pass.

## Documentation Deliverables

- Add a concise host configuration guide for visual language and density.
- Update theme documentation with the independent-axis model and public token
  rules.
- Update the extension guide so module authors know to rely on semantic
  tokens and page patterns instead of profile internals.
- State clearly that this profile is Flow-native and has no React/Vue runtime
  dependency.

## Completion Criteria

The work is complete when the reference application can select `vaadin` or
`ant` as a host configuration, both retain the same functional behavior, and
the `ant` profile meets the defined shell, page-pattern, accessibility,
responsive, and verification requirements. The result must retain Vaadin as
the component foundation rather than becoming a parallel frontend framework.
