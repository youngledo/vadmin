package io.github.youngledo.vadmin.springflow.navigation;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import java.util.Objects;

/** Supplies module metadata as a standard Vaadin navigation access rule. */
public final class AdminModuleNavigationAccessChecker implements NavigationAccessChecker {
    private final AdminModuleRegistry modules;
    private final CurrentUserProvider currentUser;
    private final AuthorizationService authorization;

    public AdminModuleNavigationAccessChecker(AdminModuleRegistry modules, CurrentUserProvider currentUser,
                                              AuthorizationService authorization) {
        this.modules = Objects.requireNonNull(modules, "modules");
        this.currentUser = Objects.requireNonNull(currentUser, "currentUser");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
    }

    @Override
    public AccessCheckResult check(NavigationContext context) {
        if (context.isErrorHandling()) {
            return context.neutral();
        }
        return modules.pages().stream()
                .filter(page -> page.route().equals(context.getLocation().getPath()))
                .findFirst()
                .map(page -> currentUser.currentUser()
                        .filter(user -> authorization.hasPermission(user, page.requiredPermission()))
                        .map(user -> context.allow())
                        .orElseGet(() -> context.deny("Missing module permission")))
                .orElseGet(context::neutral);
    }
}
