# Repository Guidelines

## Git Commits

- Commit messages must be written in English, including the subject and body.
- Follow the Conventional Commits specification: `type(optional scope): imperative summary`.
- Use a concise imperative subject, no longer than 72 characters. Prefer standard types such as `feat`, `fix`, `docs`, `refactor`, `test`, `build`, and `chore`.
- Add a body when context, rationale, migration steps, or breaking-change details are needed.
- Describe breaking changes in the commit footer using `BREAKING CHANGE:`.

## Theme Governance

- The default `vaadin` visual language uses Vaadin Aura as-is. VAdmin must not
  ship or register CSS that changes its visual appearance; compose the shell
  with documented Vaadin component APIs instead.
- Use `ColorScheme` and `Page.setColorScheme()` for system, light, and dark
  color schemes. Do not implement color schemes by mutating the `theme` HTML
  attribute or by duplicating light and dark component rules.
- Do not introduce VAdmin color, radius, spacing, density, control, or
  notification token systems in the default visual language. An application
  may set the official Aura `--aura-base-size` property directly.
- The optional `ant` visual language is the only place that may define
  VAdmin-owned visual tokens or use targeted `::part()` overrides to reproduce
  Ant Design behavior. Keep every such selector scoped by
  `[data-vadmin-visual-language="ant"]`.
- Business modules express business semantics and use VAdmin Flow patterns and
  Vaadin variants. They do not add global visual CSS, override Vaadin
  component internals, or depend on Ant-only selectors.
