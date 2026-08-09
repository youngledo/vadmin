package io.github.vaadinadminstarter.springflow.navigation;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;

/** Registers contributed administration pages in each Vaadin service's route registry. */
public final class AdminModuleRouteRegistrar {
    private final AdminModuleRegistry modules;
    private final AdminHostLayout hostLayout;
    private final Set<VaadinContext> registeredContexts = Collections.newSetFromMap(
            new IdentityHashMap<VaadinContext, Boolean>());

    public AdminModuleRouteRegistrar(AdminModuleRegistry modules, AdminHostLayout hostLayout) {
        this.modules = Objects.requireNonNull(modules, "modules");
        this.hostLayout = Objects.requireNonNull(hostLayout, "hostLayout");
    }

    public void register(VaadinContext context) {
        Objects.requireNonNull(context, "context");
        synchronized (registeredContexts) {
            if (registeredContexts.contains(context)) {
                return;
            }

            var routes = RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(context));
            routes.update(() -> {
                for (var page : modules.pages()) {
                    if (routes.isPathAvailable(page.route())) {
                        var existing = routes.getRoute(page.route()).map(Class::getName).orElse("an existing route");
                        throw new IllegalStateException("Cannot register admin page route '" + page.route()
                                + "' because it is already occupied by " + existing);
                    }
                }
                modules.pages().forEach(page -> routes.setRoute(page.route(), page.viewType(),
                        List.of(hostLayout.layoutType())));
            });
            registeredContexts.add(context);
        }
    }
}
