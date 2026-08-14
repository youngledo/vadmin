package io.github.youngledo.vadmin.springjpa.access;

import io.github.youngledo.vadmin.contracts.auth.LocalUserSession;
import io.github.youngledo.vadmin.contracts.auth.LocalUserSessionLookup;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

public final class JpaLocalUserSessionLookup implements LocalUserSessionLookup {
    private final EntityManager entityManager;

    public JpaLocalUserSessionLookup(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<LocalUserSession> findSessionByUserId(UUID userId) {
        return entityManager.createQuery("""
                        select new io.github.youngledo.vadmin.contracts.auth.LocalUserSession(
                            user.id, user.enabled, user.authVersion)
                        from JpaUserAccountEntity user
                        where user.id = :userId
                """, LocalUserSession.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .findFirst();
    }
}
