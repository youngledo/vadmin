package io.github.vaadinadminstarter.verification.consumer;

import io.github.vaadinadminstarter.contracts.audit.AuditEvent;
import io.github.vaadinadminstarter.contracts.audit.AuditOutcome;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.audit.CorrelationIdProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.springboot.SpringBootErrorConfiguration;
import io.github.vaadinadminstarter.springjpa.JpaAccessControlConfiguration;
import io.github.vaadinadminstarter.springjpa.access.PermissionCatalogSynchronizer;
import io.github.vaadinadminstarter.springsecurity.SpringSecurityConfiguration;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
@Import({SpringBootErrorConfiguration.class, JpaAccessControlConfiguration.class, SpringSecurityConfiguration.class})
public class StandaloneConsumerConfiguration {
    @Bean
    AdminHostLayout adminHostLayout() {
        return new AdminHostLayout(StandaloneConsumerLayout.class);
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
                                              CorrelationIdProvider correlationIdProvider,
                                              PlatformTransactionManager transactionManager) {
        return args -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var count = ((Number) entityManager.createNativeQuery("select count(*) from users").getSingleResult()).longValue();
            if (count != 0) {
                return;
            }
            var userId = UUID.randomUUID();
            var roleId = UUID.randomUUID();
            entityManager.createNativeQuery("""
                    insert into users (id, username, password_hash, enabled, auth_version)
                    values (:id, 'admin', :passwordHash, true, 0)
                    """)
                    .setParameter("id", userId)
                    .setParameter("passwordHash", passwordEncoder.encode("change-me"))
                    .executeUpdate();
            entityManager.createNativeQuery("insert into roles (id, code) values (:id, 'administrator')")
                    .setParameter("id", roleId)
                    .executeUpdate();
            entityManager.createNativeQuery("insert into user_roles (user_id, role_id) values (:userId, :roleId)")
                    .setParameter("userId", userId)
                    .setParameter("roleId", roleId)
                    .executeUpdate();
            for (var permission : catalog.all()) {
                var permissionId = entityManager.createNativeQuery("select id from permissions where code = :code")
                        .setParameter("code", permission.value())
                        .getSingleResult();
                entityManager.createNativeQuery("insert into role_permissions (role_id, permission_id) values (:roleId, :permissionId)")
                        .setParameter("roleId", roleId)
                        .setParameter("permissionId", permissionId)
                        .executeUpdate();
            }
            auditSink.append(new AuditEvent(userId, "consumer:bootstrap:create", "user", userId.toString(),
                    AuditOutcome.SUCCESS, Instant.now(), correlationIdProvider.currentCorrelationId(),
                    Map.of("bootstrap", "true")));
        });
    }
}
