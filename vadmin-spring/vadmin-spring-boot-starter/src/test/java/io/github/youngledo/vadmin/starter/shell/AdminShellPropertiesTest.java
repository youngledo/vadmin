package io.github.youngledo.vadmin.starter.shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminShellPropertiesTest {
    @Test
    void enablesTheWorkplaceByDefault() {
        assertThat(new AdminShellProperties().workplaceEnabled()).isTrue();
    }

    @Test
    void acceptsDisablingTheWorkplaceNavigationEntry() {
        var properties = new AdminShellProperties();

        properties.setWorkplaceEnabled(false);

        assertThat(properties.workplaceEnabled()).isFalse();
    }
}
