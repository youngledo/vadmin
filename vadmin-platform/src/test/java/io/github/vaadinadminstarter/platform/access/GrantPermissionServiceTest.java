package io.github.vaadinadminstarter.platform.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;

class GrantPermissionServiceTest {
    @Test
    void grantingPermissionAuditsSuccess() {
        var grantPermission = PermissionCode.of("system:role:grant");
        var userRead = PermissionCode.of("system:user:read");
        var repository = new InMemoryRepository(new Role(UUID.randomUUID(), "operator"), new Permission(UUID.randomUUID(), userRead));
        var audit = new Events();
        var service = new GrantPermissionService(new SetAuthorizationService(), repository,
                new PermissionCatalog(Set.of(grantPermission, userRead)), audit, () -> "request-42");

        service.grant(new CurrentUser(UUID.randomUUID(), "admin", Set.of(grantPermission), 1),
                new GrantPermissionCommand("operator", userRead));

        assertThat(repository.granted).isTrue();
        assertThat(repository.authVersionIncremented).isTrue();
        assertThat(audit.events).singleElement().extracting(AuditEvent::actionCode).isEqualTo("system:role:grant");
        assertThat(audit.events).singleElement().extracting(AuditEvent::correlationId).isEqualTo("request-42");
    }

    @Test
    void unknownCatalogPermissionDoesNotPersistAndAuditsFailure() {
        var grantPermission = PermissionCode.of("system:role:grant");
        var unknownPermission = PermissionCode.of("system:user:delete");
        var repository = new InMemoryRepository(new Role(UUID.randomUUID(), "operator"), new Permission(UUID.randomUUID(), unknownPermission));
        var audit = new Events();
        var service = new GrantPermissionService(new SetAuthorizationService(), repository,
                new PermissionCatalog(Set.of(grantPermission)), audit);
        var actor = new CurrentUser(UUID.randomUUID(), "admin", Set.of(grantPermission), 1);

        assertThatThrownBy(() -> service.grant(actor, new GrantPermissionCommand("operator", unknownPermission)))
                .isInstanceOf(io.github.vaadinadminstarter.contracts.error.BusinessFailure.class);
        assertThat(repository.granted).isFalse();
        assertThat(audit.events).singleElement().extracting(AuditEvent::outcome).isEqualTo(AuditOutcome.FAILURE);
    }

    private static final class SetAuthorizationService implements AuthorizationService {
        public boolean hasPermission(CurrentUser user, PermissionCode permission) { return user.permissions().contains(permission); }
        public void requirePermission(CurrentUser user, PermissionCode permission) { if (!hasPermission(user, permission)) throw new IllegalStateException("denied"); }
    }
    private static final class Events implements AuditSink { final List<AuditEvent> events = new ArrayList<>(); public void append(AuditEvent event) { events.add(event); } }
    private static final class InMemoryRepository implements AccessControlRepository {
        final Role role; final Permission permission; boolean granted;
        InMemoryRepository(Role role, Permission permission) { this.role = role; this.permission = permission; }
        public Optional<Role> findRoleByCode(String code) { return Optional.ofNullable(role.code().equals(code) ? role : null); }
        public Optional<Permission> findPermissionByCode(PermissionCode code) { return Optional.ofNullable(permission.code().equals(code) ? permission : null); }
        boolean authVersionIncremented;
        public void grantPermission(UUID roleId, UUID permissionId) { granted = role.id().equals(roleId) && permission.id().equals(permissionId); }
        public void incrementAuthVersionForRole(UUID roleId) { authVersionIncremented = role.id().equals(roleId); }
    }
}
