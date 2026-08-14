# Quick Start

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

[简体中文](../zh-CN/quick-start.md) | English

`vadmin-spring-boot-starter` is the normal adoption dependency. It supplies the
default Flow shell and theme, local login, system administration, module
assembly, and Spring adapters. Your application supplies its datasource,
Flyway migrations, and business modules.

## Prerequisites

- JDK 25.
- Docker Desktop for PostgreSQL or the full Compose stack.
- Access to Maven Central. The repository includes a Maven 4 RC6 wrapper.

## Add The Starter

Add the single first-party dependency to a Spring Boot application:

```xml
<dependency>
  <groupId>io.github.youngledo</groupId>
  <artifactId>vadmin-spring-boot-starter</artifactId>
  <version>${vadmin.version}</version>
</dependency>
```

Ensure the application's existing `@EnableVaadin` scan includes the Starter UI
packages. This is Vaadin's route-discovery requirement, not custom shell
composition:

```java
@EnableVaadin({"com.example.inventory", "io.github.youngledo.vadmin.starter",
        "io.github.youngledo.vadmin.springsecurity.ui"})
@SpringBootApplication
public class InventoryApplication {
}
```

For `spring-boot:run` development mode, add Vaadin's optional development
server directly to the consumer as well. It is intentionally not transitive
from the starter and is excluded from the production artifact:

```xml
<dependency>
  <groupId>com.vaadin</groupId>
  <artifactId>vaadin-dev</artifactId>
  <version>${vaadin.version}</version>
  <optional>true</optional>
</dependency>
```

Configure PostgreSQL and Flyway in the consumer application. Keep its migration
location separate from the starter's migrations and never rewrite a migration
that has reached an environment:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: inventory
    password: change-me
  flyway:
    locations: classpath:db/migration
```

Start the application. On an empty database, provide a strong
`APP_BOOTSTRAP_PASSWORD`; the initial `admin` account receives that password.
Changing the variable later does not reset an existing account.

```bash
APP_BOOTSTRAP_PASSWORD='replace-this-secret' ./mvnw -B -ntp spring-boot:run
```

Open `http://localhost:8080`. The starter supplies the home page, navigation,
Users, Roles, Permissions, Audit, `zh-CN` and `en-US`, light/dark mode, and
the selected visual language and density. A normal consumer does not define a
Flow shell, `AdminHostLayout`, `AppShellConfigurator`, or `@Theme`.

## Run The Reference Application

The repository reference application is a thin starter consumer and acceptance
fixture. Run it locally with PostgreSQL:

```bash
cp .env.example .env
docker compose --env-file .env up -d postgres
set -a
. ./.env
set +a
export DATABASE_URL="jdbc:postgresql://localhost:5432/${POSTGRES_DB}"
export DATABASE_USERNAME="${POSTGRES_USER}"
export DATABASE_PASSWORD="${POSTGRES_PASSWORD}"
SPRING_PROFILES_ACTIVE=development \
  ./mvnw -B -ntp -pl :vadmin-reference-app -am spring-boot:run
```

For the complete container stack:

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

The development profile uses the local empty-database password only. Use a
protected `APP_BOOTSTRAP_PASSWORD` for a production-like database. The
production artifact excludes the Vaadin development server:

```bash
./mvnw -B -ntp -Pproduction -pl :vadmin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=prod \
  java -jar vadmin-reference-app/target/vadmin-reference-app-0.1.0-SNAPSHOT.jar
```

Removing local demonstration data deletes Compose volumes. Confirm that no
data must be retained before running:

```bash
docker compose down --volumes
```

To add a business page, follow the [Extension Guide](extension-guide.md).
Only a deliberate full shell replacement needs a consumer `AdminHostLayout`
and `AppShellConfigurator`.
