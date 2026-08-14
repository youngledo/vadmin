package io.github.youngledo.vadmin.starter.views;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
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
