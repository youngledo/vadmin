package io.github.youngledo.vadmin.springjpa.audit;

import io.github.youngledo.vadmin.contracts.audit.AuditEvent;
import io.github.youngledo.vadmin.contracts.audit.AuditMetadataRedactor;
import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import jakarta.persistence.EntityManager;
import java.util.UUID;

public final class JpaAuditSink implements AuditSink {
    private final EntityManager entityManager;
    private final AuditMetadataRedactor redactor;

    public JpaAuditSink(EntityManager entityManager, AuditMetadataRedactor redactor) {
        this.entityManager = entityManager;
        this.redactor = redactor;
    }

    @Override
    public void append(AuditEvent event) {
        entityManager.persist(new JpaAuditEntryEntity(
                UUID.randomUUID(), event.actorUserId(), event.actionCode(), event.targetType(), event.targetId(),
                event.outcome(), event.occurredAt(), event.correlationId(), redactor.redact(event.metadata())));
    }
}
