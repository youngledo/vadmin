# Spring Module Aggregation Design

**Status:** Approved design
**Date:** 2026-08-05

## Goal

Group the Spring-specific adapters beneath one Maven aggregation module while
preserving the framework-neutral core and all existing published Maven
coordinates.

## Scope

Move the existing Spring module directories into a new `admin-spring` parent:

```text
admin-spring/
  pom.xml
  admin-spring-security/
  admin-spring-jpa/
  admin-spring-boot/
```

The following modules remain at the repository root:

```text
admin-contracts/
admin-platform/
admin-flow/
admin-reference-app/
```

## Maven Structure

`admin-spring` is a `pom`-packaging parent and reactor aggregator. It inherits
from the root POM and lists the three Spring child modules as Maven 4
`subproject` entries.

Each moved child module inherits from `admin-spring` using an explicit
`../../pom.xml` relative path. Root-level dependency management, Java release,
plugin management, and the production profile remain inherited transitively.

The root POM replaces its three direct Spring subprojects with one
`admin-spring` subproject. It continues to aggregate the framework-neutral
modules and reference application directly.

## Compatibility Rules

The following artifact IDs remain unchanged:

- `admin-spring-security`
- `admin-spring-jpa`
- `admin-spring-boot`

No Java package, public type, dependency coordinate, configuration key, or
runtime behavior changes. `admin-reference-app` continues to depend on the
same three artifacts.

## Architecture Constraints

This is an organizational refactor only. The existing ports-and-adapters
direction remains:

```text
Reference application -> Spring adapters -> Flow/platform/contracts
Flow/platform -> contracts
Spring adapters -> Flow/platform/contracts
Contracts -> Java only
```

The `admin-spring` parent contributes no runtime code and must not become an
adapter dependency. It exists solely to aggregate Spring-specific modules and
provide their common Maven parent.

The dependency rule is a project architecture policy: Spring Security, JPA,
Flyway, and Spring Boot remain outside `admin-contracts`, `admin-platform`,
and `admin-flow`.

## Required Updates

- Update Maven parent paths and reactor subproject declarations.
- Update CI, Docker, and documentation references that include moved paths.
- Update architecture documentation to present `admin-spring` as the Spring
  adapter family and its three child modules as concrete adapters.
- Preserve or update architecture tests only where their source paths are
  explicit; package-level dependency rules must remain unchanged.

## Verification

1. Run `./mvnw -B -ntp verify` from the root to validate the complete reactor.
2. Run `./mvnw -B -ntp -Pproduction verify` to validate the production
   packaging path.
3. Run `docker compose --env-file .env.example config` to validate deployment
   configuration paths and environment interpolation.
4. Confirm `git status --short` contains only intended source, documentation,
   and build-layout changes.

## Architecture Guidance Result

- **Phase:** Architecture Guidance
- **Status:** completed
- **Inputs:** existing modular-monolith architecture, user-requested Spring
  aggregation, and the confirmed requirement to retain Maven coordinates.
- **Summary:** introduce a Maven-only Spring parent that improves navigation
  without altering module responsibilities or dependency direction.
- **Assumptions:** the root POM remains the source of shared version and plugin
  management; `admin-spring` is not intended as a separately consumed runtime
  library.
- **Decisions:** use `admin-spring/` with children named after their existing
  artifact IDs; retain all existing artifact IDs and Java packages.
- **Constraints:** framework-neutral modules remain free of Spring, JPA, and
  Flyway dependencies; the reference application remains the composition root.
- **Evidence:** root `pom.xml`, module POMs, and `docs/architecture.md`.
- **Open Questions:** none.
- **Artifacts:** this design specification.
- **Recommended Next Step:** create a staged implementation plan for the Maven
  and path migration.
- **Handoff Notes:** the inward dependency direction is an existing project
  policy, not a claim that every Java project requires this module layout.
