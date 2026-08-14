package io.github.youngledo.vadmin.starter.administration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserAdministrationServiceTest {
    @Test
    void createsUserOnlyAfterCheckingCreatePermission() {
        var jdbcTemplate = mock(JdbcTemplate.class);
        var authorization = mock(AuthorizationService.class);
        var auditSink = mock(AuditSink.class);
        var passwordEncoder = mock(PasswordEncoder.class);
        var service = new UserAdministrationService(jdbcTemplate, authorization, auditSink, passwordEncoder);
        var actor = new CurrentUser(UUID.randomUUID(), "administrator",
                Set.of(PermissionCode.of("system:user:create")), 0);

        org.mockito.Mockito.when(passwordEncoder.encode("initial-password")).thenReturn("encoded-password");
        service.create(actor, "operator", "initial-password");

        verify(authorization).requirePermission(actor, PermissionCode.of("system:user:create"));
        verify(jdbcTemplate).update(any(String.class), any(UUID.class), eq("operator"), eq("encoded-password"));
        verify(auditSink).append(any());
    }

    @Test
    void reportsDuplicateUsernameAsFieldValidation() {
        var jdbcTemplate = mock(JdbcTemplate.class);
        var service = new UserAdministrationService(jdbcTemplate, mock(AuthorizationService.class), mock(AuditSink.class),
                mock(PasswordEncoder.class));
        var actor = new CurrentUser(UUID.randomUUID(), "administrator",
                Set.of(PermissionCode.of("system:user:create")), 0);

        org.mockito.Mockito.when(jdbcTemplate.update(any(String.class), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("users_username_key"));

        assertThatThrownBy(() -> service.create(actor, "operator", "initial-password"))
                .isInstanceOfSatisfying(BusinessFailure.class, failure -> {
                    assertThat(failure.detailKey()).isEqualTo("validation.failed");
                    assertThat(failure.fieldErrors()).containsEntry("username", "already-exists");
                });
    }
}
