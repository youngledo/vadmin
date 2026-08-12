# Standalone Consumer Verification

## Status

Approved implementation direction. This verification closes the gap between an
independently packaged module inside the reactor and the experience of a Java
team that consumes starter artifacts from a Maven repository.

## Goal

Prove that a minimal Spring Boot host can resolve the starter and
`admin-example-orders` exclusively through locally installed Maven artifacts,
provide its own host wiring, and serve the dynamically registered `/orders`
page after local-password authentication.

## Scope

Create a small Maven project below `verification/standalone-consumer`. It is
deliberately excluded from the root Maven 4 reactor and has no parent or source
dependency on this repository's reference application. Its POM pins the same
Java 25, Spring Boot 4.1.0, and Vaadin Flow 25.2.5 baseline, then imports
first-party artifacts only by their `io.github.vaadinadminstarter` coordinates
and `0.1.0-SNAPSHOT` version.

The host provides the responsibilities that remain correctly host-owned:

- a minimal `@Layout` with `@Uses(OrdersView.class)` as the production frontend
  anchor;
- `AdminHostLayout`, JPA entity scanning, and explicitly imported Spring JPA,
  security, and HTTP-error adapters;
- a small bootstrap configuration that synchronizes the assembled module
  permission catalog and creates a development-only `admin` account with its
  permissions; and
- its own application configuration and Flyway/JPA settings.

The verification project consumes `admin-example-orders`; it must not create a
second business module, copy `admin-reference-app` layouts, depend on its
classes, or add a production publishing configuration. The normal local Maven
repository is sufficient for this milestone.

## Verification Contract

The root script installs the complete first-party reactor to the local Maven
repository, then invokes Maven from the standalone POM. It must run:

1. the standalone consumer's integration tests against Testcontainers
   PostgreSQL;
2. a browser flow that logs in as its own development administrator, opens
   `/orders`, and observes the orders module's translated title and rows; and
3. a production-profile package of the standalone POM, proving that the host's
   static `@Uses` anchor keeps the dynamic orders route available to Vaadin's
   production frontend build.

The standalone project records the resolved first-party artifact tree as an
assertion of repository consumption. It must not use `-am`, a root POM parent,
or classpath directories from the reactor when it builds.

## Non-Goals

- Publishing to Maven Central, signing, staging, release tagging, or changing
  first-party versions.
- A generic application generator, a second reference application, a complete
  application shell, or an alternate runtime.
- Testing every starter feature; the existing reactor remains responsible for
  comprehensive RBAC, OIDC, audit, visual-profile, and error-flow coverage.

## Completion Criteria

This work is complete when a developer can run one documented script from a
clean source checkout with Docker available, and that script proves local Maven
installation followed by normal and production standalone-consumer builds. The
extension and release guides explain this validation path without claiming that
the artifacts have been publicly published.
