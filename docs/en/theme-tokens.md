# Theme Policy

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

`vadmin-spring-boot-starter` owns the default Vaadin Flow shell. Its
`DefaultApplicationShell` explicitly loads Lumo; the bundled Ant resource is
fully scoped and has no effect unless the host selects the `ant` language.
Normal consumers and business modules do not define a competing shell or global
theme.

## Default Vaadin Language

`vaadin` is the default visual language. It uses Lumo and Vaadin base-style
properties directly: `--lumo-*` and `--vaadin-*`. Use component variants before
writing CSS. Color scheme selection uses `ColorScheme` and
`Page.setColorScheme()` with System preference, Light, and Dark choices.

The shell does not override App Layout, Grid, fields, buttons, dialogs,
overlays, or notifications. Lumo's standard sizing is retained without a VAdmin
density setting.

## Ant Visual Language

`ant` is an explicit alternative visual language. Its selectors are scoped by
`[data-vadmin-visual-language="ant"]`, and its `--vadmin-ant-*` tokens and
targeted `::part()` overrides implement Ant Design behavior. No module may
depend on those selectors or tokens.

Use `AdminIcon` and `AdminIconName` from `admin-flow` for standard actions.
Do not import starter theme icons, assign `data-admin-icon`, or set icon-mask
variables from a business module. The starter owns profile-specific icon assets
and Vaadin fallbacks.

## Business Module CSS

Keep CSS scoped to a module-owned component class:

```css
.inventory-summary {
  background: var(--lumo-base-color);
  border: 1px solid var(--vaadin-border-color-secondary);
  border-radius: var(--vaadin-radius-m);
  color: var(--vaadin-text-color);
  padding: var(--vaadin-padding-m);
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
