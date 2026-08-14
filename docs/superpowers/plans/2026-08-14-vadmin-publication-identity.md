# VAdmin Publication Identity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename the pre-publication project to VAdmin and make the root Maven
build an explicit library/starter build that imports the Spring Boot BOM rather
than inheriting `spring-boot-starter-parent`.

**Architecture:** The root POM becomes the stable VAdmin build parent and
publication descriptor. Reactor directory and artifact names migrate to the
`vadmin-` prefix first; all Java package roots then move atomically from
`io.github.vaadinadminstarter` to `io.github.youngledo.vadmin`. Current docs,
runtime identity strings, and deployment assets follow the same identity.

**Tech Stack:** Java 25, Maven 4 RC6, Spring Boot 4.1.0 BOM, Vaadin Flow
25.2.5, Spring Security, JPA/Flyway, PostgreSQL, Playwright.

**Spec:** `docs/superpowers/specs/2026-08-14-vadmin-publication-identity-design.md`

## Global Constraints

- Product name is `VAdmin`; GitHub identity is `youngledo/vadmin`.
- Maven group is exactly `io.github.youngledo`; public artifact IDs use the
  `vadmin-` prefix.
- Java package root is exactly `io.github.youngledo.vadmin`.
- Root `pom.xml` has no parent and imports
  `org.springframework.boot:spring-boot-dependencies:${spring-boot.version}`.
- Do not retain old coordinates, package aliases, relocation POMs, or duplicate
  module trees; this is a pre-publication breaking rename.
- Preserve Java 25, Spring Boot 4.1.0, Vaadin Flow 25.2.5, the current public
  behavior, `AdminModule` contract, routes, permissions, and runtime scope.
- Historical records under `docs/superpowers/**` are not rewritten.

---

### Task 1: Establish The Independent VAdmin Root Build

**Files:**
- Modify: root `pom.xml` and every current reactor child `pom.xml`
- Create: `admin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/build/RootPomIdentityTest.java`

**Interfaces:** The root reactor exposes `io.github.youngledo:vadmin` and has
no POM parent. Dependency management imports the Spring Boot BOM. The root
plugin management supplies `org.springframework.boot:spring-boot-maven-plugin`
at `${spring-boot.version}` so the reference application can retain its plugin
declaration without inheriting the Boot parent.

- [ ] **Step 1: Write the failing root-build contract test**

Create `RootPomIdentityTest` using `DocumentBuilderFactory`. Resolve the root
with `Path.of("..").toAbsolutePath().normalize().resolve("pom.xml")`. Assert
the root project has direct `groupId` `io.github.youngledo`, direct `artifactId`
`vadmin`, no direct `parent` element, a dependency-management entry for
`spring-boot-dependencies` with type `pom` and scope `import`, and plugin
management entry `spring-boot-maven-plugin` with version `${spring-boot.version}`.

```java
assertThat(directText(project, "groupId")).isEqualTo("io.github.youngledo");
assertThat(directText(project, "artifactId")).isEqualTo("vadmin");
assertThat(directChildren(project, "parent")).isEmpty();
assertThat(dependency("spring-boot-dependencies").getTextContent())
        .contains("org.springframework.boot", "pom", "import");
```

- [ ] **Step 2: Verify the test is red**

Run:

```bash
./mvnw -B -ntp -pl :admin-contracts -am test \
  -Dtest=RootPomIdentityTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: FAIL because the root still has `spring-boot-starter-parent`, old
coordinates, and no imported Spring Boot BOM.

- [ ] **Step 3: Replace inherited Boot build configuration with explicit management**

In root `pom.xml`:

1. Delete the complete `<parent>` block.
2. Set the root coordinates to `io.github.youngledo:vadmin`.
3. Add Maven Central metadata: name `VAdmin`, the Java/Vaadin Flow baseline
   description, `https://github.com/youngledo/vadmin`, Apache-2.0 license,
   developer ID/name `youngledo`, and SCM connection, developer connection,
   URL, and `HEAD` tag for `youngledo/vadmin`.
4. Add Spring Boot's BOM as the first imported dependency-management POM,
   before the Testcontainers BOM.
5. Add the Spring Boot Maven plugin with version `${spring-boot.version}` to
   root plugin management. Keep the explicit compiler, Surefire, and Failsafe
   settings unchanged.

Update every child POM that currently names the root as parent to parent
`io.github.youngledo:vadmin:0.1.0-SNAPSHOT`; this includes the contracts,
platform, Flow, Spring aggregator, and reference-app POMs. Leave all child
artifact IDs and the Spring-child parent artifact `admin-spring` unchanged in
this task. Do not add `spring-boot-starter-parent` to any child.

- [ ] **Step 4: Verify the independent root build**

