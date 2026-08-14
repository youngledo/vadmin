package io.github.youngledo.vadmin.springflow.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.auth.AccessCheckDecision;
import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import com.vaadin.flow.server.auth.NavigationContext;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import io.github.youngledo.vadmin.flow.navigation.AdminNavigationGroup;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminModuleNavigationAccessCheckerTest {
    private static final PermissionCode READ = PermissionCode.of("sample:record:read");

    @Test
    void allowsTheDeclaredModuleRouteForAnAuthorizedUser() {
        var checker = checker(Optional.of(user(READ)));

        assertThat(checker.check(context("sample")).decision()).isEqualTo(AccessCheckDecision.ALLOW);
    }

    @Test
    void deniesTheDeclaredModuleRouteForAnUnauthorizedUser() {
        var checker = checker(Optional.of(user()));

        assertThat(checker.check(context("sample")).decision()).isEqualTo(AccessCheckDecision.DENY);
    }

    @Test
    void abstainsFromRoutesThatAreNotContributedByAModule() {
        var checker = checker(Optional.of(user()));

        assertThat(checker.check(context("login")).decision()).isEqualTo(AccessCheckDecision.NEUTRAL);
    }

    @Test
    void abstainsWhileVaadinRendersAnAccessDeniedErrorView() {
        var checker = checker(Optional.of(user()));

        assertThat(checker.check(errorContext("sample")).decision()).isEqualTo(AccessCheckDecision.NEUTRAL);
    }

    @Test
    void makesModuleMetadataAuthoritativeWhenViewAnnotationsAlsoAllowTheRoute() {
        var checker = checker(Optional.of(user()));
        var resolver = new AdminModuleAccessCheckDecisionResolver(checker);

        assertThat(resolver.resolve(List.of(AccessCheckResult.allow(), AccessCheckResult.deny("Missing permission")),
                context("sample")).decision()).isEqualTo(AccessCheckDecision.DENY);
    }

    private static AdminModuleNavigationAccessChecker checker(Optional<CurrentUser> user) {
        var modules = new AdminModuleRegistry(List.of(new AdminModule("sample",
                List.of(new AdminNavigationGroup("sample", "sample.navigation", 100)),
                List.of(new AdminPage("sample.list", "sample", "sample.list.title", "sample.list.intent", "briefcase",
                        100, "sample", READ, Div.class)), Set.of(READ),
                List.of(new AdminMessageBundle("sample", "i18n.sample")))));
        CurrentUserProvider currentUser = () -> user;
        AuthorizationService authorization = new AuthorizationService() {
            @Override public boolean hasPermission(CurrentUser actor, PermissionCode permission) {
                return actor.permissions().contains(permission);
            }

            @Override public void requirePermission(CurrentUser actor, PermissionCode permission) {
                throw new UnsupportedOperationException();
            }
        };
        return new AdminModuleNavigationAccessChecker(modules, currentUser, authorization);
    }

    private static CurrentUser user(PermissionCode... permissions) {
        return new CurrentUser(UUID.randomUUID(), "reader", Set.of(permissions), 0);
    }

    private static NavigationContext context(String path) {
        return context(path, false);
    }

    private static NavigationContext errorContext(String path) {
        return context(path, true);
    }

    private static NavigationContext context(String path, boolean errorHandling) {
        return new NavigationContext(new Router(configuredRegistry()), Div.class, new Location(path),
                com.vaadin.flow.router.RouteParameters.empty(), null, role -> false, errorHandling, true);
    }

    private static com.vaadin.flow.server.RouteRegistry configuredRegistry() {
        var registry = ApplicationRouteRegistry.getInstance(new TestVaadinContext());
        RouteConfiguration.forRegistry(registry).setRoute("sample", Div.class);
        return registry;
    }

    private static final class TestVaadinContext implements com.vaadin.flow.server.VaadinContext {
        private final java.util.Map<Class<?>, Object> attributes = new java.util.HashMap<>();

        @Override public <T> T getAttribute(Class<T> type, java.util.function.Supplier<T> supplier) {
            var value = attributes.get(type);
            if (value == null && supplier != null) {
                value = supplier.get();
                attributes.put(type, value);
            }
            return type.cast(value);
        }

        @Override public <T> void setAttribute(Class<T> type, T value) {
            if (value == null) attributes.remove(type); else attributes.put(type, value);
        }

        @Override public void removeAttribute(Class<?> type) { attributes.remove(type); }
        @Override public java.util.Enumeration<String> getContextParameterNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public String getContextParameter(String name) { return null; }
    }
}
