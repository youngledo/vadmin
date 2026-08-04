package io.github.vaadinadminstarter.flow.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

class PermissionGateTest {
    @Test
    void disablesActionWhenCurrentUserLacksRequiredPermission() {
        var permission = PermissionCode.of("system:user:create");
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(), 1);
        AuthorizationService authorization = new AuthorizationService() {
            public boolean hasPermission(CurrentUser candidate, PermissionCode required) { return candidate.permissions().contains(required); }
            public void requirePermission(CurrentUser candidate, PermissionCode required) { throw new UnsupportedOperationException(); }
        };

        assertThat(new PermissionGate(authorization).isAllowed(user, permission)).isFalse();
    }
}
