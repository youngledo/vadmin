package io.github.vaadinadminstarter.contracts.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExternalIdentityMapperTest {

    @Test
    void mapsAnExternalIdentityToAnOptionalCurrentUser() {
        ExternalIdentityMapper mapper = identity -> Optional.empty();

        assertThat(mapper.map(new ExternalIdentity(URI.create("https://issuer.example"), "subject-42", null, null,
                Map.of()))).isEmpty();
    }
}
