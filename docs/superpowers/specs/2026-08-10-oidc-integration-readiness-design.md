# OIDC Integration Readiness Design

**Status:** Approved for planning  
**Date:** 2026-08-10

## Goal

Offer a standards-based OpenID Connect login path for Spring Boot consumers
while preserving local-password login and the starter's Java-only authorization
contracts. The starter must be usable with global, mainland-China, self-hosted,
or future identity providers that correctly implement standard OIDC discovery.

## Product Boundary

The starter provides protocol integration and a deliberately conservative local
identity hand-off. A consuming application owns its identity-to-authorization
policy.

| Provided by the starter | Owned by the consuming application |
| --- | --- |
| OIDC authorization-code login through issuer discovery | Which OIDC provider(s) to configure |
| Standard claim extraction from an authenticated OIDC subject | Mapping subject, groups, or private claims to a local user |
| A Spring-free external-identity and mapping SPI | Automatic account creation, role grants, or deprovisioning |
| Safe denial when no local user is resolved | Organization hierarchy, tenant/data scope, SCIM, MFA, SAML, or LDAP |
| Coexistence with existing local-password login | Provider-specific claim conventions and lifecycle workflows |

The first version does not implement a universal enterprise authorization
model. It must never infer local roles or permissions merely because an OIDC
login succeeded.

## Standards And Provider Compatibility

The runtime integration uses OpenID Connect Discovery and the authorization
code flow supplied by Spring Security. Provider configuration is issuer-based;
the public contract must not import or configure vendor SDKs.

Keycloak is a test fixture only. It verifies interoperable behavior in local
and CI environments and is not a production dependency or recommended identity
provider. A compliant issuer from a mainland-China provider, a self-hosted
system, or a global provider is expected to work through the same configuration
path. Provider-specific documentation belongs in consumer examples, not the
core contract.

## Architecture

### Neutral Identity Contract

`admin-contracts` gains a small identity model that contains only:

- the normalized issuer URI;
- the stable OIDC `sub` value;
- optional display name and email claims; and
- immutable, string-valued standard or provider claims exposed for consumer
  mapping.

An `ExternalIdentityMapper` SPI resolves that identity to an existing
`CurrentUser` or declines it. The SPI receives no Spring Security, Flow,
database, or provider-SDK types. A mapper may use application-owned lookup
ports, group claims, or its own domain services, but those policies are outside
the starter.

The mapper result is intentionally binary for the first version: a resolved
local current user, or no result. Mapping failure ends the external login with
a clear access-denied result. There is no default just-in-time provisioning,
role synchronization, or fallback grant.

### Spring Security Adapter

`admin-spring-security` adapts Spring Security's authenticated OIDC principal
to the neutral external-identity contract. It delegates resolution to exactly
one `ExternalIdentityMapper`, then creates the same local Spring Security
principal used by password login. Consequently `CurrentUserProvider`, route
authorization, permission checks, Flow module navigation, auditing, and
authentication-version invalidation continue to consume one local-user model.

OIDC configuration is opt-in. Without a configured Spring Security client
registration, the current local login page and DAO authentication behavior are
unchanged. With OIDC enabled, the login experience offers both local and
external sign-in; an external authorization failure returns to the local login
surface without exposing provider tokens or private claims.

The existing authentication-version filter remains a local-session safeguard.
It is applied to external logins only after the mapper has resolved a local
account, so a disabled or invalidated local account cannot retain access
through an external session.

### Runtime Boundaries

- `admin-contracts` remains framework-neutral.
- `admin-flow` remains independent of authentication-provider details.
- `admin-spring-security` is the only module that imports Spring Security OIDC
  APIs.
- The reference application supplies an explicit sample mapper which resolves
  a pre-existing local account by a configured, stable external identity key.
- External modules do not receive raw OIDC principals or tokens.

## Security Rules

- Use issuer discovery and Spring Security's authorization-code handling; do
  not implement token exchange, JWT validation, nonce handling, or PKCE by
  hand.
- Treat `iss` and `sub` together as the stable external identity. Do not use
  mutable display names or email addresses as the primary identifier.
- Never log access tokens, ID tokens, client secrets, raw private claims, or
  authorization codes.
- Reject external authentication when a mapper is absent, returns no local
  user, or resolves an inactive local account.
- Preserve CSRF protection, session fixation protection, local logout, and
  existing route authorization.
- Local-password login remains available by default. Removing it is an
  application-level deployment decision, not a starter default.

## User Experience

The login page stays a local Flow view. When external sign-in is configured,
it exposes a concise provider-neutral external-login action next to the local
form. The action uses accessible text and a normal navigation/redirect; it
does not add a provider logo or marketing treatment. Provider selection remains
Spring Security registration configuration and is not duplicated as an
administration UI.

After a successful external login, the user returns to the originally requested
route. On mapping denial or provider failure, the login page explains that the
account is not authorized and preserves the local-login option. Detailed
provider errors remain server-side diagnostics.

## Testing And Acceptance

The implementation must prove:

1. The new neutral contract has no Spring, Flow, or persistence dependency.
2. Local password authentication and the existing `CurrentUserProvider` behavior
   remain unchanged when OIDC is disabled.
3. A mapper receives normalized issuer and subject values and can resolve an
   existing local user into the established local principal.
4. Missing mapping, disabled account, and authentication-version invalidation
   deny external access without permissions leaking into the session.
5. A Keycloak-backed integration test completes discovery, callback, local-user
   resolution, route protection, and logout without introducing a production
   Keycloak dependency.
6. Browser coverage verifies local and external login entry points, return URL,
   denial messaging, locale refresh, and no token/claim rendering.
7. Normal and production reactor verification pass with OIDC disabled and with
   the integration fixture enabled only in tests.

## Non-Goals

This phase does not add SAML, LDAP, MFA, SCIM, provider administration pages,
multi-tenancy, organization hierarchy, group-to-role policy, automatic account
provisioning, automatic deprovisioning, or data-scope authorization. These are
consumer-owned extensions that may later receive separate specifications when
real adopter requirements exist.

## Decision Record

Proceed with generic OIDC discovery plus a Keycloak test fixture. Do not ship a
Keycloak-specific production adapter or a vendor compatibility list. The next
artifact is a detailed implementation plan; code work starts only from that
plan.
