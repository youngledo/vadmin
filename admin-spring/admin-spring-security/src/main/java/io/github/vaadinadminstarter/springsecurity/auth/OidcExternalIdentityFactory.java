package io.github.vaadinadminstarter.springsecurity.auth;

import io.github.vaadinadminstarter.contracts.auth.ExternalIdentity;
import java.net.URI;
import java.util.LinkedHashMap;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class OidcExternalIdentityFactory {
    public ExternalIdentity from(OidcUser oidcUser) {
        var claims = oidcUser.getClaims();
        var issuer = requiredString(claims, "iss");
        var subject = requiredString(claims, "sub");
        var stringClaims = new LinkedHashMap<String, String>();
        claims.forEach((name, value) -> {
            if (value instanceof String stringValue) {
                stringClaims.put(name, stringValue);
            }
        });
        return new ExternalIdentity(URI.create(issuer), subject, optionalString(claims, "name"),
                optionalString(claims, "email"), stringClaims);
    }

    private static String requiredString(java.util.Map<String, Object> claims, String name) {
        var value = optionalString(claims, name);
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
