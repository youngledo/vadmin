package io.github.youngledo.vadmin.springsecurity.auth;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

/** Exposes the local Spring Security principal without leaking Spring types into application services. */
public final class SecurityContextCurrentUserProvider implements CurrentUserProvider {
    @Override
    public Optional<CurrentUser> currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof LocalUserPrincipal principal) {
            return Optional.of(principal.currentUser());
        }
        return Optional.empty();
    }
}
