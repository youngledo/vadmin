package io.github.youngledo.vadmin.springboot.error;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import io.github.youngledo.vadmin.contracts.audit.CorrelationIdProvider;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemDetailMapper {
    private static final Map<ErrorCode, HttpStatus> STATUSES = Map.of(
            ErrorCode.AUTHORIZATION_DENIED, HttpStatus.FORBIDDEN,
            ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST,
            ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorCode.CONFLICT_VERSION, HttpStatus.CONFLICT,
            ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

    private final CorrelationIdProvider correlationIdProvider;

    public ProblemDetailMapper(CorrelationIdProvider correlationIdProvider) {
        this.correlationIdProvider = correlationIdProvider;
    }

    public ProblemDetail map(BusinessFailure failure) {
        var status = STATUSES.getOrDefault(failure.errorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        var detail = ProblemDetail.forStatusAndDetail(status, failure.detailKey());
        detail.setType(URI.create("urn:vaadin-admin-starter:error:" + failure.errorCode().value()));
        detail.setTitle(titleFor(failure.errorCode()));
        detail.setProperty("errorCode", failure.errorCode().value());
        detail.setProperty("fieldErrors", failure.fieldErrors());
        var correlationId = correlationIdProvider.currentCorrelationId();
        if (correlationId != null) {
            detail.setProperty("correlationId", correlationId);
        }
        return detail;
    }

    private String titleFor(ErrorCode errorCode) {
        return switch (errorCode) {
            case AUTHORIZATION_DENIED -> "Authorization denied";
            case VALIDATION_FAILED -> "Validation failed";
            case RESOURCE_NOT_FOUND -> "Resource not found";
            case CONFLICT_VERSION -> "Conflict";
            case INTERNAL_ERROR -> "Internal server error";
        };
    }
}
