package io.github.youngledo.vadmin.flow.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class AdminIconTest {
    @Test
    void rendersAStableSemanticWrapperAroundTheVaadinFallback() {
        var icon = AdminIcon.of(AdminIconName.EYE);

        assertThat(icon.getClassNames()).contains("admin-icon");
        assertThat(icon.getElement().getAttribute("data-admin-icon")).isEqualTo("eye");
        assertThat(icon.getElement().getAttribute("aria-hidden")).isEqualTo("true");
        assertThat(icon.getElement().getChildren().count()).isEqualTo(1);
    }

    @Test
    void resolvesEverySupportedNavigationKeyToAStableIconName() {
        for (var iconName : AdminIconName.values()) {
            assertThat(AdminIconCatalog.iconName(iconName.cssValue())).isEqualTo(iconName);
        }
        assertThatIllegalArgumentException().isThrownBy(() -> AdminIconCatalog.iconName("untrusted"));
    }
}
