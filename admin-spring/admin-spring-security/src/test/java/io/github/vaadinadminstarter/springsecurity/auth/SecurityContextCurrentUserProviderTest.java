package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextCurrentUserProviderTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exposesTheAuthenticatedLocalPrincipalAsCurrentUser() {
        var account = new LocalUserAccount(UUID.randomUUID(), "operator", "stored-hash", true, 3,
                Set.of(PermissionCode.of("system:user:read")));
        var principal = new LocalUserPrincipal(account);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        var user = new SecurityContextCurrentUserProvider().currentUser();

        assertThat(user).contains(principal.currentUser());
    }

    @Test
    void returnsEmptyWhenThereIsNoLocalAuthentication() {
        assertThat(new SecurityContextCurrentUserProvider().currentUser()).isEmpty();
    }
}
