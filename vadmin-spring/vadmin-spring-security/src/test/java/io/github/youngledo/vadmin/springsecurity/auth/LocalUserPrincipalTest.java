package io.github.youngledo.vadmin.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccount;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalUserPrincipalTest {
    @Test
    void exposesTheFrameworkNeutralAccountAndPermissionAuthorities() {
        var account = new LocalUserAccount(UUID.randomUUID(), "operator", "stored-hash", true, 4,
                Set.of(PermissionCode.of("system:user:read")));

        var principal = new LocalUserPrincipal(account);

        assertThat(principal.currentUser().authVersion()).isEqualTo(4);
        assertThat(principal.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("system:user:read");
    }

    @Test
    void derivesTheSamePermissionAuthoritiesFromACurrentUser() {
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(PermissionCode.of("system:user:read")), 4);

        var principal = new LocalUserPrincipal(user);

        assertThat(principal.currentUser()).isEqualTo(user);
        assertThat(principal.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("system:user:read");
    }
}
