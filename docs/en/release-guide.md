# Release Guide

[简体中文](../zh-CN/release-guide.md) | English

## Scope

This guide prepares the first public `0.1.0` release. The current source
version remains `0.1.0-SNAPSHOT`; this document does not publish artifacts,
create a tag, change a Maven version, or authorize a Maven Central release.

The release goal is a reproducible Spring Boot and Vaadin Flow baseline for
Java teams building internal administration applications. It includes the
reusable modules, the independently packaged orders example, and the reference
application. It is not a promise to support every Spring Boot, Vaadin, JVM,
database, identity provider, or visual language version.

## Verified Compatibility Baseline

The root `pom.xml`, Maven Wrapper, production profile, Compose stack, and test
suite are the source of truth for this table. A release records exact verified
versions, not an untested compatibility range.

| Area | `0.1.0` verified baseline | Release boundary |
| --- | --- | --- |
| JDK | Java 25 | Java 25 is required to build and run the reference application. |
| Build tool | Maven Wrapper 4.0.0-rc-6 | Maven 4 is still a release candidate; a Maven GA upgrade is independent work. |
| Runtime | Spring Boot 4.1.0 | Spring Boot is the only supported runtime. |
| UI | Vaadin Flow 25.2.5 | Flow is the only UI programming model. Hilla, React, Vue, and TypeScript are excluded. |
| Database and migration | PostgreSQL 18 Compose baseline, Flyway 13.1.0 | PostgreSQL 17 is used by some Testcontainers fixtures; that is test coverage, not a separate deployment promise. |
| Visual language | `vaadin`, `ant` | Both retain the same Flow/Java, route, permission, module, and i18n contracts. |
| Color and density | light/dark session mode; comfortable/compact host density | These are independent appearance axes. |
| UI locales | `zh-CN`, `en-US` | Modules must provide both declared message bundles. |
| External identity | Standard OIDC authorization-code login, opt-in | Keycloak is a test fixture only. Provider lifecycle, SCIM, SAML, LDAP, MFA, tenant, and data-scope policies belong to consumers. |

An upgrade to Java, Maven, Spring Boot, or Vaadin requires a dedicated pull
request, updated compatibility evidence, and both verification profiles. A
dependency patch or minor update must also be tested before it is recorded as
supported.

## Versioning Policy

The reactor version is owned by the root `pom.xml`; every first-party Maven
module inherits it. The public release changes `0.1.0-SNAPSHOT` to `0.1.0` in
one dedicated release change. Do not release a mixture of first-party artifact
versions.

This project follows semantic-versioning intent and Keep a Changelog:

| Change | Version treatment before 1.0 | Required record |
| --- | --- | --- |
| Documentation, tests, compatible fixes | patch release when independently releasable | Changelog entry and release verification. |
| Additive documented API, configuration, module metadata, or behavior | minor release | Compatibility statement, migration notes, and full verification. |
| Remove or change documented consumer behavior | next minor release during `0.x` | Explicit breaking-change section, migration guide, and full verification. |
| Unpublished snapshot work | no compatibility promise | Keep it under `Unreleased`; do not imply it is consumable as a release. |

Only documented APIs and configuration form the initial consumer contract.
Internal implementation classes, Vaadin shadow DOM, profile-private CSS
selectors, test fixtures, and reference-application business data are not
public extension APIs.

## Upgrade Rules For Consumers

Before updating a consumer, read the target release notes and make a rollback
plan for its database and file storage. Then apply these checks in order:

1. Update all `io.github.vaadinadminstarter` dependencies to the same target
   version. Do not mix a released module with a snapshot sibling.
2. Run the consumer with Java 25 and the project Maven Wrapper baseline. Keep
   its Spring Boot and Vaadin versions aligned with the verified release table.
3. Review every Flyway migration. Apply migrations once to a production-like
   database copy and validate the corresponding file-storage backup before
   deployment. Never rewrite a migration that has reached an environment.
4. For independently packaged Flow modules, preserve the documented
   `AdminModule` contract: module-owned permissions, routes, icon keys,
   `zh-CN` and `en-US` bundles, and route views without `@Route`. Do not add a
   host `PermissionCatalog` or target host theme internals.
5. Preserve externally supplied secrets and deployment configuration. In
   particular, an existing `admin` account is not reset when
   `APP_BOOTSTRAP_PASSWORD` changes.
6. Re-run the consumer's authorization, locale, appearance, and representative
   business-flow tests. An OIDC consumer must also verify its existing-local-
   account mapping; the starter never provisions or assigns roles from claims.

## Release Checklist

Run this checklist in a clean checkout of the intended release commit. Record
the command output, tool versions, image digest, release commit, and tag in the
release record.

### Source And Documentation

- [ ] `git status --short` is empty after intentional generated-artifact
  cleanup.
- [ ] `git diff --check` has no output.
- [ ] `CHANGELOG.md` moves the release contents out of `Unreleased`, includes
  the release date, and identifies any breaking changes.
- [ ] The compatibility table, quick start, deployment guide, security guide,
  extension guide, and release guide agree on the exact version and supported
  runtime boundary.
- [ ] The root `pom.xml` changes from `0.1.0-SNAPSHOT` to the release version;
  all first-party reactor artifacts resolve to that exact version.
- [ ] `LICENSE`, third-party notices, and vendored icon attribution remain in
  the source and release archive.

### Verification And Packaging

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
./scripts/verify-standalone-consumer.sh
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:0.1.0-rc .
```

- [ ] Both Maven commands complete with `BUILD SUCCESS`.
- [ ] Unit, architecture, Testcontainers, OIDC, baseline browser, Ant desktop,
  and Ant compact browser suites report zero failures and zero errors.
- [ ] The production build includes the dynamically composed Flow routes and
  works without the Vaadin development server.
- [ ] The standalone-consumer script installs local artifacts, completes its
  authenticated orders browser flow, and creates its independent production
  frontend package.
- [ ] Compose configuration renders without unresolved variables.
- [ ] The image builds with the non-root runtime image and starts against a
  fresh PostgreSQL 18 service using externally supplied credentials.
- [ ] A reviewer signs in, switches locale and light/dark mode, opens Users,
  Customers, Roles, Audit, and Orders, and verifies the selected host visual
  language (`vaadin` or `ant`) and its compact/comfortable density.

### Publication Prerequisites

This repository does not yet configure public artifact publication. Before
uploading a public `0.1.0`, add and verify a separate publishing change that
covers the chosen repository's coordinates, credentials, signing, POM metadata
(name, description, URL, SCM, developers, and license), source and Javadoc
artifacts where applicable, and provenance requirements. Review the resulting
staged artifacts from a clean consumer project before publishing or promoting
them.

The repository includes the equivalent pre-publication acceptance path for
snapshots in the local Maven repository:

```bash
./scripts/verify-standalone-consumer.sh
```

It installs the first-party reactor locally, then verifies an independent
Spring Boot host that consumes the starter and `admin-example-orders` only by
coordinates. Run it after the normal release checks; it is evidence of local
consumer adoption, not an artifact-publication step.

Do not treat a green local reactor build as authorization to upload artifacts,
push a release tag, create a GitHub Release, or expose bootstrap credentials.

## Post-Release Record

The release record should contain the release version and tag, commit SHA,
verification date, Java/Maven/Spring Boot/Vaadin versions, Maven command
results, container image digest, known limitations, and links to release notes
and staged or published artifacts. Keep it with the release system of record,
not in source control secrets or local shell history.
