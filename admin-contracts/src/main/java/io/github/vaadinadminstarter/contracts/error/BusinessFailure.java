package io.github.vaadinadminstarter.contracts.error;

import java.util.Map;
import java.util.Objects;

public final class BusinessFailure extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailKey;
    private final Map<String, String> fieldErrors;

    public BusinessFailure(ErrorCode errorCode, String detailKey, Map<String, String> fieldErrors) {
        super(detailKey, null, false, false);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.detailKey = Objects.requireNonNull(detailKey, "detailKey");
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public ErrorCode errorCode() { return errorCode; }
    public String detailKey() { return detailKey; }
    public Map<String, String> fieldErrors() { return fieldErrors; }
}
