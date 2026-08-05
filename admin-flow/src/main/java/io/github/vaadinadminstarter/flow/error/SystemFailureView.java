package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("system-error")
@PageTitle("系统错误")
public final class SystemFailureView extends VerticalLayout {
    public SystemFailureView() {
        add(new H1("系统暂时不可用"));
    }
}
