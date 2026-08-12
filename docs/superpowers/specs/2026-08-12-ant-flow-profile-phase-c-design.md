# Ant Flow Profile Phase C: Data Workspace And Operational States

## Status

Approved implementation direction. This specification completes the data-heavy
portion of the existing Flow-native `ant` visual-language profile. It follows
the completed shell/icon Phase A and control/overlay Phase B work. It does not
introduce a frontend runtime, an alternative component library, or a new
application data model.

## Problem

The reference application already has reusable Java patterns for workspaces,
server-side grids, page toolbars, empty states, operation feedback, and
confirmation dialogs. Those patterns correctly preserve permissions and Flow
behavior, but their dense data presentation is only partly expressed by the
Ant profile. A user can still encounter unrelated table, paging, selection,
empty, failure, and row-action treatments on otherwise related administration
pages.

## Goal

Give all existing data workspaces a coherent, compact, accessible Ant-inspired
operational presentation while preserving the supported `vaadin` baseline and
the Java-only programming model.

The result must make a dense worklist read as one intentional system: query
controls lead into a clearly bounded workspace; headers, rows, selection,
empty/loading/failure states, pagination, row actions, confirmation, and
feedback retain consistent hierarchy at desktop and narrow viewports.

## Product Boundary

### In Scope

- Ant-scoped presentation of the existing `Grid`, virtualized grid rows,
  sortable headers, selection controls, and an explicit server-side paging
  control.
- Ant-scoped presentation of the existing `PageToolbar`, `DataWorkspace`,
  `EmptyState`, `ConfirmationDialog`, and `OperationFeedback` flows.
- Stable, profile-neutral state hooks on reusable Flow patterns when a state is
  not otherwise addressable by host CSS.
- Focused CSS/token contract tests and browser evidence over Users, Customers,
  independently packaged Orders, Roles, and Audit.
- Desktop light and dark coverage plus compact, narrow-screen geometry and an
  explicit state-transition test for busy, empty, and failure workspaces.
- English and Simplified Chinese documentation for host and module ownership.

### Explicitly Out Of Scope

- A new Flow grid abstraction, client-side grid implementation, dashboard,
  chart, bulk-edit workflow, filter builder, saved views, or infinite-scroll
  data model.
- Changing business queries, permissions, routes, localization keys, command
  behavior, audit behavior, or error mapping solely for a visual profile.
- Styling undocumented Vaadin shadow-DOM internals, global CSS resets, or
  profile-specific CSS in a business module.
- Supporting arbitrary third-party Vaadin controls, a second runtime, or a
  Naive UI/Element UI profile.

## Architecture And Boundaries

### Existing Pattern Ownership

`admin-flow` remains Spring-free and continues to own behavioral page patterns:

| Pattern | Phase C responsibility |
| --- | --- |
| `DataWorkspace<T>` | Expose the current presentation state through a stable semantic hook, retain selection and state behavior, and host an optional pager footer. |
| `PagedGrid<T>` | Continue server-side page loading, sorting, filtering, refresh, and empty text; expose a profile-neutral pager for the current `PagedQuery`/`PagedResult` contract. |
| `PageToolbar` | Continue filter/action slots and busy action disabling without profile awareness. |
| `EmptyState` | Retain meaningful title, description, optional next action, and status semantics. |
| `ConfirmationDialog` / `OperationFeedback` | Retain confirmation, busy, failure, and notification behavior; host styling remains external. |

The profile-neutral workspace hook is a `data-admin-workspace-state` attribute
whose values exactly match `DataWorkspace.State`: `ready`, `busy`, `empty`, or
`failure`. `DataWorkspace` owns setting it at construction and on every state
transition. It does not expose a visual-language enum or select CSS profiles.
Consumers can use the attribute as an accessibility-neutral semantic hook, but
must not use it to make authorization or business decisions.

The existing virtual Grid binding loads server-side pages but does not expose a
user-operated page navigation control. Phase C adds a small Flow-native
`PaginationBar` pattern rather than treating that implementation detail as a
finished pagination experience. `PagedGrid` owns its current zero-based page,
total result count, and refresh/sort/filter transitions; it composes a pager
with previous, next, current-page summary, and total-result summary. It keeps
using the existing `PagedQuery` and `PagedResult` contracts, so application
query services and external modules do not change.

`DataWorkspace` provides a profile-neutral footer slot for the pager. A view
explicitly installs `pages.getPaginationBar()` after it creates the workspace.
The footer is hidden for busy, empty, and failure states; `PaginationBar`
itself hides when the result has one page. This keeps a pagination control in
the same bounded work surface as its Grid without making `DataWorkspace`
depend on query services, total counts, or `PagedGrid`.

`ConfirmationDialog` marks its consequence paragraph with the stable semantic
class `admin-confirmation-consequence`. The host may render this destructive
context with workspace tokens, but it must not identify confirmations through
undocumented overlay structure or change the dialog's confirm/cancel behavior.

### Host Theme Ownership

