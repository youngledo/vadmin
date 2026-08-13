# Theme Tokens

`admin-spring-starter` owns the default Vaadin Flow theme. Its
`DefaultApplicationShell` registers `@Theme("admin-theme")`, and the starter's
`admin-theme/styles.css` implements this token contract. Normal consumers and
business modules use the tokens; they do not define a shell or global theme.

## Token Contract

Tokens are available in default and dark scopes. Use a token for its semantic
meaning, never for a particular color or size.

| Token | Meaning |
| --- | --- |
| `--admin-surface` | Default surface for content and controls. |
| `--admin-surface-raised` | Raised surface, such as application chrome. |
| `--admin-text-primary` | Primary readable text. |
| `--admin-text-secondary` | Supporting text and less prominent labels. |
| `--admin-border` | Neutral structural border. |
| `--admin-accent` | Primary action and selected-state accent. |
| `--admin-success` | Successful outcome. |
| `--admin-warning` | Warning outcome. |
| `--admin-danger` | Destructive action or error outcome. |
| `--admin-focus` | Keyboard focus indicator. |
| `--admin-font-family` | Application font stack. |
| `--admin-space-sm` | Compact related spacing. |
| `--admin-space-md` | Standard layout and control spacing. |
| `--admin-space-lg` | Content-canvas spacing and larger separation. |
| `--admin-control-height` | Standard control height for the active density. |
| `--admin-grid-cell-padding` | Grid padding for the active density. |
| `--admin-utility-size` | Stable square dimension for shell utility controls. |
| `--admin-radius-control` | Standard interactive-control corner radius. |
| `--admin-radius-surface` | Corner radius for contained work surfaces. |
| `--admin-elevation-raised` | Shadow for raised application chrome. |
| `--admin-elevation-workspace` | Reserved elevation for a data workspace. |
| `--admin-control-fill` | Normal fill of a form control. |
| `--admin-control-border` | Resting border of a form control. |
| `--admin-control-hover-border` | Enabled-control hover border. |
| `--admin-control-disabled-fill` | Non-interactive control fill. |
| `--admin-overlay-surface` | Surface for menus, pickers, dialogs, and notifications. |
| `--admin-overlay-shadow` | Elevation for overlays. |
| `--admin-workspace-header-fill` | Header surface for dense data workspaces. |
| `--admin-workspace-header-text` | Secondary workspace-header text. |
| `--admin-workspace-row-hover` | Hover surface for enabled workspace rows. |
| `--admin-workspace-row-selected` | Selected-row and selection-summary surface. |
| `--admin-workspace-divider` | Low-emphasis workspace separator. |
| `--admin-workspace-status-fill` | Busy, empty, and failure state surface. |
| `--admin-workspace-danger-fill` | Consequence surface for a destructive confirmation. |

The default theme maps the relevant Vaadin Lumo variables from these semantic
tokens in both light and dark scopes. Legacy aliases remain only for
compatibility; new module CSS uses the canonical names above.

## Appearance Axes

Visual language, color mode, and density are independent. The starter supports
`vaadin` and `ant` visual languages, session-scoped light/dark mode, and
comfortable/compact density. Modules consume tokens and shared Flow patterns;
they must not select a visual language or density, modify global Lumo variables,
or depend on profile-specific selectors.

Use `AdminIcon` and `AdminIconName` from `admin-flow` for standard actions.
Do not import starter theme icons, assign `data-admin-icon`, or set icon-mask
variables from a business module. The starter owns profile-specific icon assets
and Vaadin fallbacks.

## Business Module CSS

Keep CSS scoped to a module-owned component class:

```css
.inventory-summary {
  background: var(--admin-surface-raised);
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-control);
  color: var(--admin-text-primary);
  padding: var(--admin-space-md);
}
```

Do not hard-code a competing brand color, replace global page backgrounds, ship
arbitrary global selectors, or target Flow overlay and Grid internals. Use the
shared `admin-flow` page patterns and normal Vaadin state APIs.

## Full Shell Replacement

Only a consumer deliberately replacing the complete shell owns an alternative
`AppShellConfigurator`, `@Theme`, and token implementation. It must preserve a
coherent accessible appearance for all assembled modules. Partial replacement
of starter shell pieces or selected system pages is not supported.
