package io.github.youngledo.vadmin.springsecurity;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OIDC login settings owned by the starter.
 */
@ConfigurationProperties("vaadin-admin.oidc")
public record OidcLoginProperties(String registrationId) {
    public static final String DEFAULT_REGISTRATION_ID = "oidc";

    public OidcLoginProperties {
        registrationId = registrationId == null ? DEFAULT_REGISTRATION_ID : registrationId;
        if (registrationId.isBlank()) {
            throw new IllegalArgumentException("registrationId must not be blank");
        }
    }
}
