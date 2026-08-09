package io.github.vaadinadminstarter.flow.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.internal.NavigationRouteTarget;
import com.vaadin.flow.server.RouteRegistry;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

class PermissionProtectedViewTest {
    private static final PermissionCode REQUIRED = PermissionCode.of("orders:order:read");

    @Test
    void reroutesAnonymousUsersToLogin() {
        var event = navigationEvent();
        var view = new TestPermissionProtectedView(() -> Optional.empty(), authorization());

        view.beforeEnter(event);

        assertThat(event.getUnknownReroute()).isEqualTo("login");
    }

    @Test
    void reroutesUnauthorizedUsersToAccessDenied() {
        var event = navigationEvent();
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(), 1);
        var view = new TestPermissionProtectedView(() -> Optional.of(user), authorization());

        view.beforeEnter(event);

        assertThat(event.getUnknownReroute()).isEqualTo("access-denied");
    }

    private static AuthorizationService authorization() {
        return new AuthorizationService() {
            @Override
            public boolean hasPermission(CurrentUser user, PermissionCode permission) {
                return user.permissions().contains(permission);
            }

            @Override
            public void requirePermission(CurrentUser user, PermissionCode permission) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static BeforeEnterEvent navigationEvent() {
        var registry = (RouteRegistry) Proxy.newProxyInstance(RouteRegistry.class.getClassLoader(),
                new Class<?>[] { RouteRegistry.class }, (proxy, method, arguments) -> {
                    if (method.getName().equals("getNavigationRouteTarget")) {
                        return new NavigationRouteTarget((String) arguments[0], null, Map.of());
                    }
                    return null;
                });
        return new BeforeEnterEvent(new Router(registry), NavigationTrigger.PROGRAMMATIC, new Location("orders"),
                TestPermissionProtectedView.class, new UI(), List.of());
    }

    private static final class TestPermissionProtectedView extends PermissionProtectedView {
        private TestPermissionProtectedView(CurrentUserProvider currentUserProvider, AuthorizationService authorization) {
            super(currentUserProvider, authorization);
        }

        @Override
        protected PermissionCode requiredPermission() {
            return REQUIRED;
        }
    }
}