Run:

```bash
./mvnw -B -ntp -pl :admin-contracts -am test \
  -Dtest=RootPomIdentityTest -Dsurefire.failIfNoSpecifiedTests=false
./mvnw -B -ntp help:effective-pom -Doutput=target/vadmin-effective-pom.xml
rg -n "spring-boot-starter-parent" target/vadmin-effective-pom.xml
```

Expected: the test passes; the effective root POM contains no Boot parent.
The final `rg` exits with status 1 because there is no inherited parent.

- [ ] **Step 5: Commit the root build boundary**

```bash
git add pom.xml admin-*/pom.xml admin-spring/*/pom.xml admin-reference-app/pom.xml
git add admin-contracts/src/test/java
git commit -m "build: establish independent VAdmin root"
```

### Task 2: Migrate Reactor Directories And Maven Coordinates

**Files:**
- Rename: `admin-contracts/` to `vadmin-contracts/`, `admin-platform/` to
  `vadmin-platform/`, `admin-flow/` to `vadmin-flow/`, `admin-spring/` to
  `vadmin-spring/`, and `admin-reference-app/` to `vadmin-reference-app/`
- Rename under `vadmin-spring/`: `admin-spring-security/`, `admin-spring-jpa/`,
  `admin-spring-boot/`, `admin-spring-flow/`, and `admin-spring-starter/` to
  their matching `vadmin-spring-*` names
- Modify: every renamed `pom.xml`, root `pom.xml`, `.gitignore`, `.dockerignore`,
  `Dockerfile`
- Create: `vadmin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/build/ReactorCoordinateTest.java`

**Interfaces:** The reactor consists of `vadmin`, `vadmin-contracts`,
`vadmin-platform`, `vadmin-flow`, `vadmin-spring`,
`vadmin-spring-security`, `vadmin-spring-jpa`, `vadmin-spring-boot`,
`vadmin-spring-flow`, `vadmin-spring-boot-starter`, and
`vadmin-reference-app`. Internal dependencies use group
`io.github.youngledo` and these exact artifact IDs.

- [ ] **Step 1: Write the failing reactor-coordinate test**

Create `ReactorCoordinateTest` beside `RootPomIdentityTest`. It parses every
reactor POM from the current root and asserts the exact artifact sequence
above, parent group `io.github.youngledo`, no `admin-` artifact IDs, and no
`io.github.vaadinadminstarter` dependency group.

```java
assertThat(artifactIds).containsExactly(
        "vadmin", "vadmin-contracts", "vadmin-platform", "vadmin-flow",
        "vadmin-spring", "vadmin-spring-security", "vadmin-spring-jpa",
        "vadmin-spring-boot", "vadmin-spring-flow",
        "vadmin-spring-boot-starter", "vadmin-reference-app");
assertThat(allPomContent).doesNotContain("io.github.vaadinadminstarter", "<artifactId>admin-");
```

- [ ] **Step 2: Verify the test is red**

Run:

```bash
./mvnw -B -ntp -pl :admin-contracts -am test \
  -Dtest=ReactorCoordinateTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: FAIL because the current module IDs, directories, and internal
coordinates retain the `admin-` naming.

- [ ] **Step 3: Rename modules and update all POM relationships**

Use `git mv` for the directories listed above. Update root and Spring
aggregator `<subprojects>` entries to the new directory names. Set every POM
parent and internal dependency group to `io.github.youngledo`, then update
artifact IDs using this mapping:

| Old | New |
| --- | --- |
| `vaadin-admin-starter` | `vadmin` |
| `admin-contracts` | `vadmin-contracts` |
| `admin-platform` | `vadmin-platform` |
| `admin-flow` | `vadmin-flow` |
| `admin-spring` | `vadmin-spring` |
| `admin-spring-security` | `vadmin-spring-security` |
| `admin-spring-jpa` | `vadmin-spring-jpa` |
| `admin-spring-boot` | `vadmin-spring-boot` |
| `admin-spring-flow` | `vadmin-spring-flow` |
| `admin-spring-starter` | `vadmin-spring-boot-starter` |
| `admin-reference-app` | `vadmin-reference-app` |

Update `.gitignore` and `.dockerignore` paths to `vadmin-reference-app`.
Update Dockerfile build selection and JAR copy source to
`vadmin-reference-app`; the JAR is
`vadmin-reference-app-0.1.0-SNAPSHOT.jar`.

- [ ] **Step 4: Verify coordinate and reactor consistency**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-spring-boot-starter -am test
./mvnw -B -ntp -pl :vadmin-reference-app -am package -DskipTests
```

Expected: both commands succeed with only the new reactor artifacts selected.

- [ ] **Step 5: Commit the coordinate migration**

