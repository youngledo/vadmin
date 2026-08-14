package io.github.vaadinadminstarter.contracts.auth;

import java.util.Optional;

@FunctionalInterface
public interface ExternalIdentityMapper {
    Optional<CurrentUser> map(ExternalIdentity identity);
}
