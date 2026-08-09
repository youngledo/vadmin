package io.github.vaadinadminstarter.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class AdminThemeTokenTest {
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
            "radius-control",
            "elevation-raised");

    @Test
    void declaresEverySemanticTokenForLightAndDarkThemes() throws IOException {
        var styles = Files.readString(Path.of("src/main/frontend/themes/admin-theme/styles.css"), StandardCharsets.UTF_8);

        assertTokenDeclarations(extractBlock(styles, ":root"));
        assertTokenDeclarations(extractBlock(styles, "html\\[theme~=\"dark\"\\],\\s*\\[theme~=\"dark\"\\]"));
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
        var root = extractBlock(styles, ":root");

        assertThat(root).contains("--lumo-success-color: var(--admin-success);");
        assertThat(root).contains("--lumo-warning-color: var(--admin-warning);");
        assertThat(root).contains("--lumo-error-color: var(--admin-danger);");
        assertThat(root).contains("--vaadin-focus-ring-color: var(--admin-focus);");
    }

    private void assertAccentStateColorMappings(String selectorBlock) {
        assertThat(selectorBlock).contains("--lumo-primary-color-50pct: color-mix(in srgb, var(--admin-accent) 50%, transparent);");
        assertThat(selectorBlock).contains("--lumo-primary-color-10pct: color-mix(in srgb, var(--admin-accent) 10%, transparent);");
    }

    private void assertTokenDeclarations(String selectorBlock) {
        REQUIRED_TOKENS.forEach(token -> assertThat(selectorBlock)
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
