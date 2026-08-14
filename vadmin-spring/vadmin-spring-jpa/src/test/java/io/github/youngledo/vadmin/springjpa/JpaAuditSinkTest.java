package io.github.youngledo.vadmin.springjpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.audit.AuditEvent;
import io.github.youngledo.vadmin.contracts.audit.AuditOutcome;
import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Map;
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

@SpringBootTest(classes = JpaAuditSinkTest.TestApplication.class)
@Testcontainers
class JpaAuditSinkTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

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
    void redactsSensitiveMetadataBeforePersistingAnAuditEntry() {
        var actorId = UUID.randomUUID();
        entityManager.createNativeQuery("""
                insert into users (id, username, password_hash, enabled, auth_version)
                values (:id, :username, :passwordHash, true, 0)
                """)
                .setParameter("id", actorId)
                .setParameter("username", "auditor")
                .setParameter("passwordHash", "not-a-real-password")
                .executeUpdate();

        auditSink.append(new AuditEvent(actorId, "system:role:grant", "role", "operator", AuditOutcome.SUCCESS,
                Instant.parse("2026-08-05T00:00:00Z"), "test-correlation",
                Map.of("requestId", "safe", "accessToken", "must-not-persist")));
        entityManager.flush();

        var metadata = entityManager.createNativeQuery("select metadata::text from audit_entries")
                .getSingleResult()
                .toString();
        assertThat(metadata).contains("requestId").doesNotContain("accessToken", "must-not-persist");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(JpaAccessControlConfiguration.class)
    static class TestApplication { }
}
