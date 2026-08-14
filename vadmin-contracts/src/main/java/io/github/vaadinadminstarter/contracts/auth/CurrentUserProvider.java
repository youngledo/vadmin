package io.github.vaadinadminstarter.contracts.auth;

import java.util.Optional;

/** Provides the authenticated user for the current request or UI. */
public interface CurrentUserProvider {
    Optional<CurrentUser> currentUser();
}
