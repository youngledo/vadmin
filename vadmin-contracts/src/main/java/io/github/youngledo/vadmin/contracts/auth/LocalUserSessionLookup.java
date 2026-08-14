package io.github.youngledo.vadmin.contracts.auth;

import java.util.Optional;
import java.util.UUID;

public interface LocalUserSessionLookup {
    Optional<LocalUserSession> findSessionByUserId(UUID userId);
}
