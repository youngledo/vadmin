package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** A grid frame with selection-aware bulk actions and explicit data presentation states. */
public final class DataWorkspace<T> extends VerticalLayout implements LocaleChangeObserver {
    static final int COMPACT_BREAKPOINT = 640;

    public enum State { READY, BUSY, EMPTY, FAILURE }

    private final Grid<T> grid;
    private final Span selectionSummary = new Span();
    private final HorizontalLayout bulkActions = new HorizontalLayout();
    private final HorizontalLayout selectionBar = new HorizontalLayout(selectionSummary, bulkActions);
    private final Div status = new Div();
    private final VirtualList<T> compactList = new VirtualList<>();
    private final List<Button> selectionActions = new ArrayList<>();
    private final Map<Button, BooleanSupplier> actionEligibility = new LinkedHashMap<>();
    private Component stateView;
    private Component footer;
    private State state = State.READY;
    private int selectedItemCount;
    private boolean selectionBarRequested = true;
    private boolean compactViewConfigured;
    private boolean compactViewport;
    private Registration resizeRegistration;

    public DataWorkspace(Grid<T> grid) {
        this.grid = Objects.requireNonNull(grid);
        setPadding(false);
        setSpacing(true);
        setSizeFull();
        addClassName("admin-page-workspace");

        selectionBar.setWidthFull();
        selectionBar.setAlignItems(Alignment.CENTER);
        bulkActions.setPadding(false);
        bulkActions.setSpacing(true);
        status.getElement().setAttribute("role", "status");
        status.getElement().setAttribute("aria-live", "polite");
        status.setVisible(false);
        grid.setSizeFull();
        compactList.setSizeFull();
        compactList.setVisible(false);
        compactList.addClassName("admin-page-compact-list");
        if (grid.getSelectionMode() != Grid.SelectionMode.NONE) {
            grid.addSelectionListener(event -> updateSelection(event.getAllSelectedItems().size()));
        }
        addAttachListener(event -> connectResponsivePresentation(event.getUI()));
        addDetachListener(event -> disconnectResponsivePresentation());
        add(selectionBar, status, grid, compactList);
        updateSelection(0);
        updateStatePresentation();
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public VirtualList<T> getCompactList() {
        return compactList;
    }

    /** Installs the narrow-screen entity renderer while retaining the Grid on wider screens. */
    public void setCompactItemRenderer(SerializableFunction<T, ? extends Component> renderer,
                                       SerializableFunction<T, String> accessibleNameGenerator) {
        Objects.requireNonNull(renderer);
        compactList.setRenderer(new ComponentRenderer<>(item -> renderer.apply(item)));
        compactList.setItemAccessibleNameGenerator(Objects.requireNonNull(accessibleNameGenerator));
        compactViewConfigured = true;
        updateStatePresentation();
    }

    /** Replaces the workspace data while preserving one consistent presentation state. */
    public void setItems(List<T> items) {
        var values = List.copyOf(Objects.requireNonNull(items));
        grid.setItems(values);
        compactList.setItems(values);
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

    /** Installs one caller-owned footer, typically a server-page navigation control. */
    public void setFooter(Component footer) {
        if (this.footer != null) {
            remove(this.footer);
        }
        this.footer = Objects.requireNonNull(footer);
        add(this.footer);
        updateStatePresentation();
    }

    public Component getFooter() {
        return footer;
    }

    /** Shows or hides selection controls for workspaces that support bulk operations. */
    public void setSelectionBarVisible(boolean visible) {
        selectionBarRequested = visible;
        updateStatePresentation();
    }

    public boolean isSelectionBarVisible() {
        return selectionBar.isVisible();
    }

    /**
     * Adds a bulk action with eligibility captured from its enabled state at registration time.
     * Use {@link #addBulkAction(Button, BooleanSupplier)} for caller-owned dynamic eligibility.
     */
    public void addBulkAction(Button action) {
        action = Objects.requireNonNull(action);
        var eligibleAtRegistration = action.isEnabled();
        addBulkAction(action, () -> eligibleAtRegistration);
    }

    /**
     * Adds a bulk action whose caller-owned eligibility is evaluated independently from selection.
     * Call {@link #refreshBulkActions()} after externally owned eligibility changes.
     */
    public void addBulkAction(Button action, BooleanSupplier eligibility) {
        action = Objects.requireNonNull(action);
        actionEligibility.put(action, Objects.requireNonNull(eligibility));
        registerBulkAction(action);
    }

    private void registerBulkAction(Button action) {
        selectionActions.add(action);
        bulkActions.add(action);
        updateSelection(selectedItemCount);
    }

    /** Re-evaluates bulk action eligibility using the current selection and workspace state. */
    public void refreshBulkActions() {
        updateSelection(grid.getSelectedItems().size());
    }

    public void setBusy(boolean busy) {
        if (busy) {
            showState(State.BUSY, text("flow.workspace.loading", "Loading data"), null);
        } else if (state == State.BUSY) {
            showData();
        }
    }

    public void showEmpty(EmptyState emptyState) {
        showState(State.EMPTY, emptyState.getTitle(), Objects.requireNonNull(emptyState));
    }

    public void showFailure(String message) {
        showState(State.FAILURE, Objects.requireNonNull(message),
                new EmptyState(text("flow.workspace.load-failed", "Unable to load data"), message));
    }

    public void showData() {
        state = State.READY;
        clearStateView();
        updateStatePresentation();
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
        updateStatePresentation();
        updateSelection(grid.getSelectedItems().size());
    }

    private void updateStatePresentation() {
        getElement().setAttribute("data-admin-workspace-state", state.name().toLowerCase(Locale.ROOT));
        status.setVisible(state != State.READY);
        var showData = state != State.EMPTY && state != State.FAILURE;
        var showCompact = compactViewConfigured && compactViewport;
        grid.setVisible(showData && !showCompact);
        compactList.setVisible(showData && showCompact);
        selectionBar.setVisible(selectionBarRequested && !showCompact);
        if (footer != null) {
            footer.setVisible(state == State.READY);
        }
    }

    private void clearStateView() {
        if (stateView != null) {
            remove(stateView);
            stateView = null;
        }
    }

    private void updateSelection(int count) {
        selectedItemCount = count;
        selectionSummary.setText(text("flow.workspace.selected", count + " selected", count));
        var enabled = count > 0 && state == State.READY;
        selectionActions.forEach(action -> updateActionAvailability(action, enabled));
    }

    private void updateActionAvailability(Button action, boolean selectionAvailable) {
        action.setEnabled(actionEligibility.get(action).getAsBoolean() && selectionAvailable);
    }

    void applyViewportWidth(int width) {
        if (width <= 0) {
            return;
        }
        compactViewport = width <= COMPACT_BREAKPOINT;
        updateStatePresentation();
    }

    private void connectResponsivePresentation(com.vaadin.flow.component.UI ui) {
        disconnectResponsivePresentation();
        var page = ui.getPage();
        resizeRegistration = page.addBrowserWindowResizeListener(event -> applyViewportWidth(event.getWidth()));
        page.getExtendedClientDetails().refresh(details -> {
            if (isAttached() && getUI().filter(current -> current == ui).isPresent()) {
                applyViewportWidth(details.getWindowInnerWidth());
            }
        });
    }

    private void disconnectResponsivePresentation() {
        if (resizeRegistration != null) {
            resizeRegistration.remove();
            resizeRegistration = null;
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateSelection(selectedItemCount);
        if (state == State.BUSY) status.setText(text("flow.workspace.loading", "Loading data"));
    }

    private String text(String key, String fallback, Object... parameters) {
        return getUI().isPresent() ? getTranslation(key, parameters) : fallback;
    }
}
