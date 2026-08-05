package io.github.vaadinadminstarter.springsecurity.auth;

import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

public final class AuthenticationVersionFilter extends OncePerRequestFilter {
    private final LocalUserSessionLookup sessionLookup;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AuthenticationVersionFilter(LocalUserSessionLookup sessionLookup) {
        this.sessionLookup = sessionLookup;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LocalUserPrincipal principal
                && isInvalid(principal)) {
            logoutHandler.logout(request, response, authentication);
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private boolean isInvalid(LocalUserPrincipal principal) {
        return sessionLookup.findSessionByUserId(principal.currentUser().userId())
                .map(session -> !session.enabled()
                        || session.authVersion() != principal.currentUser().authVersion())
                .orElse(true);
    }
}
