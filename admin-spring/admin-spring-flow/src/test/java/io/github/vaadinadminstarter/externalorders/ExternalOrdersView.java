package io.github.vaadinadminstarter.externalorders;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.Div;

@Component
public final class ExternalOrdersView extends Div {
    private final OrdersService service;

    public ExternalOrdersView(OrdersService service) {
        this.service = service;
    }

    public OrdersService service() {
        return service;
    }
}
