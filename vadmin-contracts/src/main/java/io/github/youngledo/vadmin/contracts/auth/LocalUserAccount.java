package io.github.youngledo.vadmin.contracts.auth;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record LocalUserAccount(UUID userId, String username, String passwordHash, boolean enabled, long authVersion,
                               Set<PermissionCode> permissions) {
    public LocalUserAccount {
        Objects.requireNonNull(userId, "userId");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        Objects.requireNonNull(passwordHash, "passwordHash");
        if (authVersion < 0) {
            throw new IllegalArgumentException("authVersion must not be negative");
        }
        permissions = Set.copyOf(permissions);
    }
}
