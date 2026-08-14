package io.github.youngledo.vadmin.springboot.error;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProblemDetailMapperTest {
    private final ProblemDetailMapper mapper = new ProblemDetailMapper(() -> "test-correlation-id");

    @Test
    void mapsAuthorizationDeniedToRfc9457ProblemDetail() {
        var detail = mapper.map(new BusinessFailure(
                ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of()));

        assertThat(detail.getStatus()).isEqualTo(403);
        assertThat(detail.getProperties())
                .containsEntry("errorCode", "authorization.denied")
                .containsEntry("correlationId", "test-correlation-id");
    }
}
