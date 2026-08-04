package io.github.vaadinadminstarter.flow.navigation;

import java.util.Objects;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public final class PermissionGate {
    private final AuthorizationService authorization;

    public PermissionGate(AuthorizationService authorization) {
        this.authorization = Objects.requireNonNull(authorization, "authorization");
    }

    public boolean isAllowed(CurrentUser user, PermissionCode permission) {
        return authorization.hasPermission(user, permission);
    }
}
