package io.github.vaadinadminstarter.springjpa.access;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public final class JpaLocalUserAccountLookup implements LocalUserAccountLookup {
    private final EntityManager entityManager;

    public JpaLocalUserAccountLookup(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<LocalUserAccount> findByUsername(String username) {
        return entityManager.createQuery(
                        "select user from JpaUserAccountEntity user where user.username = :username",
                        JpaUserAccountEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(this::toAccount);
    }

    @Override
    public Optional<LocalUserAccount> findByUserId(UUID userId) {
        return Optional.ofNullable(entityManager.find(JpaUserAccountEntity.class, userId)).map(this::toAccount);
    }

    private LocalUserAccount toAccount(JpaUserAccountEntity user) {
        return new LocalUserAccount(user.id(), user.username(), user.passwordHash(), user.enabled(), user.authVersion(),
                permissionsFor(user.id()));
    }

    private Set<PermissionCode> permissionsFor(UUID userId) {
        List<?> codes = entityManager.createNativeQuery("""
                select distinct permission.code
                from permissions permission
                join role_permissions role_permission on role_permission.permission_id = permission.id
                join user_roles user_role on user_role.role_id = role_permission.role_id
                where user_role.user_id = :userId
                """)
                .setParameter("userId", userId)
                .getResultList();
        return codes.stream()
                .map(code -> PermissionCode.of((String) code))
                .collect(Collectors.toUnmodifiableSet());
    }
}
