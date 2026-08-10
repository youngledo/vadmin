# OIDC Integration Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an opt-in, provider-neutral OIDC login path that resolves an
authenticated external subject to an existing local `CurrentUser`, while
preserving password login and every established authorization boundary.

**Architecture:** `admin-contracts` defines a Spring-free `ExternalIdentity`
value and `ExternalIdentityMapper` SPI. `admin-spring-security` alone adapts
Spring Security OIDC authentication to that SPI, replaces the successful OIDC
principal with the existing local principal, and applies the current session
invalidation filter. The reference app supplies a deliberately explicit sample
mapper; Keycloak is restricted to integration tests.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Security OAuth2 Client, Vaadin
Flow 25, JUnit 5, Testcontainers Keycloak, Maven 4.

---

### Task 1: Add Framework-Neutral External Identity Contracts

**Files:**
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/auth/ExternalIdentity.java`
- Create: `admin-contracts/src/main/java/io/github/vaadinadminstarter/contracts/auth/ExternalIdentityMapper.java`
- Create: `admin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/auth/ExternalIdentityTest.java`
- Create: `admin-contracts/src/test/java/io/github/vaadinadminstarter/contracts/auth/ExternalIdentityMapperTest.java`

- [ ] **Step 1: Write failing identity validation tests**

```java
@Test
void requiresAnAbsoluteIssuerAndStableSubject() {
    assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExternalIdentity(URI.create("issuer"), "subject", null, null, Map.of()));
    assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExternalIdentity(URI.create("https://issuer.example"), " ", null, null, Map.of()));
}

@Test
void exposesAnImmutableClaimSnapshot() {
    var identity = new ExternalIdentity(URI.create("https://issuer.example"), "subject-42",
            "Ada", "ada@example.test", Map.of("groups", "operators"));
    assertThat(identity.claims()).containsEntry("groups", "operators");
    assertThatThrownBy(() -> identity.claims().put("role", "admin"))
            .isInstanceOf(UnsupportedOperationException.class);
}
```

- [ ] **Step 2: Run the focused test and observe the missing-type failure**

Run: `./mvnw -B -ntp -pl :admin-contracts test -Dtest=ExternalIdentityTest,ExternalIdentityMapperTest`

Expected: FAIL because `ExternalIdentity` and `ExternalIdentityMapper` do not
exist.

- [ ] **Step 3: Implement the immutable contract**

```java
public record ExternalIdentity(URI issuer, String subject, String displayName,
        String email, Map<String, String> claims) {
    public ExternalIdentity {
        if (issuer == null || !issuer.isAbsolute()) throw new IllegalArgumentException("issuer must be absolute");
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("subject must not be blank");
        claims = Map.copyOf(claims);
    }
}

@FunctionalInterface
public interface ExternalIdentityMapper {
    Optional<CurrentUser> map(ExternalIdentity identity);
}
```

Do not add Spring annotations, provider names, role-mapping methods, or account
creation methods.

- [ ] **Step 4: Verify the contracts and dependency purity**

Run: `./mvnw -B -ntp -pl :admin-contracts test`

Expected: PASS; contract tests contain no Spring or Flow imports.

- [ ] **Step 5: Commit the neutral contract**

```bash
git add admin-contracts
git commit -m "feat: add external identity mapping contract"
```

### Task 2: Adapt OIDC Authentication To The Existing Local Principal

**Files:**
- Modify: `admin-spring/admin-spring-security/pom.xml`
- Modify: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/auth/LocalUserPrincipal.java`
- Create: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/auth/OidcExternalIdentityFactory.java`
- Create: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/auth/OidcLocalUserAuthenticationSuccessHandler.java`
- Create: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/auth/OidcAccessDeniedFailureHandler.java`
- Create: `admin-spring/admin-spring-security/src/test/java/io/github/vaadinadminstarter/springsecurity/auth/OidcExternalIdentityFactoryTest.java`
- Create: `admin-spring/admin-spring-security/src/test/java/io/github/vaadinadminstarter/springsecurity/auth/OidcLocalUserAuthenticationSuccessHandlerTest.java`

- [ ] **Step 1: Add failing OIDC adapter tests**

```java
@Test
void normalizesIssuerSubjectAndStringClaimsFromAnOidcUser() {
    var identity = factory.from(oidcUser("https://issuer.example", "subject-42",
            Map.of("name", "Ada", "email", "ada@example.test", "department", "platform")));
    assertThat(identity.issuer()).isEqualTo(URI.create("https://issuer.example"));
    assertThat(identity.subject()).isEqualTo("subject-42");
    assertThat(identity.claims()).containsEntry("department", "platform");
}

