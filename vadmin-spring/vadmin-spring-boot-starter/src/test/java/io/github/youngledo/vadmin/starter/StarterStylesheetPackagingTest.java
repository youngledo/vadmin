package io.github.youngledo.vadmin.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StarterStylesheetPackagingTest {
    @Test
    void exposesOnlyAntVisualResourcesForStarterConsumers() {
        var classLoader = getClass().getClassLoader();

        assertThat(classLoader.getResource("META-INF/resources/vadmin/ant.css")).isNotNull();
        assertThat(classLoader.getResource("META-INF/resources/vadmin/icons/users.svg")).isNotNull();
    }
}
