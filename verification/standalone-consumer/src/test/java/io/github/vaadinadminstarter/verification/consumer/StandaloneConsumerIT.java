package io.github.vaadinadminstarter.verification.consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StandaloneConsumerIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    private Page page;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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

    @AfterEach
    void closePage() {
        if (page != null) {
            page.context().close();
        }
    }

    @Test
    void consumesInstalledOrdersModuleAfterLocalAuthentication() {
        page = browser.newPage();
        page.setDefaultTimeout(10_000);
        page.navigate(baseUrl() + "/login");

        var login = page.locator("vaadin-login-form");
        login.locator("input:not([type=hidden])").nth(0).fill("admin");
        login.locator("input:not([type=hidden])").nth(1).fill("change-me");
        login.locator("vaadin-button[slot=submit]").click();
        page.waitForURL(baseUrl() + "/");

        page.navigate(baseUrl() + "/orders");
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("订单"))).isVisible();
        assertThat(page.locator("[data-testid=orders-workspace] vaadin-grid")).isVisible();
        assertThat(page.getByText("ORD-1001", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
