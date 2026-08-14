package io.github.youngledo.vadmin.contracts.auth;

import java.net.URI;
import java.util.Map;

public record ExternalIdentity(URI issuer, String subject, String displayName, String email, Map<String, String> claims) {
    public ExternalIdentity {
        if (issuer == null || !issuer.isAbsolute()) {
            throw new IllegalArgumentException("issuer must be absolute");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        claims = Map.copyOf(claims);
    }
}
