package io.github.youngledo.vadmin.platform.access;

import java.util.Optional;
import java.util.UUID;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

public interface AccessControlRepository {
    Optional<Role> findRoleByCode(String code);
    Optional<Permission> findPermissionByCode(PermissionCode code);
    void grantPermission(UUID roleId, UUID permissionId);
    void incrementAuthVersionForRole(UUID roleId);
}
