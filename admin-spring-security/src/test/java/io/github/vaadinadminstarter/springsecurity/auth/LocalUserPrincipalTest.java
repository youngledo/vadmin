package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
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
}