```bash
git add -A
git commit -m "refactor: rename reactor artifacts to VAdmin"
```

### Task 3: Move The Java Namespace And Runtime Metadata

**Files:**
- Rename in every `vadmin-*` module: `src/main/java/io/github/vaadinadminstarter/`
  and `src/test/java/io/github/vaadinadminstarter/` to
  `src/*/java/io/github/youngledo/vadmin/`
- Modify: all Java sources and tests, Flow service-loader file under
  `vadmin-flow/src/main/resources/META-INF/services/`, both Spring
  `AutoConfiguration.imports` files, `Application.java`, architecture tests,
  test fixtures, and resource references containing fully-qualified class names
- Create: `vadmin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/build/NamespaceMigrationTest.java`

**Interfaces:** Application component scanning, Vaadin package scanning, JPA
entity scanning, Spring auto-configuration import names, service providers, and
all public Java classes use `io.github.youngledo.vadmin` with their existing
suffixes. No class keeps the old package root.

- [ ] **Step 1: Write the failing namespace migration test**

Create `NamespaceMigrationTest` in the contracts test tree. It walks all
non-generated `src/main/java`, `src/test/java`, and `src/main/resources` files
below the root, excludes its own old-namespace negative assertion, and fails
when production source/resource content contains
`io.github.vaadinadminstarter`. It also asserts that the reference application
source contains `@SpringBootApplication(scanBasePackages =
"io.github.youngledo.vadmin")`.

```java
assertThat(productionFiles).allSatisfy(file ->
        assertThat(Files.readString(file)).doesNotContain("io.github.vaadinadminstarter"));
assertThat(applicationSource).contains("io.github.youngledo.vadmin");
```

- [ ] **Step 2: Verify the test is red**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-contracts -am test \
  -Dtest=NamespaceMigrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: FAIL with old package references reported from production sources.

- [ ] **Step 3: Perform the path-aware package migration**

For each module source root, use `git mv` to move
`io/github/vaadinadminstarter` to `io/github/youngledo/vadmin`. Rewrite Java
`package` and `import` declarations, annotation package arrays, reflection
class-name strings, test source ownership assertions, service provider class
names, and Spring auto-configuration class names from
`io.github.vaadinadminstarter` to `io.github.youngledo.vadmin`.

Preserve all package suffixes and class names. In the renamed reference app,
update the following scanning declarations explicitly:

```java
@SpringBootApplication(scanBasePackages = "io.github.youngledo.vadmin")
@EnableVaadin({
        "io.github.youngledo.vadmin.app",
        "io.github.youngledo.vadmin.flow.error",
        "io.github.youngledo.vadmin.starter",
        "io.github.youngledo.vadmin.springsecurity.ui"
})
@EntityScan(basePackages = "io.github.youngledo.vadmin.springjpa")
```

- [ ] **Step 4: Verify the namespace migration**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-reference-app -am test
rg -n "io\.github\.vaadinadminstarter" \
  vadmin-contracts vadmin-platform vadmin-flow vadmin-spring vadmin-reference-app \
  --glob '!**/target/**' --glob '!**/NamespaceMigrationTest.java'
```

Expected: tests pass. The scan exits with status 1 because no old namespace
remains in module source, test, or resource files.

- [ ] **Step 5: Commit the Java namespace migration**

```bash
git add -A
git commit -m "refactor: move Java packages to VAdmin namespace"
```

### Task 4: Apply The VAdmin Product Identity To Runtime And Documentation

**Files:**
- Modify: `README.md`, `docs/requirements.md`, `docs/contributing.md`,
  `docs/deployment.md`, `docs/en/{architecture,quick-start,extension-guide,release-guide,theme-tokens}.md`,
  `docs/en/appearance-profiles.md`,
  `docs/zh-CN/{architecture,quick-start,extension-guide,release-guide,appearance-profiles}.md`,
  `docs/security.md`,
  `vadmin-reference-app/src/main/resources/application.yaml`, `Dockerfile`,
  `docker-compose.yml`, `.env.example`, `vadmin-reference-app` browser tests,
  `vadmin-spring-boot` Problem Details mapper, and Keycloak test fixture
- Rename: `docs/diagrams/vaadin-admin-starter-architecture-options.drawio` to
  `docs/diagrams/vadmin-architecture-options.drawio`
- Modify: renamed `CurrentDocumentationTest` in
  `vadmin-reference-app/src/test/java/io/github/youngledo/vadmin/app/`

**Interfaces:** The visible product name is `VAdmin`; users depend on
`io.github.youngledo:vadmin-spring-boot-starter`; the reference application
name, Docker image examples, default database naming, Problem Details type URN,
and Keycloak fixture description use `vadmin`.

- [ ] **Step 1: Update the documentation identity guard first**

Rename and edit `CurrentDocumentationTest` to require `VAdmin`,
`io.github.youngledo`, and `vadmin-spring-boot-starter` in every current guide.
It must reject `Vaadin Admin Starter`, `vaadin-admin-starter`,
`io.github.vaadinadminstarter`, and `admin-spring-starter`, while continuing
to reject retired business samples and required consumer shell assembly.

```java
assertThat(content)
        .contains("VAdmin", "io.github.youngledo", "vadmin-spring-boot-starter")
        .doesNotContain("Vaadin Admin Starter", "vaadin-admin-starter",
                "io.github.vaadinadminstarter", "admin-spring-starter");
