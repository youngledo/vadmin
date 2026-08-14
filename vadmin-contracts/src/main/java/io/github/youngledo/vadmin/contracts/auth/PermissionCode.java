package io.github.youngledo.vadmin.contracts.auth;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public record PermissionCode(String value) implements Serializable {
    private static final Pattern PATTERN = Pattern.compile("[a-z][a-z0-9-]*:[a-z][a-z0-9-]*:[a-z][a-z0-9-]*");

    public PermissionCode {
        Objects.requireNonNull(value, "value");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Permission code must use domain:resource:action format");
        }
    }

    public static PermissionCode of(String value) {
        return new PermissionCode(value);
    }
}
