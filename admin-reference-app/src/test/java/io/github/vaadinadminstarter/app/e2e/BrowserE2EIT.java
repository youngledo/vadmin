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
import com.microsoft.playwright.options.ViewportSize;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;
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
    void workplaceShowsOnlyPermittedOperationEntries() {
        var roleId = createRole("user-readers", "system:user:read");
        createUser("workplace-reader", "workplace-password", roleId);

        signInAs("workplace-reader", "workplace-password");

        var shortcuts = page.locator("[data-testid=workplace-shortcuts]");
        assertThat(shortcuts.getByRole(AriaRole.LINK, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("用户"))).isVisible();
        assertThat(shortcuts.locator("[data-testid=workplace-entry]")).hasCount(1);
        assertThat(shortcuts.getByText("管理可登录账户及其启用状态。",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(shortcuts.getByRole(AriaRole.LINK).count()).isEqualTo(1);
    }

    @Test
    void ordersOnlyUserSeesTheExternalModuleAndCanOpenItsProtectedRoute() {
        var roleId = createRole("orders-readers", "orders:order:read");
        createUser("orders-reader", "orders-password", roleId);

        signInAs("orders-reader", "orders-password");

        assertThat(page.getByLabel("业务管理").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(page.getByLabel("系统管理")).not().isVisible();
        assertThat(page.getByLabel("客户管理")).not().isVisible();
        var shortcuts = page.locator("[data-testid=workplace-shortcuts]");
        assertThat(shortcuts.getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(shortcuts.locator("[data-testid=workplace-entry]")).hasCount(1);

        page.navigate(baseUrl() + "/orders");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(page.getByTestId("orders-workspace").locator("vaadin-grid")).isVisible();
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
        var currentNavigationItem = page.locator("vaadin-side-nav-item[current]");
        assertThat(currentNavigationItem).isVisible();
        org.assertj.core.api.Assertions.assertThat(currentNavigationItem.getAttribute("path")).isEqualTo("users");
    }

    @Test
    void desktopShellCanSwitchBetweenLightAndDarkModes() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        var userMenu = page.getByLabel("当前用户菜单");
        userMenu.locator("vaadin-menu-bar-button:not([hidden])").press("Enter");
        page.getByText("切换至深色模式", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.locator("body")).hasAttribute("theme", "dark");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--lumo-primary-color")).isEqualTo("#52b6e8");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--lumo-body-text-color")).isEqualTo("#edf2f7");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--lumo-success-color")).isEqualTo("#62c78f");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--vaadin-focus-ring-color")).isEqualTo("#7cc8ef");
        userMenu.locator("vaadin-menu-bar-button:not([hidden])").press("Enter");
        assertThat(page.getByText("切换至浅色模式", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    @Test
    void authenticatedAdministratorCanSwitchTheShellAndWorkplaceLanguage() {
        signInAs("admin", "change-me");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("工作台"))).isVisible();
        page.getByLabel("语言").click();
        page.getByText("English", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Workplace"))).isVisible();
        assertThat(page.getByLabel("System administration").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Users"))).isVisible();
        assertThat(page.getByText("Manage sign-in accounts and their enabled status.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    @Test
    void authenticatedAdministratorCanSwitchAnOpenUsersViewLanguageWithoutNavigation() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("用户"))).isVisible();
        assertThat(page.getByLabel("搜索用户")).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("用户名"))).isVisible();
        assertThat(page.getByLabel("查看用户详情：admin")).isVisible();
        assertThat(page.locator(".admin-shell-location")).hasText("用户");
        assertThat(page).hasTitle("用户");

        page.getByLabel("语言").click();
        page.getByText("English", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Users"))).isVisible();
        assertThat(page.getByLabel("Search users")).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Username"))).isVisible();
        assertThat(page.getByLabel("View user details: admin")).isVisible();
        assertThat(page.locator(".admin-shell-location")).hasText("Users");
        assertThat(page).hasTitle("Users");
    }

    @Test
    void ordersWorkspaceRefreshesShellAndOperationLabelsAfterLanguageSelection() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/orders");

        assertThat(page.getByLabel("当前用户菜单")).isVisible();
        assertThat(page.getByLabel("业务管理").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("操作"))).isVisible();

        page.getByLabel("语言").click();
        page.getByText("English", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.getByLabel("Current user menu")).isVisible();
        assertThat(page.getByLabel("Business administration").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Orders"))).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Orders"))).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Actions"))).isVisible();
        assertThat(page).hasTitle("Orders");
    }

    @Test
    void ordersWorkspaceRemainsVisibleWithDistinctDarkThemeTokens() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/orders");

        var workspace = page.getByTestId("orders-workspace");
        assertThat(workspace).isVisible();
        var lightSurface = computedThemeVariable("--admin-surface");
        var lightText = computedThemeVariable("--admin-text-primary");
        org.assertj.core.api.Assertions.assertThat(lightSurface).isNotEqualTo(lightText);

        var userMenu = page.getByLabel("当前用户菜单");
        userMenu.locator("vaadin-menu-bar-button:not([hidden])").press("Enter");
        page.getByText("切换至深色模式", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.locator("body")).hasAttribute("theme", "dark");
        assertThat(workspace).isVisible();
        var darkSurface = computedThemeVariable("--admin-surface");
        var darkText = computedThemeVariable("--admin-text-primary");
        org.assertj.core.api.Assertions.assertThat(darkSurface).isNotEqualTo(lightSurface);
        org.assertj.core.api.Assertions.assertThat(darkText).isNotEqualTo(lightText);
        org.assertj.core.api.Assertions.assertThat(darkSurface).isNotEqualTo(darkText);
    }

    @Test
    void narrowShellKeepsNavigationReachable() {
        useNarrowBrowser();
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");

        assertThat(page.getByLabel("切换导航")).isVisible();
        assertThat(page.getByLabel("当前用户菜单")).isVisible();
        page.getByLabel("切换导航").click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("客户"))).isVisible();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("客户")).click();
        page.waitForURL(baseUrl() + "/customers");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("客户"))).isVisible();
    }

    @Test
    void authenticatedAdministratorSeesEachNavigationGroupHeadingOnce() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");
        assertThat(page.locator("vaadin-side-nav")).hasCount(4);

        assertNavigationGroupHeadingOccursOnce("工作空间");
        assertNavigationGroupHeadingOccursOnce("系统管理");
        assertNavigationGroupHeadingOccursOnce("客户管理");
        assertNavigationGroupHeadingOccursOnce("业务管理");
    }

    @Test
    void anonymousVisitorCanLoadLoginWithoutAuthenticatedShellControls() {
        page.navigate(baseUrl() + "/login");

        assertThat(page.locator("vaadin-login-form")).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.getByLabel("当前用户菜单").count()).isZero();
        org.assertj.core.api.Assertions.assertThat(page.locator("vaadin-side-nav").count()).isZero();
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
        workspace.getByRole(AriaRole.CHECKBOX, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("Select Row")).click();

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
    void userDetailsAndStatusChangeRequireExplicitConfirmation() {
        createUser("managed-user", "managed-password");

        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/users");
        page.getByLabel("查看用户详情：managed-user").click();

        var details = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("用户详情"));
        assertThat(details.getByLabel("用户名")).hasValue("managed-user");
        assertThat(details.getByLabel("状态")).hasValue("启用");
        details.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("关闭")).click();

        page.getByLabel("停用用户：managed-user").click();
        var confirmation = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("停用用户"));
        assertThat(confirmation.getByText("该用户将无法登录。",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
        confirmation.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("取消")).click();
        org.assertj.core.api.Assertions.assertThat(userIsEnabled("managed-user")).isTrue();

        page.getByLabel("停用用户：managed-user").click();
        page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("停用用户"))
                .getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("停用")).click();
        assertThat(page.getByText("用户已停用。", new Page.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("停用用户")).count()).isZero();
        waitUntil(() -> !userIsEnabled("managed-user"));
    }

    @Test
    void roleDetailsPresentReadableRoleMetadata() {
        createRole("role-details", "system:user:read");

        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/roles");
        page.getByLabel("查看角色详情：role-details").click();

        var details = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("角色详情"));
        assertThat(details.getByLabel("角色代码")).hasValue("role-details");
        assertThat(details.getByLabel("权限数量")).hasValue("1");
        var grantedPermissions = details.getByLabel("已授予权限");
        assertThat(grantedPermissions).isVisible();
        assertThat(grantedPermissions).hasValue("system:user:read");
        assertThat(grantedPermissions).not().isEditable();
    }

    @Test
    void emptyCustomerWorkspaceShowsTheGridEmptyPresentation() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/customers");

        var workspace = page.locator("[data-testid=customers-workspace]");
        assertThat(workspace.locator("vaadin-grid")).isVisible();
        assertThat(workspace.getByText("暂无数据", new com.microsoft.playwright.Locator.GetByTextOptions()
                .setExact(true))).isVisible();
    }

    @Test
    void systemFailureRouteUsesTheFailurePresentation() {
        signInAs("admin", "change-me");
        page.navigate(baseUrl() + "/system-error");

        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("系统暂时不可用"))).isVisible();
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
        page.getByLabel("查看客户详情：Acme E2E").click();
        var details = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("客户详情"));
        assertThat(details.getByLabel("名称")).hasValue("Acme E2E");
        assertThat(details.getByLabel("邮箱")).hasValue("contact@acme-e2e.test");
        details.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("关闭")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("客户附件")).click();
        page.locator("input[type=file]").setInputFiles(new FilePayload("brief.txt", "text/plain",
                "attachment content".getBytes(StandardCharsets.UTF_8)));

        assertThat(page.getByText("brief.txt", new Page.GetByTextOptions().setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("关闭")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("编辑客户")).click();
        page.getByLabel("名称").fill("Acme Updated");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByText("Acme Updated", new Page.GetByTextOptions().setExact(true))).isVisible();
        var customerWorkspace = page.getByTestId("customers-workspace");
        customerWorkspace.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("删除客户")).click();
        var deletionDialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("删除客户"));
        assertThat(deletionDialog.getByRole(AriaRole.HEADING,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("删除客户"))).isVisible();
        deletionDialog.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("取消")).click();
        assertThat(deletionDialog).not().isVisible();
        assertThat(page.getByText("Acme Updated", new Page.GetByTextOptions().setExact(true))).isVisible();
        customerWorkspace.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("删除客户")).click();
        page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("删除客户"))
                .getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("删除客户")).click();
        assertThat(page.getByText("客户已删除。", new Page.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("删除客户")).count()).isZero();
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

    private void useNarrowBrowser() {
        browserContext.close();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(390, 844))
                .setIsMobile(true));
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
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

    private void assertNavigationGroupHeadingOccursOnce(String heading) {
        var occurrenceCount = Stream.concat(
                        page.locator(".admin-drawer-section").allInnerTexts().stream(),
                        page.locator("vaadin-side-nav").allInnerTexts().stream())
                .mapToInt(text -> text.split(heading, -1).length - 1)
                .sum();
        org.assertj.core.api.Assertions.assertThat(occurrenceCount).isEqualTo(1);
    }

    private String computedThemeVariable(String name) {
        return (String) page.locator("body")
                .evaluate("(element, name) => getComputedStyle(element).getPropertyValue(name).trim()", name);
    }

    private void grantPermission(String roleCode, String permissionCode) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("授予权限")).click();
        var dialog = page.getByRole(AriaRole.DIALOG);
        dialog.getByLabel("角色").click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(roleCode)).click();
        var permission = dialog.getByLabel("权限");
        permission.click();
        permission.pressSequentially(permissionCode);
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

    private boolean userIsEnabled(String username) {
        var enabled = jdbcTemplate.queryForObject("select enabled from users where username = ?", Boolean.class, username);
        return Boolean.TRUE.equals(enabled);
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
