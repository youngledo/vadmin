package io.github.youngledo.vadmin.springjpa.audit;

import io.github.youngledo.vadmin.contracts.audit.AuditOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_entries")
public class JpaAuditEntryEntity {
    @Id
    private UUID id;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "action_code", nullable = false)
    private String actionCode;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditOutcome outcome;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "correlation_id")
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, String> metadata;

    protected JpaAuditEntryEntity() { }

    public JpaAuditEntryEntity(UUID id, UUID actorUserId, String actionCode, String targetType, String targetId,
                               AuditOutcome outcome, Instant occurredAt, String correlationId,
                               Map<String, String> metadata) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.actionCode = actionCode;
        this.targetType = targetType;
        this.targetId = targetId;
        this.outcome = outcome;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.metadata = Map.copyOf(metadata);
    }
}
