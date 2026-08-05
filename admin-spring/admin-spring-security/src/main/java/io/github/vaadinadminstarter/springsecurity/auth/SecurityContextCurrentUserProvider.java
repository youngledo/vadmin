package io.github.vaadinadminstarter.springsecurity.auth;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

/** Exposes the local Spring Security principal without leaking Spring types into application services. */
public final class SecurityContextCurrentUserProvider {
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