@Test
void replacesTheOidcPrincipalOnlyAfterAMapperResolvesALocalUser() throws Exception {
    var handler = new OidcLocalUserAuthenticationSuccessHandler(identity -> Optional.of(currentUser("ada")));
    handler.onAuthenticationSuccess(request, response, oidcAuthentication());
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .isInstanceOf(LocalUserPrincipal.class);
}
```

- [ ] **Step 2: Run the focused test and observe the missing adapter failure**

Run: `./mvnw -B -ntp -pl :admin-spring-security -am test -Dtest=OidcExternalIdentityFactoryTest,OidcLocalUserAuthenticationSuccessHandlerTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because the OIDC adapter classes and OAuth2 Client dependency
are absent.

- [ ] **Step 3: Add the OAuth2 Client dependency and preserve the local principal model**

Add only Spring Boot's managed client starter:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Add a `LocalUserPrincipal(CurrentUser currentUser)` constructor that derives the
same authorities as its `LocalUserAccount` constructor. Do not expose OIDC
tokens from `LocalUserPrincipal` or `CurrentUserProvider`.

- [ ] **Step 4: Implement adaptation, resolution, and denial**

`OidcExternalIdentityFactory` reads `iss`, `sub`, `name`, and `email` from an
`OidcUser`; it includes only scalar string claims in the neutral snapshot.
`OidcLocalUserAuthenticationSuccessHandler` calls the mapper, rejects a missing
mapping, rejects a disabled resolved account through the existing local lookup,
then stores a `UsernamePasswordAuthenticationToken` with `LocalUserPrincipal`.
It saves the security context and redirects through the saved-request flow.
`OidcAccessDeniedFailureHandler` clears the context and returns to `/login` with
the existing localized access-denied parameter; it must not include provider
error descriptions in the redirect.

- [ ] **Step 5: Verify adapter behavior and regression coverage**

Run: `./mvnw -B -ntp -pl :admin-spring-security -am test`

Expected: PASS; existing password-principal and authentication-version tests
remain green.

- [ ] **Step 6: Commit the OIDC adapter**

```bash
git add admin-spring/admin-spring-security
git commit -m "feat: adapt OIDC subjects to local users"
```

### Task 3: Make Spring Security Configuration Opt-In And Preserve Local Login

**Files:**
- Modify: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/SpringSecurityConfiguration.java`
- Create: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/OidcLoginAvailability.java`
- Modify: `admin-spring/admin-spring-security/src/main/java/io/github/vaadinadminstarter/springsecurity/ui/LoginView.java`
- Modify: `admin-flow/src/main/resources/i18n/flow_en_US.properties`
- Modify: `admin-flow/src/main/resources/i18n/flow_zh_CN.properties`
- Create: `admin-spring/admin-spring-security/src/test/java/io/github/vaadinadminstarter/springsecurity/SpringSecurityConfigurationTest.java`
- Create: `admin-spring/admin-spring-security/src/test/java/io/github/vaadinadminstarter/springsecurity/ui/LoginViewTest.java`

- [ ] **Step 1: Write failing local/OIDC configuration tests**

```java
@Test
void keepsPasswordLoginWhenNoClientRegistrationExists() {
    contextRunner.run(context -> assertThat(context.getBean(OidcLoginAvailability.class).isAvailable()).isFalse());
}

@Test
void exposesAnExternalLoginActionOnlyWhenOidcIsConfigured() {
    var view = new LoginView(() -> true);
    assertThat(view.getElement().getText()).contains("Continue with single sign-on");
}
```

