package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("access-denied")
@PageTitle("无权访问")
public final class AccessDeniedView extends VerticalLayout {
    public AccessDeniedView() {
        add(new H1("无权访问"));
    }
}
