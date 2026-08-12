package io.github.vaadinadminstarter.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.github.vaadinadminstarter.flow.navigation.AdminIconName;
import org.junit.jupiter.api.Test;

class AdminThemeTokenTest {
    private static final List<String> PROFILE_TOKENS = List.of("control-height", "grid-cell-padding");
    private static final List<String> REQUIRED_TOKENS = List.of(
            "surface",
            "surface-raised",
            "text-primary",
            "text-secondary",
            "border",
            "accent",
            "success",
            "warning",
            "danger",
            "focus",
            "font-family",
            "space-sm",
            "space-md",
            "space-lg",
            "control-height",
            "grid-cell-padding",
            "utility-size",
            "radius-control",
            "radius-surface",
            "elevation-raised",
            "elevation-workspace",
            "control-fill",
            "control-border",
            "control-hover-border",
            "control-disabled-fill",
            "overlay-surface",
            "overlay-shadow",
            "workspace-header-fill",
            "workspace-header-text",
            "workspace-row-hover",
            "workspace-row-selected",
            "workspace-divider",
            "workspace-status-fill",
            "workspace-danger-fill");
    private static final List<String> WORKSPACE_TOKENS = List.of(
            "workspace-header-fill",
            "workspace-header-text",
            "workspace-row-hover",
            "workspace-row-selected",
            "workspace-divider",
            "workspace-status-fill",
            "workspace-danger-fill");

