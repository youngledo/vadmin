package io.github.vaadinadminstarter.springjpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.platform.access.AccessControlRepository;
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

@SpringBootTest(classes = JpaAccessControlRepositoryTest.TestApplication.class)
@Testcontainers
class JpaAccessControlRepositoryTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AccessControlRepository repository;

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
    void findsRoleAndPermissionAndGrantsTheRelationship() {
        var roleId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        entityManager.createNativeQuery("insert into roles (id, code) values (:id, :code)")
                .setParameter("id", roleId)
                .setParameter("code", "operator")
                .executeUpdate();
        entityManager.createNativeQuery("""
                insert into permissions (id, code, system_managed)
                values (:id, :code, true)
                """)
                .setParameter("id", permissionId)
                .setParameter("code", "system:user:read")
                .executeUpdate();

        var role = repository.findRoleByCode("operator");
        var permission = repository.findPermissionByCode(PermissionCode.of("system:user:read"));
        repository.grantPermission(role.orElseThrow().id(), permission.orElseThrow().id());
        entityManager.flush();

        var grants = ((Number) entityManager.createNativeQuery("select count(*) from role_permissions")
                .getSingleResult()).longValue();
        assertThat(grants).isOne();
    }

    @Test
    @Transactional
    void incrementsAuthenticationVersionForUsersAssignedToTheChangedRole() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        entityManager.createNativeQuery("""
                insert into users (id, username, password_hash, enabled, auth_version)
                values (:id, 'operator', 'stored-hash', true, 4)
                """).setParameter("id", userId).executeUpdate();
        entityManager.createNativeQuery("insert into roles (id, code) values (:id, 'operator')")
                .setParameter("id", roleId).executeUpdate();
        entityManager.createNativeQuery("insert into user_roles (user_id, role_id) values (:userId, :roleId)")
                .setParameter("userId", userId).setParameter("roleId", roleId).executeUpdate();

        repository.incrementAuthVersionForRole(roleId);

        assertThat(entityManager.createNativeQuery("select auth_version from users where id = :id")
                .setParameter("id", userId).getSingleResult()).isEqualTo(5L);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(JpaAccessControlConfiguration.class)
    static class TestApplication { }
}
