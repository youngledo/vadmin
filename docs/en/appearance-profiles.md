# Appearance Profiles

The reference application has three independent appearance axes:

| Axis | Values | Owner |
| --- | --- | --- |
| Visual language | `vaadin`, `ant` | Host configuration |
| Color mode | `light`, `dark` | Current UI session |
| Density | `comfortable`, `compact` | Host configuration |

The `ant` option is a Flow-native visual language inspired by Ant Design's
operational hierarchy and density. It does not add an Ant Design, React, Vue,
or other frontend runtime dependency. Vaadin Flow remains the component and
programming model for every profile.

## Host Configuration

Set the profile and density in the host application configuration:

```yaml
app:
  appearance:
    visual-language: ant # vaadin | ant
    density: compact # comfortable | compact
```

The same values can be supplied through `APP_APPEARANCE_VISUAL_LANGUAGE` and
`APP_APPEARANCE_DENSITY`. The corresponding Spring properties are
`app.appearance.visual-language` and `app.appearance.density`. Unknown or blank
values safely resolve to `vaadin` and `comfortable`. The host applies the resolved values as
`data-admin-visual-language` and `data-admin-density` on the Flow UI root.

Profile and density are host choices, not ordinary user preferences in this
release. The shell appearance menu continues to control only the user's
light/dark session mode; changing that mode does not replace the selected
visual language or density.

## Module Boundary

Business modules use public `--admin-*` semantic tokens and the shared Flow
page patterns. They must not import the host's `admin-theme`, register a global
`@Theme`, select a profile or density, mutate global Lumo variables, or depend
on Ant-only selectors. This keeps a module portable across every host profile.
