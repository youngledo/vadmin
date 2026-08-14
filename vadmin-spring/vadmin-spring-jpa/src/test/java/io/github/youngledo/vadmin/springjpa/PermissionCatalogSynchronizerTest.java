package io.github.youngledo.vadmin.springjpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.springjpa.access.PermissionCatalogSynchronizer;
import jakarta.persistence.EntityManager;
import java.util.Set;
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

@SpringBootTest(classes = PermissionCatalogSynchronizerTest.TestApplication.class)
@Testcontainers
class PermissionCatalogSynchronizerTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PermissionCatalogSynchronizer synchronizer;

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
    void insertsMissingCatalogPermissionsAsSystemManaged() {
        synchronizer.synchronize(new PermissionCatalog(Set.of(PermissionCode.of("system:user:read"))));
        entityManager.flush();

        var row = (Object[]) entityManager.createNativeQuery("select code, system_managed from permissions")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("system:user:read");
        assertThat(row[1]).isEqualTo(true);
    }

    @Test
    @Transactional
    void rejectsChangesToSystemManagedPermissions() {
        synchronizer.synchronize(new PermissionCatalog(Set.of(PermissionCode.of("system:user:read"))));
        entityManager.flush();

        assertThatThrownBy(() -> synchronizer.requireCustomerManaged("system:user:read"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("system-managed");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(JpaAccessControlConfiguration.class)
    static class TestApplication { }
}
