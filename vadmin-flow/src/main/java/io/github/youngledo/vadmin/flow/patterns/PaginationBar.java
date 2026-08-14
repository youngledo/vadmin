package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.util.Objects;

/** A compact, server-page navigation footer for a data workspace. */
public final class PaginationBar extends HorizontalLayout implements LocaleChangeObserver {
    private final Button previousAction;
    private final Button nextAction;
    private final Span summary = new Span();
    private int pageIndex;
    private int pageCount;
    private long total;

    public PaginationBar(Runnable previous, Runnable next) {
        previousAction = new Button();
        previousAction.addClickListener(event -> Objects.requireNonNull(previous).run());
        nextAction = new Button();
        nextAction.addClickListener(event -> Objects.requireNonNull(next).run());
        setPadding(false);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        addClassName("admin-pagination-bar");
        getElement().setAttribute("role", "navigation");
        updateText();
        updateAvailability();
        add(summary, previousAction, nextAction);
    }

    public void setPage(int pageIndex, int pageCount, long total) {
        if (pageIndex < 0 || pageCount < 0 || total < 0 || pageIndex >= pageCount && pageCount > 0) {
            throw new IllegalArgumentException("invalid page state");
        }
        this.pageIndex = pageIndex;
        this.pageCount = pageCount;
        this.total = total;
        setVisible(pageCount > 1);
        updateText();
        updateAvailability();
    }

    public Button getPreviousAction() {
        return previousAction;
    }

    public Button getNextAction() {
        return nextAction;
    }

    public String getSummary() {
        return summary.getText();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateText();
    }

    private void updateAvailability() {
        previousAction.setEnabled(pageIndex > 0);
        nextAction.setEnabled(pageIndex + 1 < pageCount);
    }

    private void updateText() {
        previousAction.setText(text("flow.pagination.previous", "Previous"));
        previousAction.setAriaLabel(text("flow.pagination.previous", "Previous"));
        nextAction.setText(text("flow.pagination.next", "Next"));
        nextAction.setAriaLabel(text("flow.pagination.next", "Next"));
        summary.setText(text("flow.pagination.summary", "Page {0} of {1}, {2} results",
                pageCount == 0 ? 0 : pageIndex + 1, pageCount, total));
    }

    private String text(String key, String fallback, Object... parameters) {
        if (getUI().isPresent()) {
            return getTranslation(key, parameters);
        }
        var result = fallback;
        for (var index = 0; index < parameters.length; index++) {
            result = result.replace("{" + index + "}", String.valueOf(parameters[index]));
        }
        return result;
    }
}
