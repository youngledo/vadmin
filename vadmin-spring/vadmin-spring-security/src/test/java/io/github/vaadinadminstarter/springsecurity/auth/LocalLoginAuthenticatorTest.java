package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

class LocalLoginAuthenticatorTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void savesAnAuthenticatedSecurityContextAfterProtectingTheSession() {
        var authenticationManager = mock(AuthenticationManager.class);
        var sessionStrategy = mock(SessionAuthenticationStrategy.class);
        var securityContextRepository = mock(SecurityContextRepository.class);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var authenticated = UsernamePasswordAuthenticationToken.authenticated("admin", null, java.util.List.of());
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(authenticated);
        var authenticator = new LocalLoginAuthenticator(authenticationManager, sessionStrategy, securityContextRepository);

        var accepted = authenticator.authenticate("admin", "change-me", request, response);

        assertThat(accepted).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authenticated);
        var order = inOrder(sessionStrategy, securityContextRepository);
        order.verify(sessionStrategy).onAuthentication(authenticated, request, response);
        order.verify(securityContextRepository).saveContext(
                org.mockito.ArgumentMatchers.argThat(context -> context.getAuthentication() == authenticated),
                org.mockito.ArgumentMatchers.same(request), org.mockito.ArgumentMatchers.same(response));
    }

    @Test
    void rejectsInvalidCredentialsWithoutChangingTheSessionOrSavingAuthentication() {
        var authenticationManager = mock(AuthenticationManager.class);
        var sessionStrategy = mock(SessionAuthenticationStrategy.class);
        var securityContextRepository = mock(SecurityContextRepository.class);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("invalid credentials"));
        var authenticator = new LocalLoginAuthenticator(authenticationManager, sessionStrategy, securityContextRepository);

        var accepted = authenticator.authenticate("admin", "incorrect-password", request, response);

        assertThat(accepted).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(sessionStrategy, securityContextRepository);
    }
}
