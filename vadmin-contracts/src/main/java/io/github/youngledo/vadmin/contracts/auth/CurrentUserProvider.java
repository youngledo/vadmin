package io.github.youngledo.vadmin.contracts.auth;

import java.io.Serializable;
import java.util.Optional;

/** Provides the authenticated user for the current request or UI and is retained by Flow views. */
public interface CurrentUserProvider extends Serializable {
    Optional<CurrentUser> currentUser();
}
