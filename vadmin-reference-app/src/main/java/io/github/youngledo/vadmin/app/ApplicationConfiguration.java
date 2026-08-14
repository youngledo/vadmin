package io.github.youngledo.vadmin.app;

import io.github.youngledo.vadmin.contracts.audit.AuditEvent;
import io.github.youngledo.vadmin.contracts.audit.AuditOutcome;
import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import io.github.youngledo.vadmin.contracts.audit.CorrelationIdProvider;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.ExternalIdentityMapper;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.app.auth.ConfiguredExternalIdentityMapper;
import io.github.youngledo.vadmin.app.auth.OidcIdentityLinkProperties;
import io.github.youngledo.vadmin.platform.access.AccessControlRepository;
import io.github.youngledo.vadmin.platform.access.GrantPermissionService;
import io.github.youngledo.vadmin.platform.access.GrantPermissionUseCase;
import io.github.youngledo.vadmin.springjpa.access.PermissionCatalogSynchronizer;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OidcIdentityLinkProperties.class)
public class ApplicationConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "app.identity.oidc.links[0]", name = "issuer")
    ExternalIdentityMapper configuredExternalIdentityMapper(OidcIdentityLinkProperties properties,
                                                            LocalUserAccountLookup accountLookup) {
        return new ConfiguredExternalIdentityMapper(properties.links(), accountLookup);
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
