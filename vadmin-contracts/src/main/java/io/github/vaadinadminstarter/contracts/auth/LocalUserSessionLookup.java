package io.github.vaadinadminstarter.contracts.auth;

import java.util.Optional;
import java.util.UUID;

public interface LocalUserSessionLookup {
    Optional<LocalUserSession> findSessionByUserId(UUID userId);
}
