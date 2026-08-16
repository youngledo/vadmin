package io.github.youngledo.vadmin.starter.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminAppearancePropertiesTest {
    @Test
    void resolvesKnownAppearanceValuesCaseInsensitively() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("AnT");

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.ANT);
    }

    @Test
    void fallsBackToSafeValuesForUnknownAppearanceValues() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("untrusted-selector");

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.VAADIN);
    }
}
