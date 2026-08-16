package io.github.youngledo.vadmin.starter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StarterStylesheetPackagingTest {
    @Test
    void exposesOnlyAntVisualResourcesForStarterConsumers() {
        var classLoader = getClass().getClassLoader();

        assertThat(classLoader.getResource("META-INF/resources/vadmin/ant.css")).isNotNull();
        assertThat(classLoader.getResource("META-INF/resources/vadmin/icons/users.svg")).isNotNull();
    }

    @Test
    void keepsTheSharedDrawerWidthOutsideTheAntStylesheet() throws Exception {
        var classLoader = getClass().getClassLoader();
        try (var stylesheet = classLoader.getResourceAsStream("META-INF/resources/vadmin/ant.css")) {
            assertThat(stylesheet).isNotNull();
            var css = new String(stylesheet.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(css).doesNotContain("width: 13rem");
        }
    }
}
