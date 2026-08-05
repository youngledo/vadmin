package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** A grid frame with selection-aware bulk actions and explicit data presentation states. */
public final class DataWorkspace<T> extends VerticalLayout {
    public enum State { READY, BUSY, EMPTY, FAILURE }

    private final Grid<T> grid;
    private final Span selectionSummary = new Span("0 selected");
    private final HorizontalLayout bulkActions = new HorizontalLayout();
    private final Div status = new Div();
    private final List<Button> selectionActions = new ArrayList<>();
    private final Map<Button, Boolean> inferredActionEligibility = new LinkedHashMap<>();
    private final Map<Button, BooleanSupplier> explicitActionEligibility = new LinkedHashMap<>();
    private final Map<Button, Boolean> actionEnabledBySelection = new LinkedHashMap<>();
    private Component stateView;
    private State state = State.READY;
    private int selectedItemCount;

    public DataWorkspace(Grid<T> grid) {
        this.grid = Objects.requireNonNull(grid);
        setPadding(false);
        setSpacing(true);
        setSizeFull();

        var selectionBar = new HorizontalLayout(selectionSummary, bulkActions);
        selectionBar.setWidthFull();
        selectionBar.setAlignItems(Alignment.CENTER);
        bulkActions.setPadding(false);
        bulkActions.setSpacing(true);
        status.getElement().setAttribute("role", "status");
        status.getElement().setAttribute("aria-live", "polite");
        status.setVisible(false);
        grid.setSizeFull();
        grid.addSelectionListener(event -> updateSelection(event.getAllSelectedItems().size()));
        add(selectionBar, status, grid);
        updateSelection(0);
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public State getState() {
        return state;
    }

    public int getSelectedItemCount() {
        return selectedItemCount;
    }

    public String getSelectionSummary() {
        return selectionSummary.getText();
    }

    public String getStatusMessage() {
        return status.getText();
    }

    public HorizontalLayout getBulkActions() {
        return bulkActions;
    }

    public void addBulkAction(Button action) {
        action = Objects.requireNonNull(action);
        inferredActionEligibility.put(action, action.isEnabled());
        registerBulkAction(action);
    }

    /**
     * Adds a bulk action whose caller-owned eligibility is evaluated independently from selection.
     */
    public void addBulkAction(Button action, BooleanSupplier eligibility) {
        action = Objects.requireNonNull(action);
        explicitActionEligibility.put(action, Objects.requireNonNull(eligibility));
        registerBulkAction(action);
    }

    private void registerBulkAction(Button action) {
        selectionActions.add(action);
        bulkActions.add(action);
        updateSelection(selectedItemCount);
    }

    public void setBusy(boolean busy) {
        if (busy) {
            showState(State.BUSY, "Loading data", null);
        } else if (state == State.BUSY) {
            showData();
        }
    }

    public void showEmpty(EmptyState emptyState) {
        showState(State.EMPTY, emptyState.getTitle(), Objects.requireNonNull(emptyState));
    }

    public void showFailure(String message) {
        showState(State.FAILURE, Objects.requireNonNull(message),
                new EmptyState("Unable to load data", message));
    }

    public void showData() {
        state = State.READY;
        clearStateView();
        status.setVisible(false);
        grid.setVisible(true);
        updateSelection(grid.getSelectedItems().size());
    }

    private void showState(State state, String message, Component stateView) {
        this.state = state;
        clearStateView();
        this.stateView = stateView;
        if (stateView != null) {
            addComponentAtIndex(getComponentCount() - 1, stateView);
        }
        status.setText(message);
        status.setVisible(true);
        grid.setVisible(state != State.EMPTY && state != State.FAILURE);
        updateSelection(grid.getSelectedItems().size());
    }

    private void clearStateView() {
        if (stateView != null) {
            remove(stateView);
            stateView = null;
        }
    }

    private void updateSelection(int count) {
        selectedItemCount = count;
        selectionSummary.setText(count == 1 ? "1 selected" : count + " selected");
        var enabled = count > 0 && state != State.BUSY;
        selectionActions.forEach(action -> updateActionAvailability(action, enabled));
    }

    private void updateActionAvailability(Button action, boolean selectionAvailable) {
        var explicitEligibility = explicitActionEligibility.get(action);
        if (explicitEligibility != null) {
            var actionEnabled = explicitEligibility.getAsBoolean() && selectionAvailable;
            action.setEnabled(actionEnabled);
            actionEnabledBySelection.put(action, actionEnabled);
            return;
        }
        var previousSelectionState = actionEnabledBySelection.get(action);
        if (previousSelectionState != null && action.isEnabled() != previousSelectionState) {
            inferredActionEligibility.put(action, action.isEnabled());
        }
        var actionEnabled = inferredActionEligibility.get(action) && selectionAvailable;
        action.setEnabled(actionEnabled);
        actionEnabledBySelection.put(action, actionEnabled);
    }
}
