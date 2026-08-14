package io.github.youngledo.vadmin.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccount;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpringSecurityBridgeTest {
    @Test
    void loadsLocalAccountAsSpringSecurityPrincipal() {
        var account = new LocalUserAccount(UUID.randomUUID(), "operator", "stored-hash", true, 1,
                Set.of(PermissionCode.of("system:user:read")));
        var service = new LocalUserDetailsService(new AccountLookup(account));

        var principal = (LocalUserPrincipal) service.loadUserByUsername("operator");

        assertThat(principal.getUsername()).isEqualTo("operator");
        assertThat(principal.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("system:user:read");
    }

    @Test
    void raisesBusinessAuthorizationFailureWhenPermissionIsMissing() {
        var authorization = new SpringAuthorizationService();
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(), 0);

        assertThatThrownBy(() -> authorization.requirePermission(user, PermissionCode.of("system:user:read")))
                .isInstanceOf(BusinessFailure.class)
                .extracting("errorCode")
                .hasToString("AUTHORIZATION_DENIED");
    }

    private record AccountLookup(LocalUserAccount account) implements LocalUserAccountLookup {
        @Override
        public Optional<LocalUserAccount> findByUsername(String username) {
            return account.username().equals(username) ? Optional.of(account) : Optional.empty();
        }

        @Override
        public Optional<LocalUserAccount> findByUserId(UUID userId) {
            return account.userId().equals(userId) ? Optional.of(account) : Optional.empty();
        }
    }
}
