package io.github.youngledo.vadmin.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.appearance.visual-language=ant", "app.appearance.aura-base-size=16"})
@Testcontainers
@ActiveProfiles("development")
class AntAuraBaseSizeE2EIT extends AbstractVisualLanguageE2EIT {
    @Test
    void antLanguageUsesTheConfiguredAuraBaseSizeWithoutASeparateDensitySystem() {
        useNarrowDesktopBrowser();
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        assertThat(page.locator("html")).hasAttribute("data-vadmin-visual-language", "ant");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--aura-base-size")).isEqualTo("16");
        var workspace = page.getByTestId("users-workspace");
        assertWithinViewport(workspace);
        var disable = page.getByLabel("停用所选用户");
        workspace.locator("vaadin-grid").getByRole(AriaRole.CHECKBOX,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Select Row")).click();
        assertThat(disable).isEnabled();
        disable.click();
        var dialog = page.locator("vaadin-dialog[opened]");
        assertThat(dialog).hasAttribute("aria-label", "停用用户");
        assertWithinViewport(dialog.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("取消")));
        assertWithinViewport(dialog.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("停用")));
    }
}
