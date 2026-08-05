package io.github.vaadinadminstarter.platform.access;

import java.time.Instant;
import java.util.Map;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.audit.CorrelationIdProvider;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;

public final class GrantPermissionService implements GrantPermissionUseCase {
    private static final PermissionCode GRANT_PERMISSION = PermissionCode.of("system:role:grant");
    private final AuthorizationService authorization;
    private final AccessControlRepository repository;
    private final PermissionCatalog catalog;
    private final AuditSink audit;
    private final CorrelationIdProvider correlationIdProvider;

    public GrantPermissionService(AuthorizationService authorization, AccessControlRepository repository,
                                  PermissionCatalog catalog, AuditSink audit) {
        this(authorization, repository, catalog, audit, () -> null);
    }

    public GrantPermissionService(AuthorizationService authorization, AccessControlRepository repository,
                                  PermissionCatalog catalog, AuditSink audit, CorrelationIdProvider correlationIdProvider) {
        this.authorization = authorization;
        this.repository = repository;
        this.catalog = catalog;
        this.audit = audit;
        this.correlationIdProvider = correlationIdProvider;
    }

    @Override public void grant(CurrentUser actor, GrantPermissionCommand command) {
        try {
            authorization.requirePermission(actor, GRANT_PERMISSION);
            catalog.requireKnown(command.permissionCode());
            var role = repository.findRoleByCode(command.roleCode()).orElseThrow(() -> failure("roleCode"));
            var permission = repository.findPermissionByCode(command.permissionCode()).orElseThrow(() -> failure("permissionCode"));
            repository.grantPermission(role.id(), permission.id());
            repository.incrementAuthVersionForRole(role.id());
            audit.append(event(actor, command, AuditOutcome.SUCCESS));
        } catch (BusinessFailure failure) {
            audit.append(event(actor, command, AuditOutcome.FAILURE));
            throw failure;
        } catch (RuntimeException failure) {
            audit.append(event(actor, command, AuditOutcome.DENIED));
            throw failure;
        }
    }

    private BusinessFailure failure(String field) { return new BusinessFailure(ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of(field, "invalid")); }
    private AuditEvent event(CurrentUser actor, GrantPermissionCommand command, AuditOutcome outcome) {
        return new AuditEvent(actor.userId(), GRANT_PERMISSION.value(), "role", command.roleCode(), outcome, Instant.now(),
                correlationIdProvider.currentCorrelationId(), Map.of("permissionCode", command.permissionCode().value()));
    }
}