- [ ] **Step 2: Run the focused tests and observe the absent availability contract**

Run: `./mvnw -B -ntp -pl :admin-spring-security -am test -Dtest=SpringSecurityConfigurationTest,LoginViewTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because OIDC availability and the conditional login action do
not exist.

- [ ] **Step 3: Wire OAuth2 login only when a mapper and registration repository exist**

Use Spring's `ClientRegistrationRepository` and `ObjectProvider<ExternalIdentityMapper>`
to construct the OIDC path only when both are present. Keep
`DaoAuthenticationProvider`, `AuthenticationVersionFilter`, Vaadin security
configuration, CSRF, and `LoginView` configured in all cases. A configured
client with no mapper must fail application startup with an actionable message,
rather than grant an unmapped OIDC principal.

- [ ] **Step 4: Add the provider-neutral Flow login action**

Inject `OidcLoginAvailability` into `LoginView`. When true, render an
accessible `Anchor` to Spring Security's `/oauth2/authorization/{registrationId}`
route using a configured default registration id. Keep the local `LoginForm`.
Use only `flow.login.sso` and `flow.login.denied` i18n keys; do not display a
provider name, icon, token, or claim.

- [ ] **Step 5: Verify local and opt-in modes**

Run: `./mvnw -B -ntp -pl :admin-spring-security -am test`

Expected: PASS; no-client contexts have the exact current password-login
behavior, mapped OIDC contexts expose one external-login entry point, and
missing mappers fail closed.

- [ ] **Step 6: Commit configuration and login UI**

```bash
git add admin-spring/admin-spring-security admin-flow/src/main/resources/i18n
git commit -m "feat: add opt-in OIDC login entry point"
```

### Task 4: Add An Explicit Reference-Application Mapping Example

**Files:**
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/ApplicationConfiguration.java`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/auth/ConfiguredExternalIdentityMapper.java`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/auth/OidcIdentityLinkProperties.java`
- Modify: `admin-reference-app/src/main/resources/application.yml`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationConfigurationTest.java`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/auth/ConfiguredExternalIdentityMapperTest.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ArchitectureTest.java`

- [ ] **Step 1: Write failing mapping tests**

```java
@Test
void resolvesOnlyAnExplicitIssuerAndSubjectLink() {
    var mapper = new ConfiguredExternalIdentityMapper(List.of(
            new OidcIdentityLinkProperties.Link(URI.create("https://issuer.example"), "subject-42", "admin")), accountLookup);
    assertThat(mapper.map(identity("https://issuer.example", "subject-42"))).contains(currentUser("admin"));
    assertThat(mapper.map(identity("https://issuer.example", "other-subject"))).isEmpty();
}
```

- [ ] **Step 2: Run the focused test and observe the missing mapper failure**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test -Dtest=ConfiguredExternalIdentityMapperTest,ApplicationConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because the reference mapper and typed configuration are absent.

- [ ] **Step 3: Implement explicit configuration-only linking**

Define a typed `app.identity.oidc.links` list with an `issuer`, `subject`, and
`username` record for each link. Resolve it through
`LocalUserAccountLookup`; return empty for unknown, disabled, or invalid links.
Do not create accounts, inspect groups, or assign roles. Register the mapper
only when at least one link exists, so default reference-app startup remains
local-login-only.

- [ ] **Step 4: Enforce the intended application boundary**

Add an ArchUnit rule that reference-app authentication mapping depends on
`admin-contracts` ports and its own configuration only. It must not import
`OidcUser`, `OAuth2AuthenticationToken`, tokens, or the Flow layout.

- [ ] **Step 5: Verify the reference configuration**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test`

Expected: PASS; default application context has no OIDC mapper and explicit
links resolve only existing enabled local users.

- [ ] **Step 6: Commit the reference mapping**

```bash
git add admin-reference-app/src/main admin-reference-app/src/test
git commit -m "feat: add explicit external identity mapping example"
```

### Task 5: Verify A Real OIDC Provider Flow With Keycloak Testcontainers

**Files:**
- Modify: `admin-reference-app/pom.xml`
- Create: `admin-reference-app/src/test/resources/keycloak/realm-vaadin-admin.json`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/oidc/KeycloakOidcIT.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/BrowserE2EIT.java`

