package io.github.vaadinadminstarter.app.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class ViewBusinessFailureHandlerTest {
    @Test
    void forwardsOnlyValidationFailuresToTheView() {
        var presented = new AtomicReference<BusinessFailure>();

        invoke(new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of()), presented::set);

        assertThat(presented.get()).isNotNull();
    }

    @Test
    void rethrowsNonValidationFailuresForGlobalFlowHandling() {
        var failure = new BusinessFailure(ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of());

        assertThatThrownBy(() -> invoke(failure, ignored -> { }))
                .isSameAs(failure);
    }

    private void invoke(BusinessFailure failure, Consumer<BusinessFailure> validationHandler) {
        ViewBusinessFailureHandler.handle(failure, validationHandler);
    }
}
