package io.github.vaadinadminstarter.app;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.audit.CorrelationIdProvider;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.app.file.LocalFileStorage;
import io.github.vaadinadminstarter.flow.navigation.PageDefinition;
import io.github.vaadinadminstarter.flow.navigation.PageRegistry;
import io.github.vaadinadminstarter.platform.access.AccessControlRepository;
import io.github.vaadinadminstarter.platform.access.GrantPermissionService;
import io.github.vaadinadminstarter.platform.access.GrantPermissionUseCase;
import io.github.vaadinadminstarter.springjpa.access.PermissionCatalogSynchronizer;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {
    @Bean
    PermissionCatalog permissionCatalog() {
        return new PermissionCatalog(Set.of(
                PermissionCode.of("system:user:read"),
                PermissionCode.of("system:user:create"),
                PermissionCode.of("system:user:update"),
                PermissionCode.of("system:role:read"),
                PermissionCode.of("system:role:grant"),
                PermissionCode.of("system:permission:read"),
                PermissionCode.of("system:audit:read"),
                PermissionCode.of("customer:customer:read"),
                PermissionCode.of("customer:customer:create"),
                PermissionCode.of("customer:customer:update"),
                PermissionCode.of("customer:customer:delete"),
                PermissionCode.of("customer:attachment:upload")));
    }

    @Bean
    FileStorage fileStorage(Environment environment) {
        return new LocalFileStorage(Path.of(environment.getRequiredProperty("app.file-storage.directory")));
    }

    @Bean
    PageRegistry pageRegistry() {
        var pages = java.util.List.of(
                new PageDefinition("system-users", "nav.users", "users", 100, "users",
                        PermissionCode.of("system:user:read")),
                new PageDefinition("system-roles", "nav.roles", "shield", 200, "roles",
                        PermissionCode.of("system:role:read")),
                new PageDefinition("system-permissions", "nav.permissions", "key", 300, "permissions",
                        PermissionCode.of("system:permission:read")),
                new PageDefinition("system-audit", "nav.audit", "clock", 400, "audit",
                        PermissionCode.of("system:audit:read")),
                new PageDefinition("customers", "nav.customers", "briefcase", 500, "customers",
                        PermissionCode.of("customer:customer:read")));
        var registry = new PageRegistry(pages);
        registry.validate();
        return registry;
    }

    @Bean
    GrantPermissionUseCase grantPermissionUseCase(AuthorizationService authorization, AccessControlRepository repository,
                                                  PermissionCatalog catalog, AuditSink auditSink,
                                                  CorrelationIdProvider correlationIdProvider,
                                                  PlatformTransactionManager transactionManager) {
        var delegate = new GrantPermissionService(authorization, repository, catalog, auditSink, correlationIdProvider);
        var transactionTemplate = new TransactionTemplate(transactionManager);
        return (actor, command) -> transactionTemplate.executeWithoutResult(status -> delegate.grant(actor, command));
    }

    @Bean
    @Order(0)
    CommandLineRunner synchronizePermissionCatalog(PermissionCatalogSynchronizer synchronizer, PermissionCatalog catalog) {
        return args -> synchronizer.synchronize(catalog);
    }

    @Bean
    @Order(1)
    CommandLineRunner bootstrapAdministrator(EntityManager entityManager, PermissionCatalog catalog,
                                              PasswordEncoder passwordEncoder, AuditSink auditSink,
                                              CorrelationIdProvider correlationIdProvider, Environment environment,
                                              PlatformTransactionManager transactionManager) {
        return args -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var count = ((Number) entityManager.createNativeQuery("select count(*) from users").getSingleResult()).longValue();
            if (count != 0) {
                return;
            }
            var password = bootstrapPassword(environment);
            var administratorId = UUID.randomUUID();
            var administratorRoleId = UUID.randomUUID();
            entityManager.createNativeQuery("""
                    insert into users (id, username, password_hash, enabled, auth_version)
                    values (:id, 'admin', :passwordHash, true, 0)
                    """)
                    .setParameter("id", administratorId)
                    .setParameter("passwordHash", passwordEncoder.encode(password))
                    .executeUpdate();
            entityManager.createNativeQuery("insert into roles (id, code) values (:id, 'administrator')")
                    .setParameter("id", administratorRoleId)
                    .executeUpdate();
            entityManager.createNativeQuery("insert into user_roles (user_id, role_id) values (:userId, :roleId)")
                    .setParameter("userId", administratorId)
                    .setParameter("roleId", administratorRoleId)
                    .executeUpdate();
            for (var permission : catalog.all()) {
                var permissionId = entityManager.createNativeQuery("select id from permissions where code = :code")
                        .setParameter("code", permission.value())
                        .getSingleResult();
                entityManager.createNativeQuery("insert into role_permissions (role_id, permission_id) values (:roleId, :permissionId)")
                        .setParameter("roleId", administratorRoleId)
                        .setParameter("permissionId", permissionId)
                        .executeUpdate();
            }
            auditSink.append(new AuditEvent(administratorId, "system:bootstrap:create", "user", administratorId.toString(),
                    AuditOutcome.SUCCESS, Instant.now(), correlationIdProvider.currentCorrelationId(), Map.of("bootstrap", "true")));
        });
    }

    private String bootstrapPassword(Environment environment) {
        var development = Arrays.asList(environment.getActiveProfiles()).contains("development");
        var password = development ? environment.getProperty("app.bootstrap.password", "change-me")
                : environment.getProperty("APP_BOOTSTRAP_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("APP_BOOTSTRAP_PASSWORD is required for an empty non-development database");
        }
        return password;
    }
}
