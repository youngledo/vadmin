package io.github.youngledo.vadmin.app.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.appearance.visual-language=ant")
@Testcontainers
@ActiveProfiles("development")
class AntVisualLanguageE2EIT extends AbstractVisualLanguageE2EIT {
    @Test
    void antProfileStylesTheStarterLoginShellAndSystemWorkspace() {
        page.navigate(baseUrl() + "/login");
        assertThat(page.locator("html")).hasAttribute("data-vadmin-visual-language", "ant");
        assertThat(page.locator("#vaadinLoginUsername")).isVisible();
        signInAsAdministrator();
        page.navigate(baseUrl() + "/users");
        var icon = page.locator("[data-vadmin-visual-language=ant] .admin-icon[data-admin-icon=users]");
        assertThat(icon).isVisible();
        org.assertj.core.api.Assertions.assertThat((String) icon.evaluate("element => getComputedStyle(element).maskImage"))
                .startsWith("url(");
        assertThat(page.getByTestId("users-workspace").locator("vaadin-grid")).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions().setName("新增用户"))).isVisible();
    }
}
