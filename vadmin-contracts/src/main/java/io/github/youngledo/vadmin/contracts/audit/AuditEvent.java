package io.github.youngledo.vadmin.contracts.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(UUID actorUserId, String actionCode, String targetType, String targetId,
                         AuditOutcome outcome, Instant occurredAt, String correlationId,
                         Map<String, String> metadata) {
    public AuditEvent { metadata = Map.copyOf(metadata); }
}
