package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("development")
class BrowserE2EIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static Playwright playwright;
    private static Browser browser;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    private BrowserContext browserContext;
    private Page page;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("app.file-storage.directory", () -> "target/e2e-test-files");
    }

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions());
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        resetData();
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
    }

    @AfterEach
    void closeContext() {
        browserContext.close();
    }

    @Test
    void adminCanAssignUserReadPermission() {
        createRole("user-readers");

        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/roles");
        grantPermission("user-readers", "system:user:read");

        waitUntil(() -> roleHasPermission("user-readers", "system:user:read"));
    }

    @Test
    void authenticatedUserLandsOnHomeRoute() {
        signInAs("admin", "change-me");

        page.waitForURL(baseUrl() + "/");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("工作台"))).isVisible();
    }

    @Test
    void workplaceShowsOnlyPermittedQuickLinks() {
        var roleId = createRole("user-readers", "system:user:read");
        createUser("workplace-reader", "workplace-password", roleId);

        signInAs("workplace-reader", "workplace-password");

        var shortcuts = page.locator("[data-testid=workplace-shortcuts]");
        assertThat(shortcuts.getByRole(AriaRole.LINK, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("用户"))).isVisible();
        org.assertj.core.api.Assertions.assertThat(shortcuts.getByRole(AriaRole.LINK).count()).isEqualTo(1);
    }

    @Test
    void readOnlyWorkspacesUseSharedPageHeaderAndGridFrame() {
        signInAs("admin", "change-me");

        assertReadOnlyWorkspace("permissions", "权限目录");
        assertReadOnlyWorkspace("audit", "审计日志");
    }

    @Test
    void authenticatedAdministratorSeesShellAndActiveNavigation() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        assertThat(page.getByText("Vaadin Admin Starter", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByLabel("当前用户菜单")).isVisible();
        assertThat(page.locator("vaadin-side-nav-item[current]")).hasText("用户");
    }

    @Test
    void unassignedUserCannotOpenUsersRoute() {
        createUser("unassigned", "unassigned-password");

        signInAs("unassigned", "unassigned-password");
        Response response = page.navigate(baseUrl() + "/users");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("无权访问"))).isVisible();
        org.assertj.core.api.Assertions.assertThat(response).isNotNull();
        org.assertj.core.api.Assertions.assertThat(response.headerValue("content-type")).contains("text/html");
    }

    @Test
    void unassignedUserCannotSeeCreateUserAction() {
        var roleId = createRole("user-readers", "system:user:read");
        createUser("read-only", "read-only-password", roleId);

        signInAs("read-only", "read-only-password");
        page.navigate(baseUrl() + "/users");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("用户"))).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("新增用户")).count()).isZero();
    }

    @Test
    void administratorCanSeeCreateUserAction() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("新增用户"))).isVisible();
    }

    @Test
    void mutatingAdministrationPagesUseSharedToolbarAndWorkspace() {
        signInAs("admin", "change-me");

        assertMutatingWorkspace("users", "用户");
        assertMutatingWorkspace("roles", "角色");
        assertMutatingWorkspace("customers", "客户");
    }

    @Test
    void selectingUserEnablesBulkActions() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        var workspace = page.locator("[data-testid=users-workspace]");
        workspace.getByText("admin", new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true)).click();

        assertThat(workspace.getByText("1 selected",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByLabel("启用所选用户")).isEnabled();
        assertThat(page.getByLabel("停用所选用户")).isEnabled();
    }

    @Test
    void userEditorShowsValidationFeedbackBeforeSubmittingInvalidValues() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增用户")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByRole(AriaRole.ALERT)).hasText("用户名和初始密码均为必填项。");
    }

    @Test
    void roleGrantCreatesAuditEntry() {
        createRole("audited-role");

        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/roles");
        grantPermission("audited-role", "system:user:read");
        waitUntil(() -> auditEntryExists("system:role:grant", "audited-role"));
    }

    @Test
    void customerCrudWithAttachment() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/customers");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();
        page.getByLabel("名称").fill("Acme E2E");
        page.getByLabel("邮箱").fill("contact@acme-e2e.test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByText("Acme E2E", new Page.GetByTextOptions().setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("客户附件")).click();
        page.locator("input[type=file]").setInputFiles(new FilePayload("brief.txt", "text/plain",
                "attachment content".getBytes(StandardCharsets.UTF_8)));

        assertThat(page.getByText("brief.txt", new Page.GetByTextOptions().setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("关闭")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("编辑客户")).click();
        page.getByLabel("名称").fill("Acme Updated");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByText("Acme Updated", new Page.GetByTextOptions().setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("删除客户")).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("删除客户"))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("删除").setExact(true)).click();
        assertThat(page.getByText("Acme Updated", new Page.GetByTextOptions().setExact(true))).not().isVisible();
    }

    private void signInAs(String username, String password) {
        page.navigate(baseUrl() + "/login");
        var loginForm = page.locator("vaadin-login-form");
        loginForm.waitFor();
        var credentials = loginForm.locator("input:not([type=hidden])");
        credentials.nth(0).fill(username);
        credentials.nth(1).fill(password);
        var loginResponse = page.waitForResponse(response -> response.url().equals(baseUrl() + "/login")
                && "POST".equals(response.request().method()),
                () -> loginForm.locator("vaadin-button[slot=submit]").click());
        org.assertj.core.api.Assertions.assertThat(loginResponse.status()).isEqualTo(302);
    }

    private void assertReadOnlyWorkspace(String route, String title) {
        page.navigate(baseUrl() + "/" + route);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(title))).isVisible();
        assertThat(page.locator("[data-testid=read-only-workspace] vaadin-grid")).isVisible();
        assertThat(page.locator("[data-testid=read-only-workspace]").getByText("0 selected",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).not().isVisible();
    }

    private void assertMutatingWorkspace(String route, String title) {
        page.navigate(baseUrl() + "/" + route);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(title))).isVisible();
        assertThat(page.locator("[data-testid=" + route + "-toolbar]")).isVisible();
        assertThat(page.locator("[data-testid=" + route + "-workspace] vaadin-grid")).isVisible();
    }

    private void grantPermission(String roleCode, String permissionCode) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("授予权限")).click();
        var dialog = page.getByRole(AriaRole.DIALOG);
        dialog.getByLabel("角色").click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(roleCode)).click();
        dialog.getByLabel("权限").click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(permissionCode)).click();
        dialog.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("保存授权")).click();
    }

    private UUID createRole(String code, String... permissions) {
        var roleId = UUID.randomUUID();
        jdbcTemplate.update("insert into roles (id, code) values (?, ?)", roleId, code);
        for (var permission : permissions) {
            jdbcTemplate.update("""
                    insert into role_permissions (role_id, permission_id)
                    select ?, id from permissions where code = ?
                    """, roleId, permission);
        }
        return roleId;
    }

    private void createUser(String username, String password, UUID... roleIds) {
        var userId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into users (id, username, password_hash, enabled, auth_version)
                values (?, ?, ?, true, 0)
                """, userId, username, passwordEncoder.encode(password));
        for (var roleId : roleIds) {
            jdbcTemplate.update("insert into user_roles (user_id, role_id) values (?, ?)", userId, roleId);
        }
    }

    private void resetData() {
        jdbcTemplate.update("delete from customer_attachments");
        jdbcTemplate.update("delete from customers");
        jdbcTemplate.update("delete from audit_entries");
        jdbcTemplate.update("delete from user_roles where user_id <> (select id from users where username = 'admin')");
        jdbcTemplate.update("delete from roles where code <> 'administrator'");
        jdbcTemplate.update("delete from users where username <> 'admin'");
    }

    private boolean roleHasPermission(String roleCode, String permissionCode) {
        var grants = jdbcTemplate.queryForObject("""
                select count(*) from role_permissions grant_item
                join roles role on role.id = grant_item.role_id
                join permissions permission on permission.id = grant_item.permission_id
                where role.code = ? and permission.code = ?
                """, Long.class, roleCode, permissionCode);
        return grants == 1;
    }

    private boolean auditEntryExists(String actionCode, String targetId) {
        var entries = jdbcTemplate.queryForObject("""
                select count(*) from audit_entries
                where action_code = ? and target_id = ? and outcome = 'SUCCESS'
                """, Long.class, actionCode, targetId);
        return entries == 1;
    }

    private void waitUntil(BooleanSupplier condition) {
        var deadline = System.nanoTime() + 5_000_000_000L;
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) {
                throw new AssertionError("condition was not met within five seconds");
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted while waiting for condition", exception);
            }
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
