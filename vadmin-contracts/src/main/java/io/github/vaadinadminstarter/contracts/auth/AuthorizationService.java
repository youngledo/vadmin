package io.github.vaadinadminstarter.contracts.auth;

public interface AuthorizationService {
    boolean hasPermission(CurrentUser user, PermissionCode permission);

    void requirePermission(CurrentUser user, PermissionCode permission);
}
