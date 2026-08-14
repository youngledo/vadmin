package io.github.youngledo.vadmin.flow.error;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;

/** Maps business failures for Flow views without involving Spring MVC error handling. */
public final class FlowErrorMapper implements java.io.Serializable {
    public FlowError map(BusinessFailure failure) {
        return switch (failure.errorCode()) {
            case VALIDATION_FAILED -> new FlowError(
                    FlowErrorPresentation.FIELD_VALIDATION, 400, failure.detailKey(), failure.fieldErrors());
            case AUTHORIZATION_DENIED -> new FlowError(
                    FlowErrorPresentation.ACCESS_DENIED, 403, failure.detailKey(), failure.fieldErrors());
            case RESOURCE_NOT_FOUND, CONFLICT_VERSION, INTERNAL_ERROR -> new FlowError(
                    FlowErrorPresentation.FAILURE, statusFor(failure.errorCode()), failure.detailKey(), failure.fieldErrors());
        };
    }

    private int statusFor(ErrorCode errorCode) {
        return switch (errorCode) {
            case RESOURCE_NOT_FOUND -> 404;
            case CONFLICT_VERSION -> 409;
            case INTERNAL_ERROR -> 500;
            case AUTHORIZATION_DENIED, VALIDATION_FAILED -> throw new IllegalArgumentException("handled separately");
        };
    }
}
