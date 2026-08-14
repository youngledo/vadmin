package io.github.youngledo.vadmin.springsecurity.auth;

import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.util.Map;

public final class SpringAuthorizationService implements AuthorizationService {
    @Override
    public boolean hasPermission(CurrentUser user, PermissionCode permission) {
        return user.permissions().contains(permission);
    }

    @Override
    public void requirePermission(CurrentUser user, PermissionCode permission) {
        if (!hasPermission(user, permission)) {
            throw new BusinessFailure(ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of());
        }
    }
}
