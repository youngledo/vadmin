package io.github.youngledo.vadmin.springsecurity;

import java.util.Objects;

/**
 * Provider-neutral indication that the local login page may offer OIDC sign-in.
 */
public final class OidcLoginAvailability {
    private final boolean available;
    private final String registrationId;

    public OidcLoginAvailability(boolean available, String registrationId) {
        this.available = available;
        this.registrationId = Objects.requireNonNull(registrationId, "registrationId");
        if (registrationId.isBlank()) {
            throw new IllegalArgumentException("registrationId must not be blank");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public String registrationId() {
        return registrationId;
    }
}
