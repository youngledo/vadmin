package io.github.youngledo.vadmin.flow.navigation;

import com.vaadin.flow.component.html.Span;

/** A semantic icon that the host visual profile can render without affecting business code. */
public final class AdminIcon extends Span {
    private AdminIcon(AdminIconName name) {
        addClassName("admin-icon");
        getElement().setAttribute("data-admin-icon", name.cssValue());
        getElement().setAttribute("aria-hidden", "true");
        add(name.vaadinIcon().create());
    }

    public static AdminIcon of(AdminIconName name) {
        return new AdminIcon(name);
    }
}