    @Test
    void declaresEverySemanticTokenForLightAndDarkThemes() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertTokenDeclarations(extractBlock(styles, ":root"), REQUIRED_TOKENS.stream()
                .filter(token -> !WORKSPACE_TOKENS.contains(token))
                .toList());
        assertTokenDeclarations(extractBlock(styles, "html\\[theme~=\"dark\"\\],\\s*\\[theme~=\"dark\"\\]"), REQUIRED_TOKENS.stream()
                .filter(token -> !WORKSPACE_TOKENS.contains(token))
                .toList());
        assertTokenDeclarations(extractBlock(styles, "\\[data-admin-visual-language=\"ant\"\\]"), WORKSPACE_TOKENS);
        assertTokenDeclarations(
                extractBlock(styles, "\\[data-admin-visual-language=\"ant\"\\]\\[theme~=\"dark\"\\]"), WORKSPACE_TOKENS);
    }

    @Test
    void derivesLumoPrimaryStateColorsFromTheCanonicalAccent() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertAccentStateColorMappings(extractBlock(styles, ":root"));
        assertAccentStateColorMappings(extractBlock(styles, "html\\[theme~=\"dark\"\\],\\s*\\[theme~=\"dark\"\\]"));
    }

    @Test
    void mapsStateTokensToLumoAndVaadinComponentVariables() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertThemeContractMappings(extractBlock(styles, ":root"));
        assertThemeContractMappings(extractBlock(styles, "html\\[theme~=\"dark\"\\],\\s*\\[theme~=\"dark\"\\]"));
    }

    @Test
    void declaresProfileAndDensityLayersWithoutReplacingSemanticMappings() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertTokenDeclarations(extractBlock(styles, ":root"), PROFILE_TOKENS);
        assertTokenDeclarations(extractBlock(styles, "html\\[theme~=\"dark\"\\],\\s*\\[theme~=\"dark\"\\]"), PROFILE_TOKENS);
        assertThat(styles).contains("[data-admin-visual-language=\"ant\"]");
        assertThat(styles).contains("[data-admin-visual-language=\"ant\"][theme~=\"dark\"]");
        assertThat(styles).contains("[data-admin-density=\"compact\"]");
        assertThat(styles).contains("--admin-accent: #1677ff;");
        assertThat(styles).contains("--lumo-size-m: var(--admin-control-height);");
        assertThat(styles).contains("[data-admin-visual-language],\n[data-admin-density] {");
        assertThat(styles).contains("--vaadin-focus-ring-color: var(--admin-focus);");
    }

    @Test
    void documentsEveryRequiredSemanticToken() throws IOException {
        var documentation = Files.readString(Path.of("../docs/en/theme-tokens.md"), StandardCharsets.UTF_8);

        REQUIRED_TOKENS.forEach(token -> assertThat(documentation)
                .as("documented token %s", token)
                .contains("`--admin-" + token + "`"));
    }

    @Test
    void documentsHostAppearanceProfiles() throws IOException {
        var documentation = Files.readString(Path.of("../docs/en/appearance-profiles.md"), StandardCharsets.UTF_8);

        assertThat(documentation).contains(
                "app.appearance.visual-language",
                "app.appearance.density",
                "vaadin",
                "ant",
                "comfortable",
                "compact");
    }

    @Test
    void keepsLoadingEmptyAndFailureSurfacesWithinTheirDataWorkspace() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertThat(styles).contains("""
                .admin-page-workspace > [role="status"] {
                  box-sizing: border-box;
                  color: var(--admin-text-secondary);
                  width: 100%;
                }
                """);
    }

    @Test
    void providesLicensedAntMaskAssetsForEverySemanticIcon() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);
        var iconDirectory = Path.of("src/main/frontend/themes/admin-theme/icons");

        assertThat(styles).contains(
                "[data-admin-visual-language=\"ant\"] .admin-icon",
                "mask-image: var(--admin-icon-mask);",
                "[data-admin-icon=\"shopping-cart\"]",
                ".admin-shell-header",
                "vaadin-side-nav-item[current]::part(link)");
        assertThat(Files.readString(iconDirectory.resolve("LICENSE"), StandardCharsets.UTF_8))
                .contains("ISC License");
        for (var icon : AdminIconName.values()) {
            var asset = Files.readString(iconDirectory.resolve(icon.cssValue() + ".svg"), StandardCharsets.UTF_8);
            assertThat(asset).contains("<svg", "viewBox=");
        }
    }

    @Test
    void scopesAntControlAndOverlaySkinningToPublicVaadinParts() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertThat(styles).contains(
                "--admin-control-fill:",
                "--admin-control-border:",
                "--admin-control-hover-border:",
                "--admin-control-disabled-fill:",
                "--admin-overlay-surface:",
                "--admin-overlay-shadow:",
                "[data-admin-visual-language=\"ant\"] vaadin-button[theme~=\"primary\"]",
                "[data-admin-visual-language=\"ant\"] vaadin-text-field[invalid]::part(input-field)",
                "[data-admin-visual-language=\"ant\"] vaadin-dialog-overlay::part(overlay)",
                "[data-admin-visual-language=\"ant\"] vaadin-menu-bar-overlay::part(overlay)",
                "[data-admin-visual-language=\"ant\"] vaadin-notification-card::part(overlay)");
    }

    @Test
    void scopesAntWorkspaceSkinningToSemanticHooksAndPublicVaadinParts() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertThat(styles).contains(
                "--admin-workspace-header-fill:",
                "--admin-workspace-header-text:",
                "--admin-workspace-row-hover:",
                "--admin-workspace-row-selected:",
                "--admin-workspace-divider:",
                "--admin-workspace-status-fill:",
                "--admin-workspace-danger-fill:",
                "[data-admin-visual-language=\"ant\"] vaadin-grid::part(header-cell)",
                "[data-admin-visual-language=\"ant\"] vaadin-grid::part(body-cell)",
                "[data-admin-visual-language=\"ant\"] .admin-page-workspace[data-admin-workspace-state]",
                "[data-admin-visual-language=\"ant\"] .admin-pagination-bar",
                "[data-admin-visual-language=\"ant\"] .admin-confirmation-consequence");
    }

    private void assertThemeContractMappings(String selectorBlock) {
        assertThat(selectorBlock).contains("--lumo-primary-color: var(--admin-accent);");
        assertAccentStateColorMappings(selectorBlock);
        assertThat(selectorBlock).contains("--lumo-success-color: var(--admin-success);");
        assertThat(selectorBlock).contains("--lumo-warning-color: var(--admin-warning);");
        assertThat(selectorBlock).contains("--lumo-error-color: var(--admin-danger);");
        assertThat(selectorBlock).contains("--lumo-body-text-color: var(--admin-text-primary);");
        assertThat(selectorBlock).contains("--lumo-secondary-text-color: var(--admin-text-secondary);");
        assertThat(selectorBlock).contains("--lumo-tertiary-text-color: var(--admin-text-muted);");
        assertThat(selectorBlock).contains("--lumo-base-color: var(--admin-surface);");
        assertThat(selectorBlock).contains("--lumo-contrast-10pct: var(--admin-border);");
        assertThat(selectorBlock).contains("--lumo-border-radius-m: var(--admin-radius-control);");
        assertThat(selectorBlock).contains("--lumo-font-family: var(--admin-font-family);");
        assertThat(selectorBlock).contains("--vaadin-focus-ring-color: var(--admin-focus);");
    }

    private void assertAccentStateColorMappings(String selectorBlock) {
        assertThat(selectorBlock).contains("--lumo-primary-color-50pct: color-mix(in srgb, var(--admin-accent) 50%, transparent);");
        assertThat(selectorBlock).contains("--lumo-primary-color-10pct: color-mix(in srgb, var(--admin-accent) 10%, transparent);");
    }

    private void assertTokenDeclarations(String selectorBlock) {
        assertTokenDeclarations(selectorBlock, REQUIRED_TOKENS);
    }

    private void assertTokenDeclarations(String selectorBlock, List<String> tokens) {
        tokens.forEach(token -> assertThat(selectorBlock)
                .as("token %s", token)
                .contains("--admin-" + token + ":"));
    }

    private String extractBlock(String styles, String selector) {
        var pattern = Pattern.compile("(?s)" + selector + "\\s*\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(styles);

        assertThat(matcher.find()).as("selector %s", selector).isTrue();
        return matcher.group(1);
    }
}
