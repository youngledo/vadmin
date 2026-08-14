package io.github.youngledo.vadmin.starter;

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
}
