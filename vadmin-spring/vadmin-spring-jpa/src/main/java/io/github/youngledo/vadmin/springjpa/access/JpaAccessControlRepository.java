package io.github.youngledo.vadmin.springjpa.access;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.platform.access.AccessControlRepository;
import io.github.youngledo.vadmin.platform.access.Permission;
import io.github.youngledo.vadmin.platform.access.Role;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

public final class JpaAccessControlRepository implements AccessControlRepository {
    private final EntityManager entityManager;

    public JpaAccessControlRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Role> findRoleByCode(String code) {
        return entityManager.createQuery("select role from JpaRoleEntity role where role.code = :code", JpaRoleEntity.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst()
                .map(role -> new Role(role.id(), role.code()));
    }

    @Override
    public Optional<Permission> findPermissionByCode(PermissionCode code) {
        return entityManager.createQuery(
                        "select permission from JpaPermissionEntity permission where permission.code = :code",
                        JpaPermissionEntity.class)
                .setParameter("code", code.value())
                .getResultStream()
                .findFirst()
                .map(permission -> new Permission(permission.id(), PermissionCode.of(permission.code())));
    }

    @Override
    public void grantPermission(UUID roleId, UUID permissionId) {
        entityManager.createNativeQuery("""
                insert into role_permissions (role_id, permission_id)
                values (:roleId, :permissionId)
                on conflict do nothing
                """)
                .setParameter("roleId", roleId)
                .setParameter("permissionId", permissionId)
                .executeUpdate();
    }

    @Override
    public void incrementAuthVersionForRole(UUID roleId) {
        entityManager.createNativeQuery("""
                update users
                set auth_version = auth_version + 1
                where id in (select user_id from user_roles where role_id = :roleId)
                """)
                .setParameter("roleId", roleId)
                .executeUpdate();
    }
}
