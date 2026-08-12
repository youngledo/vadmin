package io.github.vaadinadminstarter.verification.consumer;

import com.example.orders.admin.OrdersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.router.Layout;
import jakarta.annotation.security.PermitAll;

@Layout
@PermitAll
@Uses(OrdersView.class)
public final class StandaloneConsumerLayout extends AppLayout {
    public StandaloneConsumerLayout() {
        addToNavbar(new Span("Standalone Consumer"));
        addToDrawer(new VerticalLayout());
    }
}
