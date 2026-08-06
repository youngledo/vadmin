# Documentation Internationalization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish English canonical documentation with complete Simplified Chinese user-documentation mirrors and stable bilingual navigation.

**Architecture:** English user documents remain under `docs/`; Chinese mirrors live under `docs/zh-CN/`. The root README becomes a concise bilingual directory. Paired documents link to each other; diagrams remain shared under `docs/diagrams/`; `docs/superpowers/` remains English-only engineering history.

**Tech Stack:** Markdown, Git, repository-relative links, shell validation.

---

### Task 1: Establish Bilingual Entry Navigation

**Files:**
- Modify: `README.md`
- Modify: `docs/quick-start.md`
- Create: `docs/zh-CN/quick-start.md`

- [ ] Add a concise `Documentation` section to `README.md` with English/Chinese pairs for Quick Start, Architecture, Extension Guide, Deployment, Security, and Contributing.
- [ ] Add `English | [简体中文](zh-CN/quick-start.md)` below the English quick-start title, and `[English](../quick-start.md) | 简体中文` below its Chinese mirror title.
- [ ] Translate every quick-start explanation and warning to Chinese while retaining commands, paths, image tags, environment-variable names, ports, and bootstrap credentials exactly.
- [ ] Run `test -f docs/zh-CN/quick-start.md && git diff --check` and commit `docs: add bilingual documentation entry points`.

### Task 2: Translate Architecture And Extension Documentation

**Files:**
- Modify: `docs/architecture.md`
- Modify: `docs/extension-guide.md`
- Create: `docs/zh-CN/architecture.md`
- Create: `docs/zh-CN/extension-guide.md`

- [ ] Normalize any Chinese explanatory prose in the two English canonical documents to English, keeping code, module IDs, Java packages, commands, RFC references, and literal UI labels unchanged.
- [ ] Create complete Chinese mirrors with matching headings, tables, diagrams, and code blocks; add reciprocal language links after each title.
- [ ] Verify English diagram paths use `diagrams/` and Chinese paths use `../diagrams/`; every referenced target must exist.
- [ ] Run `git diff --check` and commit `docs: add Chinese architecture and extension guides`.

### Task 3: Translate Operational And Community Documentation

**Files:**
- Modify: `docs/deployment.md`
- Modify: `docs/security.md`
- Modify: `docs/contributing.md`
- Modify: `docs/requirements.md`
- Create: `docs/zh-CN/deployment.md`
- Create: `docs/zh-CN/security.md`
- Create: `docs/zh-CN/contributing.md`
- Create: `docs/zh-CN/requirements.md`

- [ ] Normalize English canonical prose, preserving exact deployment commands, environment variables, placeholders, image tags, HTTP media types, module IDs, and security constraints.
- [ ] Create complete Chinese counterparts and reciprocal language links without translating commands or identifiers.
- [ ] Run `find docs/zh-CN -maxdepth 1 -name '*.md' | wc -l` and confirm seven Chinese user documents exist.
- [ ] Run `git diff --check` and commit `docs: add Chinese operational guides`.

### Task 4: Validate Links, Language Boundaries, And Scope

**Files:**
- Modify if needed: `README.md`, `docs/*.md`, `docs/zh-CN/*.md`
- Exclude: `docs/superpowers/**`

- [ ] Check that every named English/Chinese pair exists and reciprocal language-switch links resolve from its own directory.
- [ ] Review each English document for English-only explanatory prose and each Chinese mirror for Chinese-only explanatory prose, allowing commands, identifiers, paths, code blocks, product names, and literal UI labels.
- [ ] Run `git diff --check` and `git status --short`; remove no user files and ensure no generated artifacts are included.
- [ ] Request fresh specification and quality reviews for technical preservation, link correctness, scope, and readability.
- [ ] Commit final adjustments with `docs: finalize bilingual documentation`.
