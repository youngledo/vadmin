package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Objects;

/** A page title, supporting context, location, and page-level action area. */
public final class PageHeader extends VerticalLayout implements LocaleChangeObserver {
    private final String titleKey;
    private final String descriptionKey;
    private final boolean translated;
    private final H2 title = new H2();
    private final Paragraph description = new Paragraph();
    private final VerticalLayout location = new VerticalLayout();
    private final HorizontalLayout actions = new HorizontalLayout();

    public PageHeader(String title) {
        this(title, null);
    }

    public PageHeader(String title, String description) {
        this.titleKey = Objects.requireNonNull(title);
        this.descriptionKey = description;
        this.translated = false;
        initialize();
    }

    private PageHeader(String titleKey, String descriptionKey, boolean translated) {
        this.titleKey = Objects.requireNonNull(titleKey);
        this.descriptionKey = descriptionKey;
        this.translated = translated;
        initialize();
    }

    /** Creates a header whose text follows the active UI locale. */
    public static PageHeader translated(String titleKey, String descriptionKey) {
        return new PageHeader(titleKey, descriptionKey, true);
    }

    private void initialize() {
        setPadding(false);
        setSpacing(true);
        setWidthFull();
        addClassName("admin-page-header");

        location.setPadding(false);
        location.setSpacing(false);
        location.setVisible(false);
        actions.setPadding(false);
        actions.setSpacing(true);

        updateText();
        var heading = new VerticalLayout(title);
        heading.setPadding(false);
        heading.setSpacing(false);
        if (descriptionKey != null && !descriptionKey.isBlank()) {
            heading.add(description);
        }
        var main = new HorizontalLayout(heading, actions);
        main.setWidthFull();
        main.setAlignItems(Alignment.CENTER);
        main.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(location, main);
    }

    public String getTitle() {
        return title.getText();
    }

    public String getDescription() {
        return description.getText();
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

    @Override
    public void localeChange(com.vaadin.flow.i18n.LocaleChangeEvent event) {
        updateText();
    }

    private void updateText() {
        title.setText(text(titleKey));
        if (descriptionKey != null) {
            description.setText(text(descriptionKey));
        }
    }

    private String text(String value) {
        return translated ? getTranslation(value) : value;
    }
}
