package io.github.vaadinadminstarter.flow.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

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

        assertThat(event.hasRerouteTarget()).isTrue();
        assertThat(event.getRerouteTargetType()).isEqualTo(LoginView.class);
        assertThat(event.getRerouteUrl()).isEqualTo("login");
    }

    @Test
    void reroutesUnauthorizedUsersToAccessDenied() {
        var event = navigationEvent();
        var user = new CurrentUser(UUID.randomUUID(), "operator", Set.of(), 1);
        var view = new TestPermissionProtectedView(() -> Optional.of(user), authorization());

        view.beforeEnter(event);

        assertThat(event.hasRerouteTarget()).isTrue();
        assertThat(event.getRerouteTargetType()).isEqualTo(AccessDeniedView.class);
        assertThat(event.getRerouteUrl()).isEqualTo("access-denied");
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
        var registry = configuredRegistry();
        return new BeforeEnterEvent(new Router(registry), NavigationTrigger.PROGRAMMATIC, new Location("orders"),
                TestPermissionProtectedView.class, new UI(), List.of());
    }

    private static RouteRegistry configuredRegistry() {
        var registry = ApplicationRouteRegistry.getInstance(new TestVaadinContext());
        var configuration = RouteConfiguration.forRegistry(registry);
        configuration.setRoute("login", LoginView.class);
        configuration.setRoute("access-denied", AccessDeniedView.class);
        return registry;
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

    static final class LoginView extends Div {
    }

    static final class AccessDeniedView extends Div {
    }

    private static final class TestVaadinContext implements VaadinContext {
        private final java.util.Map<Class<?>, Object> attributes = new HashMap<>();

        @Override
        public <T> T getAttribute(Class<T> type, java.util.function.Supplier<T> defaultValueSupplier) {
            var value = attributes.get(type);
            if (value == null && defaultValueSupplier != null) {
                value = defaultValueSupplier.get();
                attributes.put(type, value);
            }
            return type.cast(value);
        }

        @Override
        public <T> void setAttribute(Class<T> type, T value) {
            if (value == null) {
                attributes.remove(type);
            } else {
                attributes.put(type, value);
            }
        }

        @Override
        public void removeAttribute(Class<?> type) {
            attributes.remove(type);
        }

        @Override
        public java.util.Enumeration<String> getContextParameterNames() {
            return Collections.emptyEnumeration();
        }

        @Override
        public String getContextParameter(String name) {
            return null;
        }
    }
}
