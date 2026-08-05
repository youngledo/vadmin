package io.github.vaadinadminstarter.app.administration;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {
    private static final PermissionCode CREATE = PermissionCode.of("system:user:create");
    private static final PermissionCode UPDATE = PermissionCode.of("system:user:update");
    private final JdbcTemplate jdbcTemplate;
    private final AuthorizationService authorization;
    private final AuditSink auditSink;
    private final PasswordEncoder passwordEncoder;

    public UserAdministrationService(JdbcTemplate jdbcTemplate, AuthorizationService authorization, AuditSink auditSink,
                                     PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.authorization = authorization;
        this.auditSink = auditSink;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void create(CurrentUser actor, String username, String password) {
        authorization.requirePermission(actor, CREATE);
        var userId = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into users (id, username, password_hash, enabled, auth_version)
                    values (?, ?, ?, true, 0)
                    """, userId, required("username", username), passwordEncoder.encode(required("password", password)));
        } catch (DuplicateKeyException exception) {
            throw new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed",
                    Map.of("username", "already-exists"));
        }
        auditSink.append(new AuditEvent(actor.userId(), "system:user:create", "user", userId.toString(),
                AuditOutcome.SUCCESS, Instant.now(), null, Map.of()));
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

    private String required(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of(field, "required"));
        }
        return value.strip();
    }
}
