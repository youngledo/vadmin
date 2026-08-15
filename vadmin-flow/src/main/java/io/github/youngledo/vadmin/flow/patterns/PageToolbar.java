package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** A compact query area with filter, reset, supplementary, and primary action slots. */
public final class PageToolbar extends HorizontalLayout {
    private final HorizontalLayout filters = new HorizontalLayout();
    private final HorizontalLayout actions = new HorizontalLayout();
    private final Map<Button, Boolean> enabledBeforeBusy = new LinkedHashMap<>();
    private Button resetAction;
    private Button primaryAction;
    private boolean busy;

    public PageToolbar() {
        setWidthFull();
        setPadding(false);
        setAlignItems(Alignment.END);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("admin-page-controls");
        filters.setPadding(false);
        filters.setSpacing(true);
        filters.setWrap(true);
        actions.setPadding(false);
        actions.setSpacing(true);
        add(filters, actions);
    }

    public HorizontalLayout getFilters() {
        return filters;
    }

    public void addFilter(Component filter) {
        filters.add(Objects.requireNonNull(filter));
    }

    public HorizontalLayout getActions() {
        return actions;
    }

    public void addAction(Component action) {
        actions.add(Objects.requireNonNull(action));
        if (action instanceof Button button && busy) {
            disableForBusy(button);
        }
    }

    public Button getResetAction() {
        return resetAction;
    }

    public void setResetAction(Button action) {
        replaceAction(resetAction, action);
        resetAction = action;
    }

    public Button getPrimaryAction() {
        return primaryAction;
    }

    public void setPrimaryAction(Button action) {
        replaceAction(primaryAction, action);
        primaryAction = action;
    }

    public boolean isBusy() {
        return busy;
    }

    /**
     * Temporarily disables command actions, then restores their pre-busy state when cleared.
     * Apply caller-owned command availability changes after {@code setBusy(false)}.
     */
    public void setBusy(boolean busy) {
        if (this.busy == busy) {
            return;
        }
        this.busy = busy;
        if (busy) {
            actions.getChildren().filter(Button.class::isInstance).map(Button.class::cast).forEach(this::disableForBusy);
        } else {
            enabledBeforeBusy.forEach(Button::setEnabled);
            enabledBeforeBusy.clear();
        }
    }

    private void replaceAction(Button previous, Button next) {
        if (previous != null) {
            actions.remove(previous);
            enabledBeforeBusy.remove(previous);
        }
        if (next != null) {
            actions.add(next);
            if (busy) {
                disableForBusy(next);
            }
        }
    }

    private void disableForBusy(Button action) {
        enabledBeforeBusy.putIfAbsent(action, action.isEnabled());
        action.setEnabled(false);
    }
}
