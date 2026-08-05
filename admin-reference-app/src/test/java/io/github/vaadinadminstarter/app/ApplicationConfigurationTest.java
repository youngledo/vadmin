package io.github.vaadinadminstarter.app;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

class ApplicationConfigurationTest {
    @Test
    void requiresAnExplicitBootstrapPasswordOutsideTheDevelopmentProfile() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("staging");

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(new ApplicationConfiguration(),
                "bootstrapPassword", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_PASSWORD");
    }
}
