package io.github.vaadinadminstarter.springjpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import jakarta.persistence.EntityManager;
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

@SpringBootTest(classes = JpaLocalUserAccountLookupTest.TestApplication.class)
@Testcontainers
class JpaLocalUserAccountLookupTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private LocalUserAccountLookup lookup;

    @Autowired
    private LocalUserSessionLookup sessionLookup;

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
    void loadsPasswordStateVersionAndPermissionsForUsername() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        entityManager.createNativeQuery("""
                insert into users (id, username, password_hash, enabled, auth_version)
                values (:id, 'operator', 'stored-hash', true, 7)
                """).setParameter("id", userId).executeUpdate();
        entityManager.createNativeQuery("insert into roles (id, code) values (:id, 'operator')")
                .setParameter("id", roleId).executeUpdate();
        entityManager.createNativeQuery("""
                insert into permissions (id, code, system_managed)
                values (:id, 'system:user:read', true)
                """).setParameter("id", permissionId).executeUpdate();
        entityManager.createNativeQuery("insert into user_roles (user_id, role_id) values (:userId, :roleId)")
                .setParameter("userId", userId).setParameter("roleId", roleId).executeUpdate();
        entityManager.createNativeQuery("""
                insert into role_permissions (role_id, permission_id)
                values (:roleId, :permissionId)
                """).setParameter("roleId", roleId).setParameter("permissionId", permissionId).executeUpdate();

        var account = lookup.findByUsername("operator").orElseThrow();

        assertThat(account.userId()).isEqualTo(userId);
        assertThat(account.passwordHash()).isEqualTo("stored-hash");
        assertThat(account.authVersion()).isEqualTo(7);
        assertThat(account.permissions()).extracting(permission -> permission.value())
                .containsExactly("system:user:read");
        assertThat(sessionLookup.findSessionByUserId(userId)).hasValueSatisfying(session -> {
            assertThat(session.enabled()).isTrue();
            assertThat(session.authVersion()).isEqualTo(7);
        });
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(JpaAccessControlConfiguration.class)
    static class TestApplication { }
}
