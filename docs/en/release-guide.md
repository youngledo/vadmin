# Release Guide

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

[简体中文](../zh-CN/release-guide.md) | English

## Scope

This guide defines the first public `0.1.0` release. The release delivers
`vadmin-spring-boot-starter` as the supported adoption artifact: default shell
and theme, system administration, module assembly, and the Spring adapters
needed by a consumer.

## Verified Compatibility Baseline

| Area | `0.1.0` verified baseline | Release boundary |
| --- | --- | --- |
| JDK | Java 25 | Java 25 is required to build and run a consumer. |
| Build tool | Maven Wrapper 4.0.0-rc-6 | A Maven GA upgrade is independent work. |
| Runtime | Spring Boot 4.1.0 | Spring Boot is the only supported runtime. |
| UI | Vaadin Flow 25.2.5 | Flow is the only UI model; Hilla, React, Vue, and TypeScript are excluded. |
| Database and migration | PostgreSQL 18 Compose baseline, Flyway 13.1.0 | Testcontainers coverage is not an additional deployment promise. |
| Appearance | `vaadin` and `ant`; light/dark; comfortable/compact | All profiles preserve the same Flow, permission, module, and i18n contracts. |
| UI locales | `zh-CN`, `en-US` | Starter and consumer module resources must support both locales. |
| External identity | Opt-in standard OIDC authorization-code login | Provider lifecycle, SCIM, SAML, LDAP, MFA, tenant, and data-scope policies remain consumer concerns. |

Any Java, Maven, Spring Boot, or Vaadin upgrade needs a dedicated pull request,
updated compatibility evidence, and both normal and production verification.

## Consumer Upgrade Rules

1. Update all `io.github.youngledo` dependencies to the same target
   version. Do not mix released artifacts with snapshot siblings.
2. Run the consumer with Java 25 and align Spring Boot and Vaadin with the
   verified release table.
3. Review Flyway migrations against a production-like database copy. Apply
   each migration once and never rewrite an applied migration.
4. Keep the documented module contract: `AdminModule` metadata, declared
   permissions, routes, icon keys, `zh-CN` and `en-US` resources, prototype
   view beans without `@Route`, and a host `@Uses` anchor for each consumer
   dynamic view.
5. Keep the starter default shell, theme, and system administration unless the
   consumer intentionally replaces the complete shell with its own
   `AdminHostLayout`, `AppShellConfigurator`, and `@Theme`.
6. Preserve external secrets and deployment configuration. Changing
   `APP_BOOTSTRAP_PASSWORD` does not reset an existing `admin` account.
7. Re-run authorization, locale, appearance, and representative business-flow
   tests. OIDC consumers must also verify their existing-local-account mapping.

## Release Checklist

Run this in a clean checkout of the intended release commit and record command
output, tool versions, image digest, release commit, and tag.

### Source And Documentation

- [ ] `git status --short` is empty after intentional generated-artifact cleanup.
- [ ] `git diff --check` has no output.
- [ ] `CHANGELOG.md` moves release content out of `Unreleased`, includes the
  release date, and identifies breaking changes.
- [ ] README, architecture, quick start, extension, and release guides agree
  that `vadmin-spring-boot-starter` supplies the default shell, theme, and system
  administration.
- [ ] Current guides contain no retired example-domain references or claims
  that a normal consumer must provide a shell or default `AdminHostLayout`.
- [ ] The root `pom.xml` has the release version and first-party reactor
  artifacts resolve to that exact version.
- [ ] Licenses, third-party notices, and vendored icon attribution remain in
  source and release archives.

### Verification And Packaging

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vadmin:0.1.0-rc .
```

- [ ] Both Maven commands complete with `BUILD SUCCESS`.
- [ ] Unit, architecture, Testcontainers, OIDC, baseline browser, and visual
  profile browser suites report zero failures and errors.
- [ ] Production output contains the starter system pages and consumer dynamic
  module routes without the Vaadin development server.
- [ ] Compose configuration renders without unresolved variables.
- [ ] The image starts as a non-root runtime against a fresh PostgreSQL 18
  service using externally supplied credentials.
- [ ] A reviewer signs in, changes locale and light/dark mode, opens Users,
  Roles, Permissions, and Audit, then confirms a permission-filtered consumer
  module page under the selected visual language and density.

## Publication

The `central-release` Maven profile attaches source and Javadoc archives, signs
all publication files with GPG, and uses the Maven Central Publisher Portal.
Credentials belong in the local Maven `settings.xml` server entry named
`central`; never commit them. Publish only after the tag commit is pushed:

```bash
./mvnw -B -ntp -Pproduction,central-release deploy
```

The profile waits for Central Portal publication to complete. Then verify a
clean consumer resolves `io.github.youngledo:vadmin-spring-boot-starter:0.1.0`
before creating the GitHub Release.

## Post-Release Record

Record the release version and tag, commit SHA, verification date,
Java/Maven/Spring Boot/Vaadin versions, command results, image digest, known
limitations, and links to release notes and staged or published artifacts.
Keep the record in the release system, not in source-control secrets or local
shell history.
