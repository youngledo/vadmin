package io.github.vaadinadminstarter.contracts.auth;

import java.util.Optional;
import java.util.UUID;

public interface LocalUserAccountLookup {
    Optional<LocalUserAccount> findByUsername(String username);

    Optional<LocalUserAccount> findByUserId(UUID userId);
}
