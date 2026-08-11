package io.github.vaadinadminstarter.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminAppearancePropertiesTest {
    @Test
    void resolvesKnownAppearanceValuesCaseInsensitively() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("AnT");
        properties.setDensity("COMPACT");

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.ANT);
        assertThat(properties.density()).isEqualTo(AdminDensity.COMPACT);
    }

    @Test
    void fallsBackToSafeValuesForUnknownAppearanceValues() {
        var properties = new AdminAppearanceProperties();
        properties.setVisualLanguage("untrusted-selector");
        properties.setDensity(" ");

        assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.VAADIN);
        assertThat(properties.density()).isEqualTo(AdminDensity.COMFORTABLE);
    }
}
