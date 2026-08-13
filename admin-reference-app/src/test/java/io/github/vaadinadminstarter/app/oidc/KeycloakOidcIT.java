package io.github.vaadinadminstarter.app.oidc;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("development")
class KeycloakOidcIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static final KeycloakFixture keycloak = new KeycloakFixture();

    private static Playwright playwright;
    private static Browser browser;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private BrowserContext browserContext;
    private Page page;

    @DynamicPropertySource
    static void oidcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.security.oauth2.client.registration.oidc.client-id", () -> "vaadin-admin-test");
        registry.add("spring.security.oauth2.client.registration.oidc.client-secret", () -> "");
        registry.add("spring.security.oauth2.client.registration.oidc.client-authentication-method", () -> "none");
        registry.add("spring.security.oauth2.client.registration.oidc.authorization-grant-type", () -> "authorization_code");
        registry.add("spring.security.oauth2.client.registration.oidc.scope", () -> "openid,profile,email");
        registry.add("spring.security.oauth2.client.provider.oidc.issuer-uri", keycloak::issuerUri);
        registry.add("app.identity.oidc.links[0].issuer", keycloak::issuerUri);
        registry.add("app.identity.oidc.links[0].subject", () -> "10000000-0000-0000-0000-000000000001");
        registry.add("app.identity.oidc.links[0].username", () -> "admin");
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
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.setDefaultTimeout(15_000);
    }

    @AfterEach
    void closeBrowserContext() {
        browserContext.close();
    }

    @Test
    void returnsToTheRequestedRouteAsTheMappedLocalUser() {
        page.navigate(baseUrl() + "/users");

        startExternalLogin("oidc-admin");

        waitForUsersRoute();
        assertThat(page.getByLabel("当前用户菜单")).isVisible();
        assertThat(page.locator(".admin-user-menu-label")).hasText("admin");
    }

    @Test
    void deniesAnUnmappedSubjectWithoutRenderingTokensOrClaims() {
        page.navigate(baseUrl() + "/login");

        startExternalLogin("unmapped-user");

        page.waitForURL(baseUrl() + "/login?error=access-denied");
        assertThat(page.getByText("不允许登录", new Page.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.content())
                .doesNotContain("access_token", "id_token", "department");
    }

    @Test
    void localLogoutInvalidatesTheApplicationSession() {
        page.navigate(baseUrl() + "/users");
        startExternalLogin("oidc-admin");
        waitForUsersRoute();

        page.getByLabel("当前用户菜单").click();
        page.getByText("退出登录", new Page.GetByTextOptions().setExact(true)).click();

        page.waitForURL(baseUrl() + "/login");
        assertThat(page.locator("vaadin-login-form")).isVisible();
    }

    @Test
    void authenticationVersionChangeRejectsAPreviouslyMappedOidcSession() {
        page.navigate(baseUrl() + "/users");
        startExternalLogin("oidc-admin");
        waitForUsersRoute();
        jdbcTemplate.update("update users set auth_version = auth_version + 1 where username = 'admin'");

        page.navigate(baseUrl() + "/users");

        page.waitForURL(baseUrl() + "/login");
        assertThat(page.locator("vaadin-login-form")).isVisible();
    }

    private void startExternalLogin(String username) {
        var externalLogin = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("使用单点登录继续"));
        assertThat(externalLogin).hasAttribute("href", "/oauth2/authorization/oidc");
        externalLogin.click();
        page.waitForURL(url -> url.startsWith(keycloak.issuerUri() + "/protocol/openid-connect/auth"));
        page.locator("#username").fill(username);
        page.locator("#password").fill("change-me");
        page.locator("#kc-login").click();
    }

    private void waitForUsersRoute() {
        page.waitForURL(url -> "/users".equals(URI.create(url).getPath()));
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

}
