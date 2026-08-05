package io.github.vaadinadminstarter.springjpa;

import io.github.vaadinadminstarter.contracts.audit.AuditMetadataRedactor;
import io.github.vaadinadminstarter.contracts.audit.AuditSink;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import io.github.vaadinadminstarter.platform.access.AccessControlRepository;
import io.github.vaadinadminstarter.springjpa.access.JpaAccessControlRepository;
import io.github.vaadinadminstarter.springjpa.access.JpaLocalUserAccountLookup;
import io.github.vaadinadminstarter.springjpa.access.JpaLocalUserSessionLookup;
import io.github.vaadinadminstarter.springjpa.access.PermissionCatalogSynchronizer;
import io.github.vaadinadminstarter.springjpa.audit.JpaAuditSink;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JpaAccessControlConfiguration {
    @Bean
    AccessControlRepository accessControlRepository(EntityManager entityManager) {
        return new JpaAccessControlRepository(entityManager);
    }

    @Bean
    LocalUserAccountLookup localUserAccountLookup(EntityManager entityManager) {
        return new JpaLocalUserAccountLookup(entityManager);
    }

    @Bean
    LocalUserSessionLookup localUserSessionLookup(EntityManager entityManager) {
        return new JpaLocalUserSessionLookup(entityManager);
    }

    @Bean
    AuditMetadataRedactor auditMetadataRedactor() {
        return new AuditMetadataRedactor();
    }

    @Bean
    AuditSink auditSink(EntityManager entityManager, AuditMetadataRedactor redactor) {
        return new JpaAuditSink(entityManager, redactor);
    }

    @Bean
    PermissionCatalogSynchronizer permissionCatalogSynchronizer(EntityManager entityManager) {
        return new PermissionCatalogSynchronizer(entityManager);
    }
}
