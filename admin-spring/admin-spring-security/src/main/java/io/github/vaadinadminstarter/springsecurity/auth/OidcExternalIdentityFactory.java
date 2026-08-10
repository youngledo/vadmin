package io.github.vaadinadminstarter.springsecurity.auth;

import io.github.vaadinadminstarter.contracts.auth.ExternalIdentity;
import java.net.URI;
import java.util.LinkedHashMap;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class OidcExternalIdentityFactory {
    public ExternalIdentity from(OidcUser oidcUser) {
        var claims = oidcUser.getClaims();
        var idToken = oidcUser.getIdToken();
        var issuer = idToken == null || idToken.getIssuer() == null ? null : idToken.getIssuer().toString();
        var subject = idToken == null ? null : idToken.getSubject();
        var stringClaims = new LinkedHashMap<String, String>();
        claims.forEach((name, value) -> {
            if (value instanceof String stringValue) {
                stringClaims.put(name, stringValue);
            }
        });
        return new ExternalIdentity(URI.create(requiredValue(issuer, "iss")), requiredValue(subject, "sub"),
                optionalString(claims, "name"),
                optionalString(claims, "email"), stringClaims);
    }

    private static String requiredValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " claim must be a non-blank string");
        }
        return value;
    }

    private static String optionalString(java.util.Map<String, Object> claims, String name) {
        var value = claims.get(name);
        return value instanceof String stringValue ? stringValue : null;
    }
}
