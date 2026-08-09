package io.github.vaadinadminstarter.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.app.customer.CustomerService;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.platform.access.GrantPermissionCommand;
import io.github.vaadinadminstarter.platform.access.GrantPermissionUseCase;
import io.github.vaadinadminstarter.springsecurity.auth.SpringAuthorizationService;
import javax.sql.DataSource;
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
    private DataSource dataSource;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private GrantPermissionUseCase grantPermissionUseCase;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("app.file-storage.directory", () -> "target/test-files");
    }

    @Test
    void startsWithThePostgresqlAdapterAndBootstrapAdministrator() {
        assertThat(accountLookup.findByUsername("admin")).isPresent();
    }

    @Test
    void assemblesBuiltInAdministrationModulesAndTheirPermissionCatalog() {
        var administrator = new CurrentUser(UUID.randomUUID(), "admin", Set.of(
                PermissionCode.of("system:user:read"), PermissionCode.of("system:role:read"),
                PermissionCode.of("system:permission:read"), PermissionCode.of("system:audit:read"),
                PermissionCode.of("customer:customer:read")), 0);

        assertThat(modules.pagesVisibleTo(administrator, new SpringAuthorizationService()))
                .extracting(page -> page.route())
                .containsExactly("users", "roles", "permissions", "audit", "customers");
        assertThat(modules.permissionCatalog()).contains(
                PermissionCode.of("system:user:read"),
                PermissionCode.of("customer:attachment:upload"));
    }

    @Test
    void installsTheIndependentCustomerModule() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select count(*) from information_schema.tables where table_name = 'customers'")) {
            try (var result = statement.executeQuery()) {
                result.next();
                assertThat(result.getInt(1)).isOne();
            }
        }
        assertThat(applicationContext.containsBean("customerService")).isTrue();
    }

    @Test
    void exposesTheDevelopmentFileStorageAdapter() {
        assertThat(applicationContext.getBean(FileStorage.class)).isNotNull();
    }

    @Test
    void storesCustomerAttachmentMetadataAndStreamsItsContent() throws Exception {
        var administrator = accountLookup.findByUsername("admin").orElseThrow();
        var actor = new CurrentUser(administrator.userId(), administrator.username(), Set.of(
                PermissionCode.of("customer:customer:create"), PermissionCode.of("customer:customer:read"),
                PermissionCode.of("customer:attachment:upload")), administrator.authVersion());
        var customer = customerService.create(actor, "Acme", "contact@acme.test");
        var stored = fileStorage.store("brief.txt", "text/plain",
                new java.io.ByteArrayInputStream("attached content".getBytes()));

        var attachment = customerService.attach(actor, customer.id(), stored);

        assertThat(customerService.attachments(actor, customer.id()))
                .extracting(item -> item.filename()).containsExactly("brief.txt");
        try (var content = customerService.openAttachment(actor, attachment.id()).content()) {
            assertThat(content.readAllBytes()).isEqualTo("attached content".getBytes());
        }
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
    void removesStoredAttachmentWhenAttachmentMetadataCannotBeCreated() throws Exception {
        var administrator = accountLookup.findByUsername("admin").orElseThrow();
        var actor = new CurrentUser(administrator.userId(), administrator.username(), Set.of(
                PermissionCode.of("customer:attachment:upload")), administrator.authVersion());
        var stored = fileStorage.store("orphan.txt", "text/plain",
                new java.io.ByteArrayInputStream("orphan".getBytes()));

        assertThatThrownBy(() -> customerService.attach(actor, UUID.randomUUID(), stored))
                .hasMessageContaining("customer.not-found");
        assertThatThrownBy(() -> fileStorage.open(stored.id()))
                .isInstanceOf(java.io.UncheckedIOException.class);
    }
}
