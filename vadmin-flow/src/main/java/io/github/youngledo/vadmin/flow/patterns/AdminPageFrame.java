package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Objects;

/** Composes the shared header, optional controls, and work area of an administration page. */
public final class AdminPageFrame extends VerticalLayout {
    public AdminPageFrame(PageHeader header, PageToolbar controls, Component workspace) {
        setPadding(true);
        setSpacing(true);
        addClassName("admin-page-frame");

        var pageHeader = Objects.requireNonNull(header);
        var pageWorkspace = Objects.requireNonNull(workspace);
        pageHeader.addClassName("admin-page-header");
        pageWorkspace.addClassName("admin-page-workspace");
        add(pageHeader);
        if (controls != null) {
            controls.addClassName("admin-page-controls");
            add(controls);
        }
        add(pageWorkspace);
        expand(pageWorkspace);
    }
}
