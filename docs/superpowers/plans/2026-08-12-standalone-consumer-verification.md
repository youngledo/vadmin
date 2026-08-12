# Standalone Consumer Verification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Verify the Spring Boot and orders-module adoption path from locally installed first-party Maven artifacts rather than reactor classes.

**Architecture:** An excluded standalone Maven project owns only a minimal host layout, runtime assembly, bootstrap administrator, and verification tests. A root script first installs the starter artifacts to the local Maven repository, then invokes the consumer POM independently for normal and production verification.

**Tech Stack:** Java 25, Maven 4.0.0-rc-6, Spring Boot 4.1.0, Vaadin Flow 25.2.5, PostgreSQL Testcontainers, Playwright, Bash.

---

## File Structure

- Create `verification/standalone-consumer/pom.xml`: independent consumer POM with first-party coordinates and no root parent.
- Create `verification/standalone-consumer/src/main/...`: minimal consumer application, layout, bootstrap, configuration, and YAML.
- Create `verification/standalone-consumer/src/test/...`: context and browser proof of authentication and `/orders` navigation.
- Create `scripts/verify-standalone-consumer.sh`: installs first-party artifacts then runs normal and production consumer verification.
- Modify `docs/en/extension-guide.md`, `docs/zh-CN/extension-guide.md`, and `docs/en/release-guide.md`: document the local-install validation path.
- Modify `docs/superpowers/specs/2026-08-09-product-roadmap-design.md`: record this release-readiness acceptance milestone.

### Task 1: Create the independent consumer boundary

- [x] **Step 1: Add the standalone POM and assert it has no reactor parent**

Declare Spring Boot 4.1.0 as its parent, Java 25 and Vaadin 25.2.5 properties,
and first-party dependencies using only `0.1.0-SNAPSHOT` Maven coordinates.
Use a focused test that reads the POM and rejects a root-parent reference or
`admin-reference-app` dependency.

- [x] **Step 2: Implement the smallest host assembly**

Create a consumer `@SpringBootApplication`, minimal `@Layout` with
`@Uses(OrdersView.class)`, `AdminHostLayout`, entity scanning, JPA/security/
error configuration imports, local development configuration, and a bootstrap
runner that synchronizes module permissions then grants them to the initial
administrator.

- [x] **Step 3: Run standalone context verification from the local repository**

Run `./mvnw -B -ntp install -DskipTests`, then
`./mvnw -B -ntp -f verification/standalone-consumer/pom.xml test`. Confirm the
consumer resolves first-party JARs from the local repository and starts with no
reference-app classes.

### Task 2: Prove real consumer navigation and production bundling

- [x] **Step 1: Add a failing browser test**

Start the standalone host against PostgreSQL Testcontainers, use its own
`admin`/`change-me` account, and assert a login followed by `/orders` renders
the externally supplied orders title and deterministic rows.

- [x] **Step 2: Implement only the host behavior required by the test**

Keep the consumer layout and bootstrap flow minimal. Do not add a second shell,
business service, host PermissionCatalog, or direct route annotation to the
orders view.

- [x] **Step 3: Run normal and production consumer verification**

Run the standalone POM's normal verification and
`-Pproduction package -DskipTests`; confirm the production bundle includes the
dynamically registered orders view through the host's `@Uses` anchor.

### Task 3: Make the acceptance path repeatable and documented

- [x] **Step 1: Add the root verification script**

The script must fail fast, install the root reactor, run standalone normal
verification, then production packaging. It must use the standalone POM path,
not root-reactor flags or the reference app.

- [x] **Step 2: Document the local-consumer workflow**

Add one concise section to the English and Chinese extension guides and the
release guide: prerequisites, script command, what it proves, and its
non-publication boundary. Update the product roadmap to list this as the next
release-readiness deliverable.

- [x] **Step 3: Verify the script and documentation links**

Run `./scripts/verify-standalone-consumer.sh`, `git diff --check`, and the
repository documentation-link check. Mark this plan complete only after all
commands succeed.
