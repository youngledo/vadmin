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
        assertThat(AdminIconCatalog.iconName("shopping-cart")).isEqualTo(AdminIconName.SHOPPING_CART);
        assertThat(AdminIconCatalog.iconName("users")).isEqualTo(AdminIconName.USERS);
        assertThatIllegalArgumentException().isThrownBy(() -> AdminIconCatalog.iconName("untrusted"));
    }
}
