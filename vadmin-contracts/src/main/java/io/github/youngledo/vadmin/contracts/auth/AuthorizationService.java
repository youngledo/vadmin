package io.github.youngledo.vadmin.contracts.auth;

import java.io.Serializable;

/** Authorization collaborator retained by Flow views in the Vaadin session. */
public interface AuthorizationService extends Serializable {
    boolean hasPermission(CurrentUser user, PermissionCode permission);

    void requirePermission(CurrentUser user, PermissionCode permission);
}
