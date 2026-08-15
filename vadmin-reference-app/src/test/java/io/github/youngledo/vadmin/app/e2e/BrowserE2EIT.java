package io.github.youngledo.vadmin.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ViewportSize;
import java.util.UUID;
import io.github.youngledo.vadmin.app.testsupport.PlaywrightBrowserSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.github.youngledo.vadmin.app.fixture.ExternalSampleFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("development")
@Import(ExternalSampleFixture.class)
class BrowserE2EIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    private static Playwright playwright;
    private static Browser browser;

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PasswordEncoder passwordEncoder;
    @LocalServerPort int port;
    private BrowserContext browserContext;
    private Page page;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeAll
    static void launchBrowser() {
        playwright = PlaywrightBrowserSupport.createPlaywright();
        browser = PlaywrightBrowserSupport.launchChromium(playwright);
    }

    @AfterAll
    static void closeBrowser() { browser.close(); playwright.close(); }

    @BeforeEach
    void setUp() {
        resetData();
        browserContext = browser.newContext(new Browser.NewContextOptions().setLocale("zh-CN"));
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
    }

    @AfterEach
    void closeContext() { browserContext.close(); }

    @Test
    void starterConsumerReceivesTheCompleteSystemAdministrationShell() {
        signInAs("admin", "change-me");
        assertThat(page.getByText("VAdmin", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByLabel("系统管理").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("用户"))).isVisible();
        assertThat(page.getByLabel("系统管理").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("角色"))).isVisible();
        assertThat(page.getByLabel("示例业务").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("示例记录"))).isVisible();
        assertThat(page.getByTestId("workplace-shortcuts").getByText("示例记录",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
        page.navigate(baseUrl() + "/sample");
        assertThat(page.getByTestId("external-sample-workspace")).isVisible();
        page.navigate(baseUrl() + "/users");
        assertThat(page.getByTestId("users-workspace").locator("vaadin-grid")).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增用户"))).isVisible();
    }

    @Test
    void starterShellTranslatesAndSwitchesColorMode() {
        signInAs("admin", "change-me");
        openShellMenu("admin-language-menu");
        page.getByText("English", new Page.GetByTextOptions().setExact(true)).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Workplace"))).isVisible();
        assertThat(page.getByLabel("System administration").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Users"))).isVisible();
        openShellMenu("admin-appearance-menu");
        page.getByText("Dark mode", new Page.GetByTextOptions().setExact(true)).click();
        assertThat(page.locator("html")).hasAttribute("theme", "dark");
    }

    @Test
    void routeAndNavigationAreFilteredForAnUnassignedUser() {
        createUser("unassigned", "password", null);
        signInAs("unassigned", "password");
        assertThat(page.getByLabel("系统管理")).not().isVisible();
        page.navigate(baseUrl() + "/users");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("无权访问"))).isVisible();
    }

    @Test
    void narrowStarterShellKeepsUtilityControlsReachable() {
        browserContext.close();
        browserContext = browser.newContext(new Browser.NewContextOptions().setViewportSize(new ViewportSize(390, 844)).setLocale("zh-CN"));
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
        signInAs("admin", "change-me");
        var header = page.locator(".admin-shell-header");
        var width = ((Number) page.evaluate("() => window.innerWidth")).doubleValue();
        org.assertj.core.api.Assertions.assertThat(((Number) header.evaluate("element => element.scrollWidth")).doubleValue())
                .isLessThanOrEqualTo(width + 1);
        assertThat(page.locator("vaadin-menu-bar.admin-language-menu vaadin-menu-bar-button:not([hidden])")).isVisible();
        assertThat(page.locator("vaadin-menu-bar.admin-appearance-menu vaadin-menu-bar-button:not([hidden])")).isVisible();
    }

    private void signInAs(String username, String password) {
        page.navigate(baseUrl() + "/login");
        var form = page.locator("vaadin-login-form");
        form.waitFor();
        var credentials = form.locator("input:not([type=hidden])");
        credentials.nth(0).fill(username);
        credentials.nth(1).fill(password);
        form.locator("vaadin-button[slot=submit]").click();
        page.waitForURL(baseUrl() + "/");
    }

    private void openShellMenu(String className) {
        PlaywrightBrowserSupport.clickThroughInjectedOverlay(
                page.locator("vaadin-menu-bar." + className + " vaadin-menu-bar-button:not([hidden])"));
    }

    private void createUser(String username, String password, UUID roleId) {
        var userId = UUID.randomUUID();
        jdbcTemplate.update("insert into users (id, username, password_hash, enabled, auth_version) values (?, ?, ?, true, 0)",
                userId, username, passwordEncoder.encode(password));
        if (roleId != null) jdbcTemplate.update("insert into user_roles (user_id, role_id) values (?, ?)", userId, roleId);
    }

    private void resetData() {
        jdbcTemplate.update("delete from audit_entries");
        jdbcTemplate.update("delete from user_roles where user_id <> (select id from users where username = 'admin')");
        jdbcTemplate.update("delete from role_permissions where role_id not in (select id from roles where code = 'administrator')");
        jdbcTemplate.update("delete from roles where code <> 'administrator'");
        jdbcTemplate.update("delete from users where username <> 'admin'");
    }

    private String baseUrl() { return "http://localhost:" + port; }
}
