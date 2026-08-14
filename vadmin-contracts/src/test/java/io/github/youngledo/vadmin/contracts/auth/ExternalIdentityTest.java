package io.github.youngledo.vadmin.contracts.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExternalIdentityTest {

    @Test
    void requiresAnAbsoluteIssuerAndStableSubject() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExternalIdentity(null, "subject", null, null, Map.of()));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExternalIdentity(URI.create("issuer"), "subject", null, null, Map.of()));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExternalIdentity(URI.create("https://issuer.example"), null, null, null, Map.of()));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExternalIdentity(URI.create("https://issuer.example"), " ", null, null, Map.of()));
    }

    @Test
    void exposesAnImmutableClaimSnapshot() {
        var sourceClaims = new HashMap<>(Map.of("groups", "operators"));
        var identity = new ExternalIdentity(URI.create("https://issuer.example"), "subject-42",
                "Ada", "ada@example.test", sourceClaims);
        sourceClaims.clear();

        assertThat(identity.claims()).containsEntry("groups", "operators");
        assertThatThrownBy(() -> identity.claims().put("role", "admin"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
