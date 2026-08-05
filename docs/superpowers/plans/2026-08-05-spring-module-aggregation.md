# Spring Module Aggregation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Group all Spring-specific modules under an `admin-spring` Maven parent without changing their Maven coordinates, Java packages, or runtime behavior.

**Architecture:** Add `admin-spring` as a Maven 4 `pom`-packaging parent and reactor aggregator. Move the existing Spring module directories below it, change only their immediate POM parent to `admin-spring`, and retain the root POM as the common version and plugin-management parent. The framework-neutral modules and the reference application remain direct root subprojects.

**Tech Stack:** Java 25, Maven 4 RC6, Spring Boot 4.1.0, Vaadin Flow 25.2.5, Testcontainers PostgreSQL, Docker Compose.

---

## File Structure

| Path | Responsibility |
|---|---|
| `pom.xml` | Root Maven 4 reactor and shared dependency/plugin management; aggregates `admin-spring` once. |
| `admin-spring/pom.xml` | Maven-only Spring adapter parent and aggregator. |
| `admin-spring/admin-spring-security/pom.xml` | Existing authentication adapter, now a child of `admin-spring`. |
| `admin-spring/admin-spring-jpa/pom.xml` | Existing persistence adapter, now a child of `admin-spring`. |
| `admin-spring/admin-spring-boot/pom.xml` | Existing Boot wiring adapter, now a child of `admin-spring`. |
| `docs/architecture.md` | Documents `admin-spring` as the adapter-family parent and preserves leaf responsibilities. |

Historical documents under `docs/superpowers/plans/2026-08-04-vaadin-admin-starter.md` describe the original implementation and must not be rewritten to erase that history.

### Task 1: Move Spring Modules Under A Maven Parent

**Files:**
- Create: `admin-spring/pom.xml`
- Move: `admin-spring-security/` to `admin-spring/admin-spring-security/`
- Move: `admin-spring-jpa/` to `admin-spring/admin-spring-jpa/`
- Move: `admin-spring-boot/` to `admin-spring/admin-spring-boot/`
- Modify: `pom.xml:31-40`
- Modify: `admin-spring/admin-spring-security/pom.xml:4-9`
- Modify: `admin-spring/admin-spring-jpa/pom.xml:4-9`
- Modify: `admin-spring/admin-spring-boot/pom.xml:4-9`

- [ ] **Step 1: Prove that the new aggregator is absent before the migration**

Run:

```bash
./mvnw -B -ntp -pl :admin-spring validate
```

Expected: Maven fails while selecting `:admin-spring`, because the parent module does not exist yet.

- [ ] **Step 2: Move the three Spring modules with Git-aware moves**

Run:

```bash
mkdir -p admin-spring
git mv admin-spring-security admin-spring/admin-spring-security
git mv admin-spring-jpa admin-spring/admin-spring-jpa
git mv admin-spring-boot admin-spring/admin-spring-boot
```

Expected: `git status --short` reports three directory renames rather than unrelated deletes and additions.

- [ ] **Step 3: Add the Maven-only Spring parent and update the root reactor**

Create `admin-spring/pom.xml` with this complete content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.1.0</modelVersion>

    <parent>
        <groupId>io.github.vaadinadminstarter</groupId>
        <artifactId>vaadin-admin-starter</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>admin-spring</artifactId>
    <packaging>pom</packaging>

    <subprojects>
        <subproject>admin-spring-security</subproject>
        <subproject>admin-spring-jpa</subproject>
        <subproject>admin-spring-boot</subproject>
    </subprojects>
</project>
```

Replace the three root Spring `subproject` entries with one entry:

```xml
<subproject>admin-spring</subproject>
```

Keep the root entries for `admin-contracts`, `admin-platform`, `admin-flow`, and `admin-reference-app` unchanged.

- [ ] **Step 4: Change each moved child POM to inherit from `admin-spring`**

In each of these three files, replace only the existing `<parent>` block with:

```xml
<parent>
    <groupId>io.github.vaadinadminstarter</groupId>
    <artifactId>admin-spring</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Do not change the child `artifactId`, dependencies, Java sources, resource files, or test files.

- [ ] **Step 5: Validate the new Spring reactor slice**

Run:

```bash
./mvnw -B -ntp -pl :admin-spring -am test
```

Expected: Maven builds `admin-contracts`, `admin-platform`, `admin-spring`, and all three Spring child modules; all unit and Testcontainers-backed tests pass.

- [ ] **Step 6: Commit the atomic Maven-layout migration**

Run:

```bash
git add pom.xml admin-spring
git commit -m "refactor: group Spring modules"
```

Expected: the commit contains only the root reactor edit, new aggregator POM, Git moves, and changed child parent blocks.

### Task 2: Document The Adapter Family Without Changing Compatibility Claims

**Files:**
- Modify: `docs/architecture.md:39-52`

- [ ] **Step 1: Add the parent module to the architecture module table**

Insert this row before the three Spring adapter rows:

```markdown
| `admin-spring` | Maven parent and reactor aggregator for Spring-specific adapters; no runtime code | root POM inheritance only |
```

Keep the `admin-spring-security`, `admin-spring-jpa`, and `admin-spring-boot` rows and their responsibilities unchanged. Add one sentence after the table:

```markdown
`admin-spring` is an organizational Maven parent only; consumers continue to depend on the three leaf artifacts directly.
```

- [ ] **Step 2: Verify that published leaf coordinates did not change**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -am test
```

Expected: the reference application resolves `admin-spring-security`, `admin-spring-jpa`, and `admin-spring-boot` using their unchanged coordinates and completes its unit-test phase successfully.

- [ ] **Step 3: Check the path migration and documentation patch**

Run:

```bash
find admin-spring -maxdepth 2 -name pom.xml -print | sort
git diff --check
```

Expected: the output contains `admin-spring/pom.xml` plus the three child POM paths, and `git diff --check` produces no output.

- [ ] **Step 4: Commit documentation**

Run:

```bash
git add docs/architecture.md
git commit -m "docs: describe Spring adapter family"
```

Expected: the commit documents the organizational parent without changing compatibility claims.

### Task 3: Verify The Full Build And Production Packaging

**Files:**
- Modify: no additional source changes

- [ ] **Step 1: Run the complete development verification**

Run:

```bash
./mvnw -B -ntp verify
```

Expected: all reactor modules, integration tests, browser E2E tests, and architecture tests pass.

- [ ] **Step 2: Run the complete production verification**

Run:

```bash
./mvnw -B -ntp -Pproduction verify
```

Expected: production frontend generation and packaging complete successfully, including the Vaadin production runtime in the reference application JAR.

- [ ] **Step 3: Validate deployment configuration remains path-independent**

Run:

```bash
docker compose --env-file .env.example config
```

Expected: Docker Compose renders a valid configuration with database environment variables resolved and no references to the former Spring module paths.

- [ ] **Step 4: Inspect the final change set**

Run:

```bash
git status --short
git log --oneline -3
```

Expected: no uncommitted tracked source or documentation changes remain; the two migration commits are visible at the top of the history.
