package io.github.vaadinadminstarter.contracts.error;

public enum ErrorCode {
    AUTHORIZATION_DENIED("authorization.denied"),
    VALIDATION_FAILED("validation.failed"),
    RESOURCE_NOT_FOUND("resource.not_found"),
    CONFLICT_VERSION("conflict.version"),
    INTERNAL_ERROR("internal.error");

    private final String value;

    ErrorCode(String value) { this.value = value; }

    public String value() { return value; }
}