- [ ] **Step 1: Write the failing Keycloak integration test**

```java
@Test
void returnsToTheRequestedRouteAsTheMappedLocalUser() {
    browser.navigate("/users");
    browser.clickExternalLogin();
    keycloak.login("oidc-admin", "change-me");
    browser.assertRoute("/users");
    browser.assertVisibleText("admin");
}

@Test
void deniesAnUnmappedSubjectWithoutRenderingTokensOrClaims() {
    browser.startExternalLogin("unmapped-user", "change-me");
    browser.assertLoginDenial();
    browser.assertPageDoesNotContain("access_token", "id_token", "department");
}
```

- [ ] **Step 2: Run the integration test and observe missing Keycloak fixture failure**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify -Dit.test=KeycloakOidcIT -Dfailsafe.failIfNoSpecifiedTests=false`

Expected: FAIL because the Keycloak container, realm, and OIDC test profile do
not exist.

- [ ] **Step 3: Add test-scoped Keycloak only**

Use the existing Testcontainers core API with a test-scoped Keycloak
`GenericContainer`; do not add a provider-specific Keycloak library. Start it
from `KeycloakOidcIT`, import a realm defining one mapped and one unmapped test
subject, and supply dynamic issuer/client/redirect configuration through Spring
test properties. Do not add Keycloak to runtime dependencies, Docker Compose,
or production configuration.

- [ ] **Step 4: Cover logout and local-session invalidation**

Extend the integration test to confirm local logout invalidates the application
session and an authentication-version change rejects a previously mapped OIDC
session. Do not attempt provider back-channel logout in this phase.

- [ ] **Step 5: Verify OIDC and existing browser flows**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify`

Expected: PASS; existing 31 browser scenarios remain green and the Keycloak
fixture validates discovery, callback, mapping, denial, return URL, logout, and
session invalidation.

- [ ] **Step 6: Commit integration coverage**

```bash
git add admin-reference-app/pom.xml admin-reference-app/src/test
git commit -m "test: verify OIDC local-user mapping flow"
```

### Task 6: Document Consumer Configuration And Record Phase 4 Outcome

**Files:**
- Modify: `docs/en/quick-start.md`
- Modify: `docs/en/extension-guide.md`
- Modify: `docs/en/architecture.md`
- Modify: `docs/superpowers/specs/2026-08-09-product-roadmap-design.md`
- Modify: `docs/superpowers/specs/2026-08-10-oidc-integration-readiness-design.md`

- [ ] **Step 1: Document generic issuer configuration and mapping ownership**

Add a provider-neutral Spring configuration example using an issuer URI, client
registration, redirect URI, and the `ExternalIdentityMapper` bean. State that
Keycloak is test-only and that a compliant mainland-China, global, or
self-hosted OIDC provider follows the same path. Do not publish vendor SDK
steps or client secrets.

- [ ] **Step 2: Document the safe default and non-goals**

Explain that successful external authentication grants nothing until a mapper
resolves an existing enabled local user. Document explicit extension ownership
for group mapping, provisioning, SCIM, MFA, SAML, LDAP, tenants, and data
scope.

- [ ] **Step 3: Record verified architecture and roadmap status**

Update the architecture module table and product roadmap only after Task 5
passes. Mark Phase 4 as completed only when normal and production evidence
exists; otherwise leave the design status approved and record any explicit
deferral decision instead.

- [ ] **Step 4: Run release verification and documentation checks**

Run: `./mvnw -B -ntp verify`

Run: `./mvnw -B -ntp -Pproduction verify`

Run: `git diff --check`

Expected: all commands PASS; Keycloak remains test-scoped and production does
not require an identity provider or a configured OIDC client.

- [ ] **Step 5: Commit the documented outcome**

```bash
git add docs
git commit -m "docs: record OIDC integration readiness"
```
