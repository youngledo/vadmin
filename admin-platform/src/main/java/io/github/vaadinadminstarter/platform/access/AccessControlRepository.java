package io.github.vaadinadminstarter.platform.access;

import java.util.Optional;
import java.util.UUID;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public interface AccessControlRepository {
    Optional<Role> findRoleByCode(String code);
    Optional<Permission> findPermissionByCode(PermissionCode code);
    void grantPermission(UUID roleId, UUID permissionId);
}
