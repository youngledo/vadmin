package io.github.vaadinadminstarter.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import io.github.vaadinadminstarter.starter.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.platform.access.GrantPermissionCommand;
import io.github.vaadinadminstarter.platform.access.GrantPermissionUseCase;
import io.github.vaadinadminstarter.springsecurity.auth.SpringAuthorizationService;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("development")
class ApplicationContextIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private LocalUserAccountLookup accountLookup;

    @Autowired
    private AdminModuleRegistry modules;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GrantPermissionUseCase grantPermissionUseCase;

    @Autowired
    private AdministrationQueryService administrationQueries;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void startsWithThePostgresqlAdapterAndBootstrapAdministrator() {
        assertThat(accountLookup.findByUsername("admin")).isPresent();
    }

    @Test
    void readsBootstrapAuditEntriesThroughThePagedQuery() {
        assertThat(administrationQueries.audit(
                new io.github.vaadinadminstarter.contracts.navigation.PagedQuery(0, 10, "occurred_at", false, java.util.Map.of()))
                .items()).isNotEmpty();
    }

    @Test
    void assemblesBuiltInAdministrationModulesAndTheirPermissionCatalog() {
        var administrator = new CurrentUser(UUID.randomUUID(), "admin", Set.of(
                PermissionCode.of("system:user:read"), PermissionCode.of("system:role:read"),
                PermissionCode.of("system:permission:read"), PermissionCode.of("system:audit:read")), 0);

        assertThat(modules.pagesVisibleTo(administrator, new SpringAuthorizationService()))
                .extracting(page -> page.route())
                .containsExactly("users", "roles", "permissions", "audit");
        assertThat(modules.permissionCatalog()).contains(
                PermissionCode.of("system:user:read"), PermissionCode.of("system:audit:read"));
    }

    @Test
    void grantsRolePermissionWithinAnApplicationTransaction() {
        var administrator = accountLookup.findByUsername("admin").orElseThrow();
        var actor = new CurrentUser(administrator.userId(), administrator.username(), Set.of(
                PermissionCode.of("system:role:grant")), administrator.authVersion());

        assertThatCode(() -> grantPermissionUseCase.grant(actor,
                new GrantPermissionCommand("administrator", PermissionCode.of("system:user:read"))))
                .doesNotThrowAnyException();
    }

    @Test
    void receivesTheDefaultStarterHostLayout() {
        assertThat(applicationContext.getBean(AdminHostLayout.class).layoutType().getName())
                .isEqualTo("io.github.vaadinadminstarter.starter.views.DefaultMainLayout");
        assertThat(applicationContext.getBean("adminModuleNavigationAccessChecker", NavigationAccessChecker.class))
                .isNotNull();
    }
}
