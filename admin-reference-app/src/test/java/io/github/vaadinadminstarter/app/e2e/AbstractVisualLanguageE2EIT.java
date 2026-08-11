package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ViewportSize;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
abstract class AbstractVisualLanguageE2EIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    int port;

    BrowserContext browserContext;
    Page page;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("app.file-storage.directory", () -> "target/appearance-e2e-files");
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
        browserContext = browser.newContext(new Browser.NewContextOptions().setLocale("zh-CN"));
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
    }

    @AfterEach
    void closeContext() {
        browserContext.close();
    }

    void signInAsAdministrator() {
        page.navigate(baseUrl() + "/login");
        var loginForm = page.locator("vaadin-login-form");
        loginForm.waitFor();
        var credentials = loginForm.locator("input:not([type=hidden])");
        credentials.nth(0).fill("admin");
        credentials.nth(1).fill("change-me");
        loginForm.locator("vaadin-button[slot=submit]").click();
        page.waitForURL(baseUrl() + "/");
    }

    void openAppearanceMenu() {
        page.locator("vaadin-menu-bar.admin-appearance-menu vaadin-menu-bar-button:not([hidden])").click();
    }

    void useNarrowBrowser() {
        browserContext.close();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(390, 844))
                .setIsMobile(true)
                .setLocale("zh-CN"));
        page = browserContext.newPage();
        page.setDefaultTimeout(10_000);
    }

    void assertShellDoesNotOverflow(Locator... controls) {
        var header = page.locator(".admin-shell-header");
        var viewportWidth = ((Number) page.evaluate("() => window.innerWidth")).doubleValue();
        var headerScrollWidth = ((Number) header.evaluate("element => element.scrollWidth")).doubleValue();
        org.assertj.core.api.Assertions.assertThat(headerScrollWidth).isLessThanOrEqualTo(viewportWidth + 1);
        for (var control : controls) {
            var box = control.boundingBox();
            org.assertj.core.api.Assertions.assertThat(box).isNotNull();
            org.assertj.core.api.Assertions.assertThat(box.x).isGreaterThanOrEqualTo(-1);
            org.assertj.core.api.Assertions.assertThat(box.x + box.width).isLessThanOrEqualTo(viewportWidth + 1);
        }
    }

    String computedThemeVariable(String name) {
        return (String) page.locator("body")
                .evaluate("(element, name) => getComputedStyle(element).getPropertyValue(name).trim()", name);
    }

    String baseUrl() {
        return "http://localhost:" + port;
    }
}
