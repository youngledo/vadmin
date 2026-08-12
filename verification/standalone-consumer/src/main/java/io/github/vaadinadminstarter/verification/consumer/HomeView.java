package io.github.vaadinadminstarter.verification.consumer;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = StandaloneConsumerLayout.class)
@PermitAll
public final class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H1("Standalone Consumer"));
    }
}
