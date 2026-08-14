package io.github.youngledo.vadmin.springjpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.platform.access.GrantPermissionCommand;
import io.github.youngledo.vadmin.platform.access.GrantPermissionService;
import io.github.youngledo.vadmin.platform.access.AccessControlRepository;
import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = JpaGrantPermissionIntegrationTest.TestApplication.class)
@Testcontainers
class JpaGrantPermissionIntegrationTest {
    private static final PermissionCode GRANT = PermissionCode.of("system:role:grant");
    private static final PermissionCode USER_READ = PermissionCode.of("system:user:read");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AccessControlRepository repository;

    @Autowired
    private AuditSink auditSink;

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    @Transactional
    void grantsPermissionAndAuditsSuccessInTheDatabase() {
        var actor = seedAccessControlData();
        service(true).grant(actor, new GrantPermissionCommand("operator", USER_READ));
        entityManager.flush();

        assertThat(count("role_permissions")).isOne();
        assertThat(entityManager.createNativeQuery("select outcome from audit_entries").getSingleResult())
                .isEqualTo("SUCCESS");
    }

    @Test
    @Transactional
    void deniedGrantDoesNotCreateRelationshipAndAuditsDenial() {
        var actor = seedAccessControlData();

        assertThatThrownBy(() -> service(false).grant(actor, new GrantPermissionCommand("operator", USER_READ)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("denied");
        entityManager.flush();

        assertThat(count("role_permissions")).isZero();
        assertThat(entityManager.createNativeQuery("select outcome from audit_entries").getSingleResult())
                .isEqualTo("DENIED");
    }

    private CurrentUser seedAccessControlData() {
        var actorId = UUID.randomUUID();
        entityManager.createNativeQuery("""
                insert into users (id, username, password_hash, enabled, auth_version)
                values (:id, :username, :passwordHash, true, 0)
                """)
                .setParameter("id", actorId)
                .setParameter("username", "administrator")
                .setParameter("passwordHash", "not-a-real-password")
                .executeUpdate();
        entityManager.createNativeQuery("insert into roles (id, code) values (:id, 'operator')")
                .setParameter("id", UUID.randomUUID())
                .executeUpdate();
        entityManager.createNativeQuery("""
                insert into permissions (id, code, system_managed)
                values (:id, :code, true)
                """)
                .setParameter("id", UUID.randomUUID())
                .setParameter("code", USER_READ.value())
                .executeUpdate();
        return new CurrentUser(actorId, "administrator", Set.of(GRANT), 0);
    }

    private GrantPermissionService service(boolean permitted) {
        return new GrantPermissionService(new FixedAuthorizationService(permitted), repository,
                new PermissionCatalog(Set.of(GRANT, USER_READ)), auditSink);
    }

    private long count(String table) {
        return ((Number) entityManager.createNativeQuery("select count(*) from " + table).getSingleResult()).longValue();
    }

    private record FixedAuthorizationService(boolean permitted) implements AuthorizationService {
        @Override
        public boolean hasPermission(CurrentUser user, PermissionCode permission) {
            return permitted;
        }

        @Override
        public void requirePermission(CurrentUser user, PermissionCode permission) {
            if (!permitted) {
                throw new IllegalStateException("denied");
            }
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(JpaAccessControlConfiguration.class)
    static class TestApplication { }
}