Only the reference host's `admin-theme/styles.css` selects the Ant profile. It
adds repeated semantic roles to the public `--admin-*` contract:

| Token | Meaning |
| --- | --- |
| `--admin-workspace-header-fill` | Grid header and related operational-header surface. |
| `--admin-workspace-header-text` | Secondary, high-legibility header text. |
| `--admin-workspace-row-hover` | Hover surface for an enabled data row. |
| `--admin-workspace-row-selected` | Selected-row and selection-summary surface. |
| `--admin-workspace-divider` | Low-emphasis separator between operational regions. |
| `--admin-workspace-status-fill` | Busy, empty, or failure state backdrop inside a workspace. |
| `--admin-workspace-danger-fill` | Consequence surface for destructive confirmation. |

Every token has Ant light and dark values. Comfortable and compact density only
change the existing spacing, control-height, and grid-padding geometry. They
must not choose a profile, a color mode, or different business behavior. The
shared Flow message bundles add pager labels for previous, next, current page,
and total result count; those labels follow the current UI locale.

Ant selectors are rooted at `[data-admin-visual-language="ant"]` and are
limited to Vaadin public host properties and exported `::part(...)` names. The
`vaadin` profile retains the existing Lumo presentation. A rule must use a
semantic token, not a literal local color or spacing value.

### State Presentation

The workspace always remains a single bounded surface. State transitions do
not reflow surrounding page structure:

1. **Ready:** grid is visible; selected rows and bulk actions are distinguishable.
2. **Busy:** the grid remains geometrically stable, commands and pager are
   disabled by their existing patterns, and the polite status message becomes
   visible.
3. **Empty:** the grid is hidden and `EmptyState` is the primary content, with
   a bounded, centered status region rather than an unstyled blank panel.
4. **Failure:** the grid is hidden and the failure `EmptyState` is visible with
   a clear error hierarchy; no exception detail is added to the theme.

Row actions continue to use their owning view's accessible name and tooltip.
The Ant profile may align compact action controls and hover affordances, but it
must not turn an icon-only action into an unlabeled control. Selection remains
controlled by `Grid`, and confirm/cancel semantics remain controlled by
`ConfirmationDialog`.

## Representative Acceptance Workflows

| Page | Required evidence |
| --- | --- |
| Users | Filter/toolbar geometry, selected-row and bulk-action treatment, and disable confirmation retain accessibility and do not overflow at a narrow viewport. |
| Customers | Search and primary action lead into a readable grid; delete confirmation and success notification retain Phase B behavior within the finished workspace language. |
| Orders | The independently packaged, read-only module renders the same Grid header, rows, explicit pager, and row-detail action without importing host CSS. |
| Roles | Toolbar action and permission-grant dialog retain disabled/busy semantics and visible boundaries. |
| Audit | Read-only workspace demonstrates a wide, dense grid with safe horizontal containment at a narrow viewport. |
| Pattern state fixture | `DataWorkspace` transitions through ready, busy, empty, and failure using existing public APIs; each state exposes the hook and retains status semantics. |

The browser tests assert computed values only where they represent a stable
contract: semantic token resolution, public-part backgrounds/borders, selected
and hover treatments, pager enablement and result summaries, state visibility,
and viewport containment. Screenshots at desktop and narrow viewports provide
human review evidence but do not replace behavioral assertions.

## Accessibility And Compatibility

- Keyboard focus remains visible on sortable headers, row actions, toolbar
  commands, pagination, and confirmation actions.
- Color is not the sole indication of selected, disabled, busy, empty, or
  failed state; native attributes, status text, and control enabled state stay
  intact.
- The theme may not remove `aria-live`, dialog focus trapping, Grid keyboard
  navigation, pagination-button names and keyboard behavior, or confirmation
  cancel paths.
- Narrow layouts may permit the Grid's established horizontal scroll region,
  but the page canvas, toolbar, workspace boundary, and actionable controls
  must not overflow the viewport or overlap each other.
- The production build is a first-class verification target because Vaadin may
  transform CSS and inline host assets during bundling.

## Documentation And Verification

The implementation updates the English token guide, English appearance guide,
Chinese appearance guide, and extension guide. It records that host themes own
workspace presentation; modules compose `DataWorkspace`, `PagedGrid`, and its
pager, semantic tokens, and normal Vaadin state APIs, never Ant selectors or
overlay/grid internals.

Verification proceeds test-first. It includes pattern unit tests for the state
hook; CSS-contract tests for the new token and public-selector boundary;
focused Ant comfortable and compact browser tests; the existing baseline
browser regression; full default and production Maven verification; a diff
whitespace check; and a fresh desktop/narrow screenshot review.

## Completion Criteria

Phase C is complete when the Ant profile presents dense data workspaces,
pagination, toolbars, state feedback, row actions, and destructive
confirmations as one coherent system in representative real workflows, while:

- `admin-flow` remains profile- and Spring-free;
- business modules remain host-theme independent;
- the Vaadin profile and existing behavior remain intact; and
- default and production verification both pass.
