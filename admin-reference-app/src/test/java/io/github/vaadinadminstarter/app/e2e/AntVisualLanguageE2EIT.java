package io.github.vaadinadminstarter.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
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
    void antProfileAppliesToTheStandaloneLoginRouteBeforeAuthentication() {
        page.navigate(baseUrl() + "/login");

        assertThat(page.locator("body")).hasAttribute("data-admin-visual-language", "ant");
        assertThat(page.locator("body")).hasAttribute("data-admin-density", "comfortable");
        assertThat(page.locator("vaadin-login-form")).isVisible();
    }

    @Test
    void antProfileRendersSemanticIconsWithHostOwnedMasks() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        var navigationIcon = page.locator("[data-admin-visual-language=ant] "
                + ".admin-icon[data-admin-icon=users]");
        assertThat(navigationIcon).isVisible();
        org.assertj.core.api.Assertions.assertThat((String) navigationIcon.evaluate(
                "element => getComputedStyle(element).maskImage"))
                .startsWith("url(")
                .isNotEqualTo("none");
        assertThat(navigationIcon.locator("vaadin-icon")).isHidden();
    }

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
    void antProfileStylesUserSelectionAndItsExplicitConfirmation() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        var workspace = page.getByTestId("users-workspace");
        var grid = workspace.locator("vaadin-grid");
        assertThat(workspace).hasAttribute("data-admin-workspace-state", "ready");
        assertThat(page.getByTestId("users-toolbar").getByLabel("搜索用户")).isVisible();
        assertThat(grid).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(grid, "header-cell", "background-color"))
                .isEqualTo("rgb(250, 250, 250)");
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(grid, "body-cell", "box-shadow"))
                .contains("rgb(240, 240, 240)");
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Path.of("target/failsafe-reports/ant-comfortable-users-workspace.png")));

        workspace.getByRole(AriaRole.CHECKBOX, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("Select Row")).click();
        assertThat(workspace.getByText("1 selected",
                new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByLabel("停用所选用户")).isEnabled();
        page.getByLabel("停用所选用户").click();

        var confirmation = page.locator("vaadin-dialog[opened]");
        assertThat(confirmation).hasAttribute("aria-label", "停用用户");
        var consequence = confirmation.locator(".admin-confirmation-consequence");
        assertThat(consequence).isVisible();
        org.assertj.core.api.Assertions.assertThat((String) consequence.evaluate(
                "element => getComputedStyle(element).backgroundColor"))
                .isEqualTo("rgb(255, 242, 240)");
        confirmation.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("取消")).click();
    }

    @Test
    void antProfileKeepsCustomerFeedbackAndConfirmationInsideTheWorkspaceFlow() {
        seedCustomers("Ant pager customer", 51);
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");

        var workspace = page.getByTestId("customers-workspace");
        var grid = workspace.locator("vaadin-grid");
        assertThat(page.getByLabel("搜索")).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户"))).isVisible();
        page.getByLabel("搜索").fill("Ant pager customer");
        var pager = workspace.locator(".admin-pagination-bar");
        assertThat(pager).isVisible();
        assertThat(pager).containsText("第 1 / 2 页，共 51 条");
        pager.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("下一页")).click();
        assertThat(pager).containsText("第 2 / 2 页，共 51 条");
        assertThat(pager.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("下一页"))).isDisabled();
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Path.of("target/failsafe-reports/ant-comfortable-customers-pager.png")));

        page.getByLabel("搜索").fill("");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();
        page.getByLabel("名称").fill("Ant workspace customer");
        page.getByLabel("邮箱").fill("workspace@ant-flow.test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByText("客户已创建。", new Page.GetByTextOptions().setExact(true))).isVisible();
        page.getByLabel("搜索").fill("Ant workspace customer");
        assertThat(page.getByText("Ant workspace customer", new Page.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(grid, "header-cell", "background-color"))
                .isEqualTo("rgb(250, 250, 250)");
        page.getByLabel("删除客户").first().click();
        var confirmation = page.locator("vaadin-dialog[opened]");
        assertThat(confirmation).hasAttribute("aria-label", "删除客户");
        org.assertj.core.api.Assertions.assertThat(computedOpenedDialogOverlayPartStyle("overlay", "background-color"))
                .isEqualTo("rgb(255, 255, 255)");
    }

    @Test
    void antProfileStylesTheExternalOrdersWorkspaceWithoutInventingPagination() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/orders");

        var workspace = page.getByTestId("orders-workspace");
        var grid = workspace.locator("vaadin-grid");
        assertThat(grid).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(grid, "header-cell", "background-color"))
                .isEqualTo("rgb(250, 250, 250)");
        assertThat(workspace.locator(".admin-pagination-bar")).isHidden();
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Path.of("target/failsafe-reports/ant-comfortable-orders-workspace.png")));
        page.getByLabel("查看订单详情：ORD-1001").click();
        var details = page.locator("vaadin-dialog[opened]");
        assertThat(details).hasAttribute("aria-label", "订单详情");
        org.assertj.core.api.Assertions.assertThat(computedOpenedDialogOverlayPartStyle("overlay", "background-color"))
                .isEqualTo("rgb(255, 255, 255)");
    }

    @Test
    void antProfileUsesTheSharedGridContractForRolesAndAudit() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/roles");

        var roleGrid = page.getByTestId("roles-workspace").locator("vaadin-grid");
        assertThat(roleGrid).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(roleGrid, "header-cell", "border-bottom-color"))
                .isEqualTo("rgb(240, 240, 240)");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("授予权限")).click();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存授权"))).isDisabled();

        page.keyboard().press("Escape");
        page.navigate(baseUrl() + "/audit");
        var auditGrid = page.getByTestId("read-only-workspace").locator("vaadin-grid");
        assertThat(auditGrid).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPublicPartStyle(auditGrid, "body-cell", "box-shadow"))
                .contains("rgb(240, 240, 240)");
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
    void antProfileProvidesAVisibleFocusRingForShellUtilityControls() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        var languageControl = page.locator(
                "vaadin-menu-bar.admin-language-menu vaadin-menu-bar-button:not([hidden])");
        assertThat(languageControl).isVisible();
        languageControl.focus();
        org.assertj.core.api.Assertions.assertThat((Boolean) languageControl.evaluate(
                "element => document.activeElement === element && element.matches(':focus-visible')"))
                .isTrue();
        org.assertj.core.api.Assertions.assertThat((String) languageControl.evaluate(
                "element => getComputedStyle(element).outlineColor"))
                .isNotEqualTo("rgba(0, 0, 0, 0)");
        org.assertj.core.api.Assertions.assertThat((String) languageControl.evaluate(
                "element => getComputedStyle(element).outlineStyle"))
                .isEqualTo("solid");
        org.assertj.core.api.Assertions.assertThat((String) languageControl.evaluate(
                "element => getComputedStyle(element).outlineWidth"))
                .isEqualTo("2px");
        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--vaadin-focus-ring-color"))
                .isEqualTo("#1677ff");
        assertThat(page.getByRole(AriaRole.MENUBAR,
                new Page.GetByRoleOptions().setName("语言选项"))).isVisible();
    }

    @Test
    void antComfortableProfileUsesItsRuntimeDensityValues() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");

        org.assertj.core.api.Assertions.assertThat(computedThemeVariable("--admin-control-height"))
                .isEqualTo("2.25rem");
        org.assertj.core.api.Assertions.assertThat(remValues(computedThemeVariable("--admin-grid-cell-padding")))
                .containsExactly(0.5, 1.0);
    }

    @Test
    void antProfileRendersNativeInvalidFieldsInTheCustomerEditor() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();
        var editor = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("新增客户"));
        var name = editor.locator("vaadin-text-field").nth(0);
        var email = editor.locator("vaadin-text-field").nth(1);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存")).click();

        assertThat(page.getByRole(AriaRole.ALERT)).isVisible();
        assertThat(name).hasAttribute("invalid", "");
        assertThat(email).hasAttribute("invalid", "");

        page.getByLabel("名称").fill("Ant validation customer");
        page.getByLabel("邮箱").fill("validation@ant-flow.test");

        assertThat(name).not().hasAttribute("invalid", "");
        assertThat(email).not().hasAttribute("invalid", "");
    }

    @Test
    void antProfileRendersARealDisabledCommand() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/roles");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("授予权限")).click();

        var dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("授予权限"));
        var save = dialog.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Locator.GetByRoleOptions()
                .setName("保存授权"));

        assertThat(save).isDisabled();
        assertThat(save).hasAttribute("disabled", "");
        org.assertj.core.api.Assertions.assertThat((String) save.evaluate(
                "element => getComputedStyle(element).backgroundColor"))
                .isEqualTo("rgb(245, 245, 245)");
        org.assertj.core.api.Assertions.assertThat((String) save.evaluate(
                "element => getComputedStyle(element).color"))
                .isEqualTo("rgba(0, 0, 0, 0.25)");
    }

    @Test
    void antProfileRendersControlsAndOverlaysFromTheSemanticTokenContract() {
        signInAsAdministrator();
        page.navigate(baseUrl() + "/customers");

        var language = page.locator("vaadin-menu-bar.admin-language-menu vaadin-menu-bar-button:not([hidden])");
        language.click();
        var menuOverlay = page.locator("vaadin-menu-bar-overlay[opened]");
        assertThat(menuOverlay).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(menuOverlay, "overlay", "background-color"))
                .isEqualTo("rgb(255, 255, 255)");
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(menuOverlay, "overlay", "box-shadow"))
                .isNotEqualTo("none");
        page.keyboard().press("Escape");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增客户")).click();
        var editor = page.locator("vaadin-dialog[opened]");
        var name = editor.locator("vaadin-text-field").nth(0);
        var email = editor.locator("vaadin-text-field").nth(1);
        var save = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("保存"));

        org.assertj.core.api.Assertions.assertThat(computedOpenedDialogOverlayPartStyle("overlay", "border-radius"))
                .isEqualTo("8px");
        org.assertj.core.api.Assertions.assertThat((String) save.evaluate("element => getComputedStyle(element).backgroundColor"))
                .isEqualTo("rgb(22, 119, 255)");
        org.assertj.core.api.Assertions.assertThat((String) save.evaluate("element => getComputedStyle(element).height"))
                .isEqualTo("36px");

        name.focus();
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(name, "input-field", "border-radius"))
                .isEqualTo("6px");

        save.click();
        assertThat(name).hasAttribute("invalid", "");
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(name, "input-field", "border-color"))
                .isEqualTo("rgb(255, 77, 79)");

        page.getByLabel("名称").fill("Ant overlay customer");
        page.getByLabel("邮箱").fill("overlay@ant-flow.test");
        save.click();
        var notification = page.locator("vaadin-notification-card");
        assertThat(notification).isVisible();
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(notification, "overlay", "background-color"))
                .isEqualTo("rgb(255, 255, 255)");
        org.assertj.core.api.Assertions.assertThat(computedPartStyle(notification, "overlay", "box-shadow"))
                .isNotEqualTo("none");
    }

    private void seedCustomers(String namePrefix, int count) {
        var now = Instant.now();
        for (var index = 0; index < count; index++) {
            var suffix = String.format(" %03d", index);
            jdbcTemplate.update("""
                    insert into customers (id, name, email, active, created_at, updated_at)
                    values (?, ?, ?, true, ?, ?)
                    """, UUID.randomUUID(), namePrefix + suffix,
                    "ant-pager-" + index + "@example.test", Timestamp.from(now), Timestamp.from(now));
        }
    }
}
