package io.github.youngledo.vadmin.starter.views;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import io.github.youngledo.vadmin.springflow.i18n.AdminLocalePreference;
import io.github.youngledo.vadmin.springflow.i18n.CompositeAdminI18NProvider;
import io.github.youngledo.vadmin.starter.brand.AdminBrandProperties;
import io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultMainLayoutSerializationTest {
    @Test
    void serializesWithoutRetainingSpringWebAuthenticationState() throws Exception {
        var layout = new DefaultMainLayout(
                new AdminModuleRegistry(List.of()),
                (CurrentUserProvider) Optional::empty,
                new PermitNothingAuthorizationService(),
                new AdminLocalePreference(),
                new CompositeAdminI18NProvider(List.of()),
                new AdminAppearanceProperties(),
                new AdminBrandProperties());

        var bytes = new ByteArrayOutputStream();
        try (var output = new ObjectOutputStream(bytes)) {
            output.writeObject(layout);
        }

        assertThat(bytes.size()).isPositive();
    }

    private static final class PermitNothingAuthorizationService implements AuthorizationService {
        @Override
        public boolean hasPermission(CurrentUser user, PermissionCode permission) {
            return false;
        }

        @Override
        public void requirePermission(CurrentUser user, PermissionCode permission) {
            throw new UnsupportedOperationException();
        }
    }
}
