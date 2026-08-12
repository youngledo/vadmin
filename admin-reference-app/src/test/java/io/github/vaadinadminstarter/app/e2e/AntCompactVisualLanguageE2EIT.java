package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Path;
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
        org.assertj.core.api.Assertions.assertThat((String) page.locator(".admin-icon[data-admin-icon=globe]")
                .evaluate("element => getComputedStyle(element).maskImage"))
                .startsWith("url(")
                .isNotEqualTo("none");
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

    @Test
    void compactDarkAntProfileKeepsEditorCommandsInsideTheNarrowViewport() {
        useNarrowDesktopBrowser();
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");
        openAppearanceMenu();
        page.getByText("深色模式", new Page.GetByTextOptions().setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();

        var save = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存"));
        var viewportWidth = ((Number) page.evaluate("() => window.innerWidth")).doubleValue();
        var saveBox = save.boundingBox();
        var controlHeight = (String) save.evaluate("element => getComputedStyle(element).height");

        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-overlay-surface"))
                .isEqualTo("#1f1f1f");
        org.assertj.core.api.Assertions.assertThat(computedOpenedDialogOverlayPartStyle("overlay", "border-radius"))
                .isEqualTo("8px");
        org.assertj.core.api.Assertions.assertThat(saveBox).isNotNull();
        org.assertj.core.api.Assertions.assertThat(saveBox.x + saveBox.width).isLessThanOrEqualTo(viewportWidth + 1);
        org.assertj.core.api.Assertions.assertThat(saveBox.height).isGreaterThan(0d);
        org.assertj.core.api.Assertions.assertThat(controlHeight).isEqualTo("32px");
    }

    @Test
    void compactDarkAntProfileKeepsTheUsersWorkspaceAndConfirmationActionsContained() {
        useNarrowDesktopBrowser();
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");
        openAppearanceMenu();
        page.getByText("深色模式", new Page.GetByTextOptions().setExact(true)).click();

        var toolbar = page.getByTestId("users-toolbar");
        var workspace = page.getByTestId("users-workspace");
        var grid = workspace.locator("vaadin-grid");
        assertThat(workspace).hasAttribute("data-admin-workspace-state", "ready");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-header-fill"))
                .isEqualTo("#262626");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-header-text"))
                .isIn("rgba(255, 255, 255, 0.65)", "#ffffffa6");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-row-hover"))
                .isEqualTo("#262626");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-row-selected"))
                .isEqualTo("#111d2c");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-divider"))
                .isEqualTo("#303030");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-status-fill"))
                .isEqualTo("#262626");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-workspace-danger-fill"))
                .isEqualTo("#2a1215");
        assertWithinViewport(toolbar);
        assertWithinViewport(workspace);
        assertWithinViewport(grid);

        var disableSelected = page.getByLabel("停用所选用户");
        assertThat(disableSelected).isDisabled();
        grid.getByRole(AriaRole.CHECKBOX, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("Select Row")).click();
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Path.of("target/failsafe-reports/ant-compact-dark-users-selection.png")));
        assertThat(disableSelected).isEnabled();
        assertWithinViewport(disableSelected);

        disableSelected.click();
        var confirmation = page.locator("vaadin-dialog[opened]");
        var cancel = confirmation.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("取消"));
        var confirm = confirmation.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("停用"));
        assertThat(confirmation).hasAttribute("aria-label", "停用用户");
        assertThat(cancel).isVisible();
        assertThat(confirm).isVisible();
        assertWithinViewport(cancel);
        assertWithinViewport(confirm);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Path.of("target/failsafe-reports/ant-compact-dark-users-confirmation.png")));
    }
}
