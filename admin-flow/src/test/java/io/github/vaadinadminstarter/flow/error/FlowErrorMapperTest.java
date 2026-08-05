package io.github.vaadinadminstarter.flow.error;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FlowErrorMapperTest {
    private final FlowErrorMapper mapper = new FlowErrorMapper();

    @Test
    void mapsValidationFailuresToFieldMessages() {
        var error = mapper.map(new BusinessFailure(
                ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of("username", "required")));

        assertThat(error.presentation()).isEqualTo(FlowErrorPresentation.FIELD_VALIDATION);
        assertThat(error.fieldErrors()).containsEntry("username", "required");
    }

    @Test
    void mapsAuthorizationFailuresToAccessDeniedPresentation() {
        var error = mapper.map(new BusinessFailure(ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of()));

        assertThat(error.presentation()).isEqualTo(FlowErrorPresentation.ACCESS_DENIED);
        assertThat(error.status()).isEqualTo(403);
    }
}
