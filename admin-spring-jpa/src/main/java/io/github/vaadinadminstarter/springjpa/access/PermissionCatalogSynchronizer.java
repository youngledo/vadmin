package io.github.vaadinadminstarter.springjpa.access;

import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class PermissionCatalogSynchronizer {
    private final EntityManager entityManager;

    public PermissionCatalogSynchronizer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void synchronize(PermissionCatalog catalog) {
        for (var permission : catalog.all()) {
            var count = entityManager.createQuery(
                            "select count(permission) from JpaPermissionEntity permission where permission.code = :code",
                            Long.class)
                    .setParameter("code", permission.value())
                    .getSingleResult();
            if (count == 0) {
                entityManager.persist(new JpaPermissionEntity(UUID.randomUUID(), permission.value(), true));
            }
        }
    }

    public void requireCustomerManaged(String permissionCode) {
        var permission = entityManager.createQuery(
                        "select permission from JpaPermissionEntity permission where permission.code = :code",
                        JpaPermissionEntity.class)
                .setParameter("code", permissionCode)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown permission code"));
        if (permission.systemManaged()) {
            throw new IllegalStateException("system-managed permissions cannot be changed through administration");
        }
    }
}
