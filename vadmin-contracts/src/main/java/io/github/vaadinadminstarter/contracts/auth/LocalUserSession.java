package io.github.vaadinadminstarter.contracts.auth;

import java.util.Objects;
import java.util.UUID;

public record LocalUserSession(UUID userId, boolean enabled, long authVersion) {
    public LocalUserSession {
        Objects.requireNonNull(userId, "userId");
        if (authVersion < 0) {
            throw new IllegalArgumentException("authVersion must not be negative");
        }
    }
}
