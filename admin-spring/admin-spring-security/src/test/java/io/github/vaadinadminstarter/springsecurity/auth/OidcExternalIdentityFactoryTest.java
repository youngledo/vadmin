package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class OidcExternalIdentityFactoryTest {
    private final OidcExternalIdentityFactory factory = new OidcExternalIdentityFactory();

    @Test
    void normalizesIssuerSubjectAndStringClaimsFromAnOidcUser() {
        var identity = factory.from(oidcUser(Map.of(
                "iss", "https://issuer.example",
                "sub", "subject-42",
                "name", "Ada",
                "email", "ada@example.test",
                "department", "platform",
                "email_verified", true,
                "groups", List.of("operators"))));

        assertThat(identity.issuer().toString()).isEqualTo("https://issuer.example");
        assertThat(identity.subject()).isEqualTo("subject-42");
        assertThat(identity.displayName()).isEqualTo("Ada");
        assertThat(identity.email()).isEqualTo("ada@example.test");
        assertThat(identity.claims()).containsEntry("department", "platform")
                .doesNotContainKeys("email_verified", "groups");
    }

    @Test
    void rejectsAnOidcUserWithoutAnAbsoluteIssuerOrStableSubject() {
        assertThatIllegalArgumentException().isThrownBy(() -> factory.from(oidcUser(Map.of(
                "iss", "issuer",
                "sub", "subject-42"))));
        assertThatIllegalArgumentException().isThrownBy(() -> factory.from(oidcUser(Map.of(
                "iss", "https://issuer.example",
                "sub", " "))));
    }

    private OidcUser oidcUser(Map<String, Object> claims) {
        var now = Instant.parse("2026-08-10T00:00:00Z");
        return new DefaultOidcUser(List.of(), new OidcIdToken("opaque-id-token", now, now.plusSeconds(300), claims));
    }
}
