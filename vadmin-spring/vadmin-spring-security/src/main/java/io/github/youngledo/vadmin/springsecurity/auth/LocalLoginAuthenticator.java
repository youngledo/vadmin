package io.github.youngledo.vadmin.springsecurity.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

public final class LocalLoginAuthenticator {
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;

    public LocalLoginAuthenticator(AuthenticationManager authenticationManager,
                                   SessionAuthenticationStrategy sessionAuthenticationStrategy,
                                   SecurityContextRepository securityContextRepository) {
        this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager");
        this.sessionAuthenticationStrategy = Objects.requireNonNull(sessionAuthenticationStrategy,
                "sessionAuthenticationStrategy");
        this.securityContextRepository = Objects.requireNonNull(securityContextRepository, "securityContextRepository");
    }

    public boolean authenticate(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return true;
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            return false;
        }
    }
}
