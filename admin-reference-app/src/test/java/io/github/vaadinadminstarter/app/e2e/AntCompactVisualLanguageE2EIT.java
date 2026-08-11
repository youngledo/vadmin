package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.appearance.visual-language=ant", "app.appearance.density=compact"})
@Testcontainers
@ActiveProfiles("development")
class AntCompactVisualLanguageE2EIT extends AbstractVisualLanguageE2EIT {
    @Test
    void compactAntProfileUsesCompactTokensWithoutClippingTheNarrowShell() {
        useNarrowBrowser();
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        assertThat(page.locator("body")).hasAttribute("data-admin-visual-language", "ant");
        assertThat(page.locator("body")).hasAttribute("data-admin-density", "compact");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-control-height"))
                .isEqualTo("2rem");
        org.assertj.core.api.Assertions.assertThat(remValues(computedThemeVariable("--admin-grid-cell-padding")))
                .containsExactly(0.375, 0.75);

        var navigation = page.getByLabel("切换导航");
        var language = page.locator("vaadin-menu-bar.admin-language-menu vaadin-menu-bar-button:not([hidden])");
        var appearance = page.locator("vaadin-menu-bar.admin-appearance-menu vaadin-menu-bar-button:not([hidden])");
        var account = page.getByLabel("当前用户菜单");
        assertShellDoesNotOverflow(navigation, language, appearance, account);
        navigation.click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("客户")).click();
        assertThat(page.getByTestId("customers-workspace")).isVisible();
    }

    @Test
    void compactAntProfileKeepsDensityWhenDarkModeShowsAnEditorDialog() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");

        openAppearanceMenu();
        page.getByText("深色模式", new Page.GetByTextOptions().setExact(true)).click();
        assertThat(page.locator("body")).hasAttribute("data-admin-density", "compact");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();
        assertThat(page.getByLabel("名称")).isVisible();
        assertThat(page.getByLabel("名称")).isEditable();
    }
}
