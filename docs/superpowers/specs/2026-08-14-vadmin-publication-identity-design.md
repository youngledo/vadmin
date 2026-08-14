# VAdmin Publication Identity And Maven Build Design

**Status:** Proposed
**Date:** 2026-08-14

## Purpose

Rename the project before its first public GitHub and Maven publication. The
project needs a durable product identity that describes a Java/Vaadin Flow
administration baseline without being tied to the current Spring Boot runtime
or to a fictional sample domain. Its Maven build must also represent a
multi-module library and starter distribution rather than an application that
inherits Spring Boot's application parent.

## Decisions

1. The product and GitHub repository name is **VAdmin** and the repository is
   `youngledo/vadmin`.
2. The Maven ownership namespace is `io.github.youngledo`.
3. Public artifacts use the `vadmin-` prefix:
   `vadmin-contracts`, `vadmin-platform`, `vadmin-flow`,
   `vadmin-spring-security`, `vadmin-spring-jpa`, `vadmin-spring-boot`,
   `vadmin-spring-flow`, and `vadmin-spring-boot-starter`.
4. The reference consumer is `vadmin-reference-app`; the Spring aggregator is
   `vadmin-spring`.
5. Java packages use `io.github.youngledo.vadmin` as their root, with the
   existing bounded suffixes (`contracts`, `platform`, `flow`, `springsecurity`,
   `springjpa`, `springboot`, `springflow`, `starter`, and `app`).
6. No compatibility coordinates, old package aliases, relocation POMs, or
   duplicate module trees are retained. This is a pre-publication breaking
   rename, so consumers are not yet owed migration artifacts.
7. The root POM has no parent. It imports
   `org.springframework.boot:spring-boot-dependencies:${spring-boot.version}`
   as a BOM and explicitly manages build plugins that were previously supplied
   by `spring-boot-starter-parent`.

## Publication Identity

The public identity is separated into four layers:

| Layer | Value | Rule |
| --- | --- | --- |
| Product | VAdmin | Human-facing name in README, shell brand, docs, and release notes. |
| GitHub | `youngledo/vadmin` | Repository and issue URL root. |
| Maven | `io.github.youngledo:vadmin-*` | Stable ownership namespace plus artifact prefix. |
| Java | `io.github.youngledo.vadmin.*` | Package namespace follows Maven ownership and product identity. |

The product must not use `Pro` in its name. That word implies a commercial or
feature-tier distinction that this community project does not provide. Vaadin
is retained in technical descriptions because Flow and Vaadin are the
underlying technology, but the product name is independent of the trademark.

## Maven Build Boundary

The root POM becomes the project's build parent and contains only project-wide
coordinates, properties, module aggregation, dependency management, and plugin
management. It must not inherit application defaults from Spring Boot.

It declares complete publication metadata at the root: `VAdmin` as name, a
Java/Vaadin Flow administration-baseline description, the Apache-2.0 license,
`youngledo` as developer, and the Git/SCM URLs for
`https://github.com/youngledo/vadmin`. Child modules inherit this metadata
unless a module has a materially different public description.

The dependency management section imports the Spring Boot BOM:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>${spring-boot.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

The root explicitly manages the Spring Boot Maven plugin at
`${spring-boot.version}` wherever child modules use it. Existing compiler,
Surefire, Failsafe, and Vaadin plugin versions remain explicit. Dependency
versions still come from the imported BOM or existing project properties; no
module may reintroduce `spring-boot-starter-parent` as a parent.

The root remains a `pom` project and retains the Maven 4 model version already
used by this repository. Removing the external parent must not remove Java 25,
UTF-8, test plugin, or production profile behavior.

## Module And Package Migration

The directory names, POM artifact IDs, inter-module dependency coordinates,
package declarations, imports, source paths, test paths, Spring component scan
packages, Vaadin `@EnableVaadin` packages, entity scan packages, service-loader
paths that encode class names, and documentation examples are migrated as one
atomic change.

The migration preserves behavior. It does not reorganize bounded contexts,
change the `AdminModule` contract, alter routes, modify permissions, or change
the supported runtime. A consumer still depends on one starter:

```xml
<dependency>
    <groupId>io.github.youngledo</groupId>
    <artifactId>vadmin-spring-boot-starter</artifactId>
</dependency>
```

The default shell brand changes from `Vaadin Admin Starter` to `VAdmin`.
Technical documentation describes it as “VAdmin, a Java/Vaadin Flow admin
baseline for Spring Boot applications”. The existing Vaadin trademark
disclaimer remains.

## Validation Requirements

The migration is accepted only when all of the following are true:

1. A Maven model test confirms the root has no `<parent>`, imports the Spring
   Boot BOM, and exposes `io.github.youngledo` plus `vadmin` coordinates.
2. A module-coordinate test confirms every reactor module has a `vadmin-`
   artifact ID and no dependency or module path contains the old coordinates.
3. A package-reference test or compile scan confirms no production source,
   resource, configuration, or documentation contains
   `io.github.vaadinadminstarter`.
4. The normal reactor test suite passes with Java 25.
5. The production profile test suite and Vaadin frontend generation pass.
6. The starter dependency test resolves
   `io.github.youngledo:vadmin-spring-boot-starter` and rejects the old starter
   coordinate.
7. The reference consumer browser tests still cover local login, system
   administration, module permissions, i18n, appearance profiles, and narrow
   layout.
8. The README, English and Chinese current guides, Docker files, and release
   metadata use VAdmin and the new coordinates. Historical specifications may
   retain the old identity as archival records.
9. The root POM has Maven Central-ready name, description, URL, license,
   developer, and SCM metadata for `youngledo/vadmin`.

## Non-goals

- Publishing to Maven Central or creating the GitHub repository in this phase.
- Changing the application behavior, visual design, authorization model, or
  supported Spring Boot/Vaadin versions.
- Introducing a second runtime or compatibility aliases for the old namespace.
- Renaming historical documents that intentionally record earlier decisions.

## Risks And Mitigations

| Risk | Mitigation |
| --- | --- |
| A string-based rename misses a package or resource reference. | Use path-aware replacement, compile every module, and run an old-namespace scan. |
| Removing the Boot parent loses plugin or dependency defaults. | Import the Boot BOM and add explicit plugin management; validate effective POM and production build. |
| A starter artifact is renamed but docs still teach the old coordinate. | Add a current-documentation guard for old coordinates, old package root, and old product name. |
| The reference app hides an incomplete migration. | Build and test every reactor module, then run the reference browser acceptance suite. |
