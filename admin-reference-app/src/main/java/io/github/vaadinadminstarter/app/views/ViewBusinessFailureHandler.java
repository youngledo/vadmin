package io.github.vaadinadminstarter.app.views;

import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.util.Objects;
import java.util.function.Consumer;

final class ViewBusinessFailureHandler {
    private ViewBusinessFailureHandler() {
    }

    static void handle(BusinessFailure failure, Consumer<BusinessFailure> validationHandler) {
        if (failure.errorCode() != ErrorCode.VALIDATION_FAILED) {
            throw failure;
        }
        Objects.requireNonNull(validationHandler).accept(failure);
    }
}
