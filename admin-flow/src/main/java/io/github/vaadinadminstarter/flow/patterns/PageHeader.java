package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Objects;

/** A page title, supporting context, location, and page-level action area. */
public final class PageHeader extends VerticalLayout {
    private final String title;
    private final String description;
    private final VerticalLayout location = new VerticalLayout();
    private final HorizontalLayout actions = new HorizontalLayout();

    public PageHeader(String title) {
        this(title, null);
    }

    public PageHeader(String title, String description) {
        this.title = Objects.requireNonNull(title);
        this.description = description;
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        location.setPadding(false);
        location.setSpacing(false);
        location.setVisible(false);
        actions.setPadding(false);
        actions.setSpacing(true);

        var heading = new VerticalLayout(new H1(title));
        heading.setPadding(false);
        heading.setSpacing(false);
        if (description != null && !description.isBlank()) {
            heading.add(new Paragraph(description));
        }
        var main = new HorizontalLayout(heading, actions);
        main.setWidthFull();
        main.setAlignItems(Alignment.CENTER);
        main.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(location, main);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Component getLocation() {
        return location.getComponentCount() == 0 ? null : location.getComponentAt(0);
    }

    public void setLocation(Component component) {
        location.removeAll();
        if (component != null) {
            location.add(component);
            location.setVisible(true);
        } else {
            location.setVisible(false);
        }
    }

    public HorizontalLayout getActions() {
        return actions;
    }

    public void addAction(Component action) {
        actions.add(Objects.requireNonNull(action));
    }
}
