package io.github.vaadinadminstarter.app.administration;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {
    private static final PermissionCode UPDATE = PermissionCode.of("system:user:update");
    private final JdbcTemplate jdbcTemplate;
    private final AuthorizationService authorization;
    private final AuditSink auditSink;

    public UserAdministrationService(JdbcTemplate jdbcTemplate, AuthorizationService authorization, AuditSink auditSink) {
        this.jdbcTemplate = jdbcTemplate;
        this.authorization = authorization;
        this.auditSink = auditSink;
    }

    @Transactional
    public void setEnabled(CurrentUser actor, UUID userId, boolean enabled) {
        setEnabled(actor, Set.of(userId), enabled);
    }

    @Transactional
    public void setEnabled(CurrentUser actor, Set<UUID> userIds, boolean enabled) {
        authorization.requirePermission(actor, UPDATE);
        Set.copyOf(userIds).forEach(userId -> {
            jdbcTemplate.update("update users set enabled = ?, auth_version = auth_version + 1 where id = ?", enabled, userId);
            auditSink.append(new AuditEvent(actor.userId(), "system:user:update", "user", userId.toString(),
                    AuditOutcome.SUCCESS, Instant.now(), null, Map.of("enabled", Boolean.toString(enabled))));
        });
    }
}
