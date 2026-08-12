# Theme Tokens

The host application owns the Vaadin Flow theme. In the reference application,
`ApplicationShell` declares `@Theme("admin-theme")`, and
`admin-theme/styles.css` is the reference implementation of this token
contract.

Business modules consume these semantic CSS custom properties. They must not
declare a global `@Theme`, modify Lumo variables globally, or select a theme at
runtime. This keeps a module visually consistent with every host that adopts
the contract, while the host remains free to apply its own brand.

## Token Contract

The following tokens are available in both the default and dark theme scopes.
Their values are intentionally semantic: modules should choose the token for
its meaning, never for a particular color or size.

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
| `--admin-space-sm` | Compact, related spacing. |
| `--admin-space-md` | Standard layout and control spacing. |
| `--admin-space-lg` | Content-canvas spacing and larger layout separation. |
| `--admin-control-height` | Standard interactive-control height for the active density. |
| `--admin-grid-cell-padding` | Grid cell padding for the active density. |
| `--admin-utility-size` | Stable square dimension for compact shell utility controls. |
| `--admin-radius-control` | Standard interactive-control corner radius. |
| `--admin-radius-surface` | Corner radius for contained work surfaces. |
| `--admin-elevation-raised` | Shadow for raised application chrome. |
| `--admin-elevation-workspace` | Reserved elevation for a data workspace surface. |

The reference theme maps Vaadin Lumo variables from these semantic tokens.
In particular, `--lumo-font-family` is mapped from `--admin-font-family`, and
the primary 10% and 50% Lumo state colors are derived from `--admin-accent`.
The standard Lumo success, warning, and error colors are mapped from
`--admin-success`, `--admin-warning`, and `--admin-danger`; Vaadin's normal
control focus ring is mapped from `--admin-focus`.
Flow applies the theme attribute to `body`, so the reference theme repeats its
Lumo and Vaadin mappings in the dark selector. This ensures those variables
resolve the dark semantic tokens instead of inheriting values computed at the
root scope.
Legacy `--admin-primary`, `--admin-text`, `--admin-space-compact`,
`--admin-space-standard`, `--admin-radius`, and `--admin-elevation` aliases
remain for compatibility. New module CSS must use the canonical names in the
table.

## Appearance Axes

Visual language, color mode, and density are independent appearance axes. The
reference application currently supports the `vaadin` and `ant` visual
languages, light and dark session color modes, and `comfortable` and `compact`
densities. The host selects visual language and density; the existing session
appearance control selects only light or dark mode. See
[Appearance Profiles](appearance-profiles.md) for host configuration.

Modules consume the semantic tokens and shared Flow patterns. They must not
select a visual language or density, mutate Lumo variables globally, or depend
on profile-specific selectors such as `[data-admin-visual-language="ant"]`.

Semantic icons follow the same ownership rule. Use `AdminIcon` and
`AdminIconName` from `admin-flow` for standard actions, but do not import
`admin-theme/icons`, assign `data-admin-icon`, or set icon mask variables from
a module. The host provides the Ant SVG masks and keeps the Vaadin fallback
visible in the Vaadin profile.

## Host Overrides

A host may override a token in its own theme while retaining the same semantic
meaning. Define default values in `:root` and provide a corresponding dark-mode
value when the visual decision needs one:

```css
:root {
  --admin-accent: #0f766e;
  --admin-focus: #0f766e;
  --admin-font-family: "IBM Plex Sans", sans-serif;
}

html[theme~="dark"],
[theme~="dark"] {
  --admin-accent: #5eead4;
  --admin-focus: #5eead4;
}
```

Override tokens in the host theme after the reference declarations. Do not
override a Lumo variable directly when a matching `--admin-*` token exists.
The host alone controls theme registration and light/dark mode; the reference
application stores its mode choice in the Vaadin session.

## Module CSS

Module CSS should remain scoped to a module-owned component class and compose
the token contract. For example:

```css
.orders-summary {
  background: var(--admin-surface-raised);
  border: 1px solid var(--admin-border);
  border-radius: var(--admin-radius-control);
  color: var(--admin-text-primary);
  padding: var(--admin-space-md);
}
```

Do not hard-code a competing brand color, replace global page backgrounds, or
ship arbitrary global selectors. Reuse the Java Flow page patterns from
`admin-flow` for standard administration workflows and use scoped CSS only for
genuine module-specific presentation.
