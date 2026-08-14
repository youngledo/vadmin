package io.github.youngledo.vadmin.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.youngledo.vadmin.contracts.auth.ExternalIdentityMapper;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.contracts.audit.AuditSink;
import io.github.youngledo.vadmin.contracts.audit.CorrelationIdProvider;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.springjpa.access.PermissionCatalogSynchronizer;
import io.github.youngledo.vadmin.platform.access.AccessControlRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import static org.mockito.Mockito.mock;

class ApplicationConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ApplicationConfiguration.class, RequiredDependencies.class);

    @Test
    void requiresAnExplicitBootstrapPasswordOutsideTheDevelopmentProfile() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("staging");

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(new ApplicationConfiguration(),
                "bootstrapPassword", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_PASSWORD");
    }

    @Test
    void doesNotRegisterAnExternalIdentityMapperWithoutExplicitLinks() {
        contextRunner.run(context -> assertThat(context.getBeansOfType(ExternalIdentityMapper.class)).isEmpty());
    }

    @Test
    void registersAnExternalIdentityMapperWhenAnExplicitLinkIsConfigured() {
        contextRunner.withPropertyValues(
                "app.identity.oidc.links[0].issuer=https://issuer.example",
                "app.identity.oidc.links[0].subject=subject-42",
                "app.identity.oidc.links[0].username=admin")
                .run(context -> assertThat(context).hasSingleBean(ExternalIdentityMapper.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class RequiredDependencies {
        @Bean AuthorizationService authorizationService() { return mock(AuthorizationService.class); }
        @Bean AccessControlRepository accessControlRepository() { return mock(AccessControlRepository.class); }
        @Bean PermissionCatalog permissionCatalog() { return mock(PermissionCatalog.class); }
        @Bean AuditSink auditSink() { return mock(AuditSink.class); }
        @Bean CorrelationIdProvider correlationIdProvider() { return mock(CorrelationIdProvider.class); }
        @Bean PlatformTransactionManager transactionManager() { return mock(PlatformTransactionManager.class); }
        @Bean EntityManager entityManager() { return mock(EntityManager.class); }
        @Bean PasswordEncoder passwordEncoder() { return mock(PasswordEncoder.class); }
        @Bean PermissionCatalogSynchronizer permissionCatalogSynchronizer() { return mock(PermissionCatalogSynchronizer.class); }
        @Bean LocalUserAccountLookup localUserAccountLookup() { return mock(LocalUserAccountLookup.class); }
    }
}
