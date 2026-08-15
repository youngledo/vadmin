# Appearance Profiles

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

VAdmin has one optional visual-language choice:

| Visual language | Owner | Behavior |
| --- | --- | --- |
| `vaadin` | Host configuration | Default. Uses Vaadin Aura and documented Vaadin APIs. |
| `ant` | Host configuration | Explicit Ant Design-inspired alternative implemented by VAdmin. |

Both languages use Vaadin's `ColorScheme` API. Each UI session can select System
preference, Light, or Dark mode. The System preference uses the browser or
operating-system setting; VAdmin does not maintain a duplicate dark-theme
attribute or a separate color token system.

## Host Configuration

Select the visual language in the host application configuration:

```yaml
app:
  appearance:
    visual-language: ant # vaadin | ant
```

The same value can be supplied through `APP_APPEARANCE_VISUAL_LANGUAGE`. Unknown
or blank values safely resolve to `vaadin`. The host applies the resolved value
as `data-vadmin-visual-language` on the UI root.

Aura's official `--aura-base-size` property controls global density. To set it
from host configuration without introducing VAdmin density aliases, use an
integer from 12 through 24:

```yaml
app:
  appearance:
    aura-base-size: 16
```

The equivalent environment variable is `APP_APPEARANCE_AURA_BASE_SIZE`. When
unset or outside the supported range, VAdmin leaves Aura's default unchanged.

## Module Boundary

Business modules use Vaadin component variants, Aura and base-style properties,
and the shared Flow page patterns. They must not add a VAdmin global stylesheet,
register a global stylesheet, modify global theme properties, or depend on Ant-only
selectors or `--vadmin-ant-*` tokens.

For standard action icons, modules may use `AdminIcon.of(AdminIconName)` from
`admin-flow`. Navigation metadata must keep using the validated
`AdminIconCatalog` keys. Modules must not reference host SVG files or CSS mask
selectors: VAdmin decides how each semantic icon is rendered for the selected
visual language.

VAdmin owns Ant presentation for buttons, fields, menus, dialogs, notifications,
and dense data workspaces. Modules keep using normal Vaadin Flow semantics and
must not target overlay or Grid internals.
