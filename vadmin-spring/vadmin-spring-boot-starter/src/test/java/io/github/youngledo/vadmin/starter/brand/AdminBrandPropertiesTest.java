package io.github.youngledo.vadmin.starter.brand;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminBrandPropertiesTest {
    @Test
    void usesTheConfiguredProductName() {
        var properties = new AdminBrandProperties();
        properties.setName("  RDC OpenAPI OPS  ");

        assertThat(properties.name()).isEqualTo("RDC OpenAPI OPS");
    }

    @Test
    void fallsBackToTheVadminProductNameWhenTheConfigurationIsBlank() {
        var properties = new AdminBrandProperties();
        properties.setName(" ");

        assertThat(properties.name()).isEqualTo("VAdmin");
    }
}
