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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

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

    void useNarrowDesktopBrowser() {
        browserContext.close();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(390, 844))
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

    String computedPartStyle(Locator component, String part, String property) {
        return (String) component.evaluate("""
                element => getComputedStyle(element.shadowRoot.querySelector('[part="%s"]'))
                        .getPropertyValue('%s')
                """.formatted(part, property));
    }

    String computedPublicPartStyle(Locator component, String partToken, String property) {
        return (String) component.evaluate("""
                (element, options) => {
                  const part = element.shadowRoot.querySelector('[part~="' + options.partToken + '"]');
                  if (!part) throw new Error('Missing exported part: ' + options.partToken);
                  return getComputedStyle(part).getPropertyValue(options.property);
                }
                """, Map.of("partToken", partToken, "property", property));
    }

    void assertWithinViewport(Locator control) {
        var box = control.boundingBox();
        var viewportWidth = ((Number) page.evaluate("() => window.innerWidth")).doubleValue();
        var viewportHeight = ((Number) page.evaluate("() => window.innerHeight")).doubleValue();
        org.assertj.core.api.Assertions.assertThat(box).isNotNull();
        org.assertj.core.api.Assertions.assertThat(box.x).isGreaterThanOrEqualTo(-1);
        org.assertj.core.api.Assertions.assertThat(box.y).isGreaterThanOrEqualTo(-1);
        org.assertj.core.api.Assertions.assertThat(box.x + box.width).isLessThanOrEqualTo(viewportWidth + 1);
        org.assertj.core.api.Assertions.assertThat(box.y + box.height).isLessThanOrEqualTo(viewportHeight + 1);
    }

    String computedOpenedDialogOverlayPartStyle(String partToken, String property) {
        return computedPublicPartStyle(page.locator("vaadin-dialog-overlay[opened]"), partToken, property);
    }

    double[] remValues(String value) {
        return java.util.Arrays.stream(value.split("\\s+"))
                .mapToDouble(part -> Double.parseDouble(part.replace("rem", "")))
                .toArray();
    }

    String baseUrl() {
        return "http://localhost:" + port;
    }
}
