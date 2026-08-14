package io.github.youngledo.vadmin.contracts.auth;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record CurrentUser(UUID userId, String username, Set<PermissionCode> permissions, long authVersion)
        implements Serializable {
    public CurrentUser {
        Objects.requireNonNull(userId, "userId");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        permissions = Set.copyOf(permissions);
        if (authVersion < 0) {
            throw new IllegalArgumentException("authVersion must not be negative");
        }
    }
}