```

- [ ] **Step 2: Verify the documentation guard is red**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-reference-app -am test \
  -Dtest=CurrentDocumentationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: FAIL and list current guides that still contain the old product
identity and starter coordinate.

- [ ] **Step 3: Replace public and runtime identity strings**

Update all current English and Chinese adoption guides to use the VAdmin name,
new group ID, new starter artifact, and `vadmin` version property. Rename the
diagram and update its XML title. Keep the Vaadin trademark disclaimer but make
VAdmin the project subject.

Update runtime/deployment identity consistently:

- Default shell product label and browser assertions: `VAdmin`.
- `spring.application.name`: `vadmin`.
- Problem Details type prefix: `urn:vadmin:error:`.
- Docker image examples: `vadmin:local` and `vadmin:0.1.0-rc`.
- Compose network/volume project naming and default database name:
  add top-level `name: vadmin`, name the PostgreSQL volume
  `vadmin-postgres-data`, and set `POSTGRES_DB=vadmin`.
- Keycloak test realm display name: `VAdmin OIDC integration test`.

Do not rename environment variable names such as `DATABASE_URL` and
`APP_BOOTSTRAP_PASSWORD`; they are generic consumer configuration contracts.

- [ ] **Step 4: Verify public identity and targeted UI behavior**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-reference-app,:vadmin-spring-boot-starter -am test \
  -Dtest=CurrentDocumentationTest,DefaultShellTranslationTest \
  -Dsurefire.failIfNoSpecifiedTests=false
docker compose --env-file .env.example config
rg -n "Vaadin Admin Starter|vaadin-admin-starter|io\.github\.vaadinadminstarter|admin-spring-starter" \
  README.md docs vadmin-* Dockerfile docker-compose.yml .env.example \
  --glob '!docs/superpowers/**' --glob '!**/target/**' \
  --glob '!**/CurrentDocumentationTest.java' \
  --glob '!**/NamespaceMigrationTest.java'
```

Expected: targeted tests and Compose rendering pass. The identity scan exits
with status 1 because current product files no longer contain old identifiers.

- [ ] **Step 5: Commit the public identity**

```bash
git add -A
git commit -m "docs: publish the VAdmin identity"
```

### Task 5: Verify The Complete Renamed Distribution

**Files:**
- Modify only if verification exposes a migration defect in a file owned by the
  preceding tasks

**Interfaces:** The final reactor, starter coordinate, generated frontend, and
container build all identify as VAdmin and preserve the existing administrator
experience.

- [ ] **Step 1: Verify the published starter dependency contract**

Run:

```bash
./mvnw -B -ntp -pl :vadmin-spring-boot-starter -am test
./mvnw -B -ntp -pl :vadmin-reference-app dependency:tree \
  -Dincludes=io.github.youngledo
```

Expected: starter tests pass and the dependency tree contains only
`io.github.youngledo:vadmin-*` project coordinates.

- [ ] **Step 2: Run full normal and production verification**

Run:

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
```

Expected: all unit, integration, browser, architecture, i18n, and production
frontend tests pass.

- [ ] **Step 3: Validate container distribution and final scope**

Run:

```bash
docker compose --env-file .env.example config
docker build -t vadmin:0.1.0-rc .
git diff --check
rg -n "io\.github\.vaadinadminstarter|vaadin-admin-starter|Vaadin Admin Starter|admin-spring-starter" \
  --glob '!docs/superpowers/**' --glob '!**/target/**' \
  --glob '!**/CurrentDocumentationTest.java' \
  --glob '!**/NamespaceMigrationTest.java' .
git status --short
```

Expected: Compose and Docker build succeed, whitespace check is clean, the
identity scan exits with status 1, and only intended tracked changes remain
before the final commit. If Docker cannot retrieve an uncached base image,
record the external registry failure separately; do not classify it as a
source failure.

- [ ] **Step 4: Commit final verification adjustments**

```bash
git add -A
git commit -m "test: verify VAdmin publication identity"
```
