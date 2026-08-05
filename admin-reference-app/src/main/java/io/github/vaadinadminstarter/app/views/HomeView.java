package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("工作台")
@PermitAll
public final class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H1("工作台"));
    }
}
