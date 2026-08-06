# Documentation Internationalization Design

**Status:** Proposed  
**Date:** 2026-08-06

## Goal

Make the project documentation usable by both international contributors and
Chinese-speaking Java teams without mixing languages inside a document body.

## Language Model

- English is the canonical documentation language under `docs/`.
- Simplified Chinese is a complete user-documentation mirror under
  `docs/zh-CN/`.
- The repository-root `README.md` is a concise bilingual entry point. It links
  to each language instead of carrying duplicated long-form content.
- English and Chinese documents contain one language per body. Product names,
  Java identifiers, commands, paths, and literal UI labels remain unchanged
  where translation would make them inaccurate.

## Included Documents

The user-facing documentation set is translated and kept structurally paired:

| English source | Chinese mirror |
| --- | --- |
| `docs/architecture.md` | `docs/zh-CN/architecture.md` |
| `docs/quick-start.md` | `docs/zh-CN/quick-start.md` |
| `docs/extension-guide.md` | `docs/zh-CN/extension-guide.md` |
| `docs/deployment.md` | `docs/zh-CN/deployment.md` |
| `docs/security.md` | `docs/zh-CN/security.md` |
| `docs/contributing.md` | `docs/zh-CN/contributing.md` |
| `docs/requirements.md` | `docs/zh-CN/requirements.md` |

Architecture diagrams remain shared assets in `docs/diagrams/`. They are not
copied for each language. Their surrounding explanatory prose is translated.

## Excluded Documents

Historical architecture plans and specifications under `docs/superpowers/`
remain English-only engineering records. They are not part of the public
documentation navigation and are not translated in this migration.

## Navigation And Links

- Every paired document has a short language switch near its title.
- Relative links must resolve from both language trees.
- The README exposes English and Chinese entry links to the quick start,
  architecture, extension guide, deployment, security, and contributing guide.
- Links to shared diagrams and repository-root files use the correct relative
  path from the Chinese tree.

## Content Quality

- Existing mixed Chinese/English prose is normalized to the document's chosen
  language.
- Translation preserves technical meaning, version constraints, commands,
  environment variable names, module names, and security caveats.
- The English canonical text is retained rather than replaced by a translation.
- No generated translation system or runtime documentation dependency is added.

## Acceptance Criteria

1. All included English documents have a Chinese counterpart with a reciprocal
   language link.
2. The README provides clear bilingual entry navigation.
3. Included document bodies do not mix English and Chinese explanatory prose.
4. Internal Markdown links and diagram references resolve from both language
   trees.
5. Existing Maven, Compose, security, architecture, and extension instructions
   remain technically accurate.
