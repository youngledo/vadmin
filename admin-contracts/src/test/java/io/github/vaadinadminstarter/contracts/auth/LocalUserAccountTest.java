package io.github.vaadinadminstarter.contracts.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalUserAccountTest {
    @Test
    void copiesGrantedPermissionsIntoAnImmutableAuthenticationSnapshot() {
        var grantedPermissions = new HashSet<PermissionCode>();
        grantedPermissions.add(PermissionCode.of("system:user:read"));

        var account = new LocalUserAccount(UUID.randomUUID(), "operator", "stored-password-hash", true, 3,
                grantedPermissions);
        grantedPermissions.clear();

        assertThat(account.permissions()).containsExactly(PermissionCode.of("system:user:read"));
    }
}
