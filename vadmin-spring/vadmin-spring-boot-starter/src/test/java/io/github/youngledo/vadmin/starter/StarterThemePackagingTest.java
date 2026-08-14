package io.github.youngledo.vadmin.starter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StarterThemePackagingTest {
    @Test
    void exposesTheDefaultThemeAsJarResourcesForStarterConsumers() {
        var classLoader = getClass().getClassLoader();

        assertThat(classLoader.getResource("META-INF/resources/themes/admin-theme/theme.json")).isNotNull();
        assertThat(classLoader.getResource("META-INF/resources/themes/admin-theme/styles.css")).isNotNull();
        assertThat(classLoader.getResource("META-INF/resources/themes/admin-theme/icons/users.svg")).isNotNull();
    }

    @Test
    void doesNotUseRemovedLumoThemeImports() throws Exception {
        var classLoader = getClass().getClassLoader();
        try (var theme = classLoader.getResourceAsStream("META-INF/resources/themes/admin-theme/theme.json")) {
            assertThat(theme).isNotNull();
            assertThat(new String(theme.readAllBytes(), StandardCharsets.UTF_8)).doesNotContain("lumoImports");
        }
    }
}
