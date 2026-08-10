# Quick Start

English | [简体中文](../zh-CN/quick-start.md)

Vaadin Admin Starter is a Vaadin Flow starter for Java teams building internal
business applications. The first release uses Spring Boot as its runtime and
PostgreSQL as its database.

## Prerequisites

- JDK 25.
- Docker Desktop, for PostgreSQL or the complete Compose stack.
- Network access to Maven Central. The project includes a Maven 4 RC6 Wrapper.

## Build And Run From Source

First, prepare local environment variables. The example file contains only
public values intended for local development:

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
  ./mvnw -B -ntp -pl admin-reference-app -am spring-boot:run
```

In development mode, open `http://localhost:8080` and sign in with `admin` /
`change-me`. This startup path keeps the Vaadin development server available
for local iteration after changes to Java Flow views, `admin-theme` CSS, or
theme configuration. The development-mode password is only suitable for an
empty database on the local machine and must not be used in a shared
environment.

The reference application uses `ApplicationShell` to register the Flow theme
named `admin-theme`. After signing in, the current-user menu can switch between
light and dark modes. The choice is retained only for the current Vaadin
session; a new session returns to light mode.

For a production build with an externalized bootstrap password, use these
commands instead:

```bash
./mvnw -B -ntp -Pproduction -pl admin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=prod java -jar admin-reference-app/target/admin-reference-app-0.1.0-SNAPSHOT.jar
```

Open `http://localhost:8080` and sign in as `admin` with the value of
`APP_BOOTSTRAP_PASSWORD`. Before initializing an empty production database,
set this variable to a strong value through a protected secret-management
mechanism. `local-admin-change-me` is only a local example. The application
reads `APP_BOOTSTRAP_PASSWORD` only while creating the first `admin` account;
changing the variable later does not change or reset an existing password. The
reference application does not currently provide a password-management
operation.

The executable JAR does not include the Vaadin development server by default,
so do not start it with the `development` profile. To use Flow development
mode, run the application through Maven with the `vaadin-dev` dependency
available; do not use development mode as the runtime for a deployment JAR.

## Configure Optional OIDC Login

Local-password login remains enabled unless the consuming application changes
its own deployment policy. To add a provider-neutral OpenID Connect login, use
Spring Security's standard issuer discovery and authorization-code client
configuration. The following example contains no literal client secret; obtain
and protect any real secret through the deployment's secret-management
mechanism.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          corporate:
            client-id: ${OIDC_CLIENT_ID}
            client-secret: ${OIDC_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            scope: openid,profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          corporate:
            issuer-uri: https://issuer.example.com

vaadin-admin:
  oidc:
    registration-id: corporate
```

Register the redirect URI resolved for the deployed application, for example
`https://admin.example.com/login/oauth2/code/corporate`. The configured
`vaadin-admin.oidc.registration-id` must name an existing Spring Security
client registration. When no client registration is configured, the application
runs with local-password login only. When a client registration is configured,
the application requires exactly one `ExternalIdentityMapper` and a matching
configured registration ID; a missing or ambiguous mapper, or a mismatched
registration ID, fails application startup rather than silently disabling
external login. With a valid configuration, the login page offers an external
sign-in action at `/oauth2/authorization/corporate` alongside local login.

The mapper is the authorization boundary between an authenticated external
subject and this application's existing local account. It must use the stable
issuer and subject pair, and return a `CurrentUser` only for an existing,
enabled local account. This example leaves the persistence of external links
to the consuming application:

```java
@Bean
ExternalIdentityMapper externalIdentityMapper(ExternalIdentityLinks links,
                                              LocalUserAccountLookup accounts) {
    return identity -> links.findByIssuerAndSubject(identity.issuer(), identity.subject())
            .flatMap(link -> accounts.findByUserId(link.localUserId()))
            .filter(LocalUserAccount::enabled)
            .map(account -> new CurrentUser(account.userId(), account.username(),
                    account.permissions(), account.authVersion()));
}
```

A successful OIDC authentication grants no role or permission by itself. A
missing mapper, an unmatched external identity, or a disabled local account
denies access safely. Group-to-role mapping, automatic provisioning or
deprovisioning, SCIM, MFA, SAML, LDAP, tenant selection, and data-scope policy
are application extensions, not starter behavior.

Keycloak is used only by the project's integration tests. A mainland-China,
global, or self-hosted identity provider follows this same path when it
supports standard OpenID Connect discovery and authorization-code login; no
provider SDK is required by the starter.

## Start The Complete Stack With Compose

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

Compose waits for the PostgreSQL health check before starting the application.
On its first startup against an empty database, the application creates the
`admin` user and assigns `APP_BOOTSTRAP_PASSWORD` as its password. Stopping the
services while retaining the named volume does not recreate the administrator
or reset its password.

Removing local demonstration data deletes the database and attachments. Ensure
that there is no data to retain before running:

```bash
docker compose down --volumes
```
