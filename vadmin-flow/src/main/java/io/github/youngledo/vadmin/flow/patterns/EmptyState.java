package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Objects;

/** A concise no-results state with an optional next action. */
public final class EmptyState extends VerticalLayout {
    private final String title;
    private final String description;
    private Component action;

    public EmptyState(String title, String description) {
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        addClassName("admin-empty-state");
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        getElement().setAttribute("role", "status");
        add(new H2(title), new Paragraph(description));
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Component getAction() {
        return action;
    }

    public void setAction(Component action) {
        if (this.action != null) {
            remove(this.action);
        }
        this.action = action;
        if (action != null) {
            add(action);
        }
    }
}
