package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OperationFeedbackTest {
    @Test
    void presentsSuccessOnlyThroughTheConfiguredLocalPresenter() {
        var message = new AtomicReference<String>();
        var feedback = new OperationFeedback(message::set);

        feedback.success("User disabled");

        assertThat(message).hasValue("User disabled");
    }

    @Test
    void sendsValidationFailuresToTheLocalFailureHandler() {
        var handled = new AtomicReference<BusinessFailure>();
        var feedback = new OperationFeedback(message -> { });
        var validation = new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed",
                Map.of("username", "required"));

        feedback.handleFailure(validation, handled::set);

        assertThat(handled).hasValue(validation);
    }

    @Test
    void rethrowsAuthorizationAndUnexpectedFailuresForGlobalHandling() {
        var feedback = new OperationFeedback(message -> { });
        var authorization = new BusinessFailure(ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of());
        var unexpected = new IllegalStateException("unexpected");

        assertThatThrownBy(() -> feedback.handleFailure(authorization, failure -> { }))
                .isSameAs(authorization);
        assertThatThrownBy(() -> feedback.handleFailure(unexpected, failure -> { }))
                .isSameAs(unexpected);
    }
}
