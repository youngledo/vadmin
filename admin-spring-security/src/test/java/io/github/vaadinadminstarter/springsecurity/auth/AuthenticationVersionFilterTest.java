package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSession;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationVersionFilterTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clearsAuthenticationWhenStoredAuthVersionChanges() throws Exception {
        var userId = UUID.randomUUID();
        var sessionPrincipal = new LocalUserPrincipal(new LocalUserAccount(
                userId, "operator", "stored-hash", true, 1, Set.of()));
        var currentSession = new LocalUserSession(userId, true, 2);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(sessionPrincipal, null, sessionPrincipal.getAuthorities()));
        var filter = new AuthenticationVersionFilter(new SessionLookup(currentSession));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> { });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private record SessionLookup(LocalUserSession session) implements LocalUserSessionLookup {
        @Override
        public Optional<LocalUserSession> findSessionByUserId(UUID userId) {
            return Optional.of(session);
        }
    }
}
