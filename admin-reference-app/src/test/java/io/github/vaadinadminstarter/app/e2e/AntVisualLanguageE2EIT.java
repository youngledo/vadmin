package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.appearance.visual-language=ant", "app.appearance.density=comfortable"})
@Testcontainers
@ActiveProfiles("development")
class AntVisualLanguageE2EIT extends AbstractVisualLanguageE2EIT {
    @Test
    void antProfileKeepsSharedPagePatternsAcrossRepresentativePages() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        assertThat(page.locator("body")).hasAttribute("data-admin-visual-language", "ant");
        assertThat(page.locator("body")).hasAttribute("data-admin-density", "comfortable");
        assertThat(page.locator(".admin-page-frame")).hasCount(1);
        assertThat(page.getByTestId("users-workspace").locator("vaadin-grid")).isVisible();

        page.navigate(baseUrl() + "/orders");
        assertThat(page.getByTestId("orders-workspace").locator("vaadin-grid")).isVisible();
        page.navigate(baseUrl() + "/roles");
        assertThat(page.locator("[data-testid=roles-workspace] vaadin-grid")).isVisible();
    }

    @Test
    void antProfileKeepsTheAuditWorkspaceAvailable() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/audit");

        assertThat(page.locator("body")).hasAttribute("data-admin-visual-language", "ant");
        assertThat(page.locator(".admin-page-frame")).hasCount(1);
        assertThat(page.locator("[data-testid=read-only-workspace] vaadin-grid")).isVisible();
    }

    @Test
    void antProfileRetainsItsIdentityWhenDarkModeChanges() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");

        openAppearanceMenu();
        page.getByText("深色模式", new Page.GetByTextOptions().setExact(true)).click();

        assertThat(page.locator("body")).hasAttribute("theme", "dark");
        assertThat(page.locator("body")).hasAttribute("data-admin-visual-language", "ant");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-canvas")).isEqualTo("#141414");
        assertThat(page.getByTestId("customers-workspace")).isVisible();
    }

    @Test
    void antProfileProvidesTheFocusTokenForShellUtilityControls() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        var languageControl = page.locator(
                "vaadin-menu-bar.admin-language-menu vaadin-menu-bar-button:not([hidden])");
        assertThat(languageControl).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--vaadin-focus-ring-color"))
                .isEqualTo("#1677ff");
        assertThat(page.getByRole(AriaRole.MENUBAR,
                new Page.GetByRoleOptions().setName("语言选项"))).isVisible();
    }
}
