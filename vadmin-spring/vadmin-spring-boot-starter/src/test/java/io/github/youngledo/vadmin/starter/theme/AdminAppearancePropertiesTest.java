package io.github.youngledo.vadmin.starter.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminAppearancePropertiesTest {
    @Test
    void resolvesKnownAppearanceValuesCaseInsensitively() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("AnT");
        properties.setAuraBaseSize(16);

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.ANT);
        assertThat(properties.auraBaseSize()).contains(16);
    }

    @Test
    void fallsBackToSafeValuesForUnknownAppearanceValues() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("untrusted-selector");
        properties.setAuraBaseSize(11);

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.VAADIN);
        assertThat(properties.auraBaseSize()).isEmpty();
    }
}
