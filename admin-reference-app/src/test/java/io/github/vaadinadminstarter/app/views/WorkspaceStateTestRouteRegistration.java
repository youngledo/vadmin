package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import java.util.List;
import org.springframework.stereotype.Component;

/** Registers the test-only workspace state view using the production dynamic-route mechanism. */
@Component
final class WorkspaceStateTestRouteRegistration implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        var routes = RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(event.getSource().getContext()));
        routes.setRoute("workspace-states", WorkspaceStateTestView.class, List.of(MainLayout.class));
    }
}
