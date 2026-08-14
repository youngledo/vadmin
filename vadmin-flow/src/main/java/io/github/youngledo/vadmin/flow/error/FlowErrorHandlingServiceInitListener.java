package io.github.youngledo.vadmin.flow.error;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public final class FlowErrorHandlingServiceInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(sessionEvent ->
                sessionEvent.getSession().setErrorHandler(new FlowErrorHandler()));
    }
}
