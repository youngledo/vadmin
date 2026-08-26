package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class DataWorkspaceTest {
    @Test
    void providesALocalizedChineseSelectionSummary() {
        var translations = ResourceBundle.getBundle("i18n.flow", Locale.SIMPLIFIED_CHINESE);

        assertThat(MessageFormat.format(translations.getString("flow.workspace.selected"), 2))
                .isEqualTo("已选择 2 项");
        assertThat(translations.getString("flow.workspace.select")).isEqualTo("选择");
        assertThat(translations.getString("flow.workspace.cancel-selection")).isEqualTo("取消选择");
        assertThat(MessageFormat.format(translations.getString("flow.workspace.select-item"), "admin"))
                .isEqualTo("选择 admin");
    }

    @Test
    void reportsSelectedItemsAndEnablesBulkActionsWhenRowsAreSelected() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var disable = new Button("Disable selected");

        workspace.addBulkAction(disable);
        grid.select(row);

        assertThat(workspace.getSelectedItemCount()).isOne();
        assertThat(workspace.getSelectionSummary()).isEqualTo("1 selected");
        assertThat(disable.isEnabled()).isTrue();
    }

    @Test
    void preservesAConsumerDisabledBulkActionAfterRowsAreSelected() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var restricted = new Button("Restricted action");
        restricted.setEnabled(false);

        workspace.addBulkAction(restricted);
        grid.select(row);

        assertThat(restricted.isEnabled()).isFalse();
    }

    @Test
    void evaluatesExplicitBulkActionEligibilityAcrossSelectionChanges() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var restricted = new Button("Restricted action");
        var eligible = new AtomicBoolean(true);

        workspace.addBulkAction(restricted, eligible::get);
        grid.select(row);
        eligible.set(false);
        grid.deselectAll();
        grid.select(row);

        assertThat(restricted.isEnabled()).isFalse();
    }

    @Test
    void usesInitialEligibilityForLegacyBulkActionsDespiteLaterButtonChanges() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var legacy = new Button("Legacy action");

        workspace.addBulkAction(legacy);
        legacy.setEnabled(false);
        grid.select(row);

        assertThat(legacy.isEnabled()).isTrue();

        legacy.setEnabled(false);
        grid.deselectAll();
        grid.select(row);

        assertThat(legacy.isEnabled()).isTrue();
    }

    @Test
    void evaluatesExplicitBulkActionEligibilityWhenSelectionChangesFromZero() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var restricted = new Button("Restricted action");
        var eligible = new AtomicBoolean(true);

        workspace.addBulkAction(restricted, eligible::get);
        eligible.set(false);
        grid.select(row);

        assertThat(restricted.isEnabled()).isFalse();
    }

    @Test
    void refreshesExplicitBulkActionEligibilityWithoutASelectionChange() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var restricted = new Button("Restricted action");
        var eligible = new AtomicBoolean(true);

        workspace.addBulkAction(restricted, eligible::get);
        grid.select(row);
        eligible.set(false);
        workspace.refreshBulkActions();

        assertThat(restricted.isEnabled()).isFalse();
    }

    @Test
    void exposesBusyEmptyAndFailureStatesWithAccessibleStatusText() {
        var workspace = new DataWorkspace<>(new Grid<Row>(Row.class, false));
        var footer = new Div();
        workspace.setFooter(footer);

        assertThat(workspace.getElement().getAttribute("data-admin-workspace-state")).isEqualTo("ready");
        assertThat(workspace.getFooter()).isSameAs(footer);
        assertThat(footer.isVisible()).isTrue();

        workspace.setBusy(true);
        assertThat(workspace.getState()).isEqualTo(DataWorkspace.State.BUSY);
        assertThat(workspace.getStatusMessage()).isEqualTo("Loading data");
        assertThat(workspace.getClassNames()).contains("admin-page-workspace");
        assertThat(workspace.getElement().getAttribute("data-admin-workspace-state")).isEqualTo("busy");
        assertThat(footer.isVisible()).isFalse();

        workspace.showEmpty(new EmptyState("No users", "Create a user to begin."));
        assertThat(workspace.getState()).isEqualTo(DataWorkspace.State.EMPTY);
        assertThat(workspace.getStatusMessage()).isEqualTo("No users");
        assertThat(workspace.getElement().getAttribute("data-admin-workspace-state")).isEqualTo("empty");
        assertThat(footer.isVisible()).isFalse();

        workspace.showFailure("Unable to load users");
        assertThat(workspace.getState()).isEqualTo(DataWorkspace.State.FAILURE);
        assertThat(workspace.getStatusMessage()).isEqualTo("Unable to load users");
        assertThat(workspace.getElement().getAttribute("data-admin-workspace-state")).isEqualTo("failure");
        assertThat(footer.isVisible()).isFalse();

        workspace.showData();

        assertThat(workspace.getElement().getAttribute("data-admin-workspace-state")).isEqualTo("ready");
        assertThat(workspace.getFooter()).isSameAs(footer);
        assertThat(footer.isVisible()).isTrue();
    }

    @Test
    void disablesBulkActionsWhenEmptyOrFailureStatesRetainGridSelection() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        grid.setItems(List.of(row));
        var workspace = new DataWorkspace<>(grid);
        var disable = new Button("Disable selected");

        workspace.addBulkAction(disable);
        grid.select(row);
        workspace.showEmpty(new EmptyState("No users", "Create a user to begin."));

        assertThat(disable.isEnabled()).isFalse();

        workspace.showFailure("Unable to load users");

        assertThat(disable.isEnabled()).isFalse();
    }

    @Test
    void canHideSelectionControlsForReadOnlyWorkspaces() {
        var workspace = new DataWorkspace<>(new Grid<Row>(Row.class, false));

        workspace.setSelectionBarVisible(false);

        assertThat(workspace.isSelectionBarVisible()).isFalse();
    }

    @Test
    void supportsNonSelectableGridsWithoutSelectionState() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        var workspace = new DataWorkspace<>(grid);

        assertThat(workspace.getSelectedItemCount()).isZero();
        assertThat(workspace.getSelectionSummary()).isEqualTo("0 selected");
    }

    @Test
    void switchesToTheConfiguredCompactListAtTheSharedBreakpoint() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var workspace = new DataWorkspace<>(grid);
        workspace.setCompactItemRenderer(row -> new CompactDataItem(row.name()), Row::name);

        workspace.applyViewportWidth(DataWorkspace.COMPACT_BREAKPOINT);

        assertThat(grid.isVisible()).isFalse();
        assertThat(workspace.getCompactList().isVisible()).isTrue();
        assertThat(workspace.isSelectionBarVisible()).isFalse();
        assertThat(workspace.getCompactList().getItemAccessibleNameGenerator().apply(new Row("Ada")))
                .isEqualTo("Ada");

        workspace.showEmpty(new EmptyState("No users", "Create a user to begin."));
        assertThat(workspace.getCompactList().isVisible()).isFalse();

        workspace.showData();
        assertThat(workspace.getCompactList().isVisible()).isTrue();

        workspace.applyViewportWidth(DataWorkspace.COMPACT_BREAKPOINT + 1);

        assertThat(grid.isVisible()).isTrue();
        assertThat(workspace.getCompactList().isVisible()).isFalse();
        assertThat(workspace.isSelectionBarVisible()).isTrue();
    }

    @Test
    void keepsTheGridVisibleWhenNoCompactRendererIsConfigured() {
        var grid = new Grid<Row>(Row.class, false);
        var workspace = new DataWorkspace<>(grid);

        workspace.applyViewportWidth(390);

        assertThat(grid.isVisible()).isTrue();
        assertThat(workspace.getCompactList().isVisible()).isFalse();
    }

    @Test
    void entersExplicitCompactSelectionAndSynchronizesCheckboxesWithTheGrid() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        var workspace = new DataWorkspace<>(grid);
        workspace.setCompactItemRenderer(item -> {
            var compactItem = new CompactDataItem(item.name());
            compactItem.addActions(new Button("Details"));
            return compactItem;
        }, Row::name);
        workspace.addBulkAction(new Button("Disable"));
        workspace.setItems(List.of(row));
        workspace.applyViewportWidth(390);

        workspace.compactSelectionTrigger().click();

        assertThat(workspace.isCompactSelectionMode()).isTrue();
        assertThat(workspace.isSelectionBarVisible()).isTrue();
        assertThat(workspace.compactSelectionTrigger().isVisible()).isFalse();
        assertThat(workspace.compactSelectionCancel().isVisible()).isTrue();
        assertThat(workspace.selectionBar().getStyle().get("position")).isEqualTo("sticky");
        assertThat(workspace.selectionBar().getStyle().get("bottom")).isEqualTo("0");

        var selectableItem = (HorizontalLayout) workspace.renderCompactItem(row);
        var checkbox = (Checkbox) selectableItem.getComponentAt(0);
        var compactItem = (CompactDataItem) selectableItem.getComponentAt(1);
        assertThat(checkbox.getAriaLabel()).hasValue("Select Ada");
        assertThat(checkbox.getValue()).isFalse();
        assertThat(compactItem.isSelectionMode()).isTrue();
        assertThat(compactItem.getActions().isVisible()).isFalse();

        checkbox.setValue(true);

        assertThat(grid.getSelectedItems()).containsExactly(row);
        assertThat(workspace.getSelectedItemCount()).isOne();
        assertThat(((Button) workspace.getBulkActions().getComponentAt(0)).isEnabled()).isTrue();
    }

    @Test
    void cancellingCompactSelectionClearsTheGridAndRestoresDefaultPresentation() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        var workspace = new DataWorkspace<>(grid);
        workspace.setCompactItemRenderer(item -> new CompactDataItem(item.name()), Row::name);
        workspace.addBulkAction(new Button("Disable"));
        workspace.setItems(List.of(row));
        workspace.applyViewportWidth(390);
        workspace.setCompactSelectionMode(true);
        grid.select(row);

        workspace.compactSelectionCancel().click();

        assertThat(workspace.isCompactSelectionMode()).isFalse();
        assertThat(grid.getSelectedItems()).isEmpty();
        assertThat(workspace.isSelectionBarVisible()).isFalse();
        assertThat(workspace.compactSelectionTrigger().isVisible()).isTrue();
        assertThat(workspace.selectionBar().getStyle().get("position")).isNull();
        assertThat(workspace.renderCompactItem(row)).isInstanceOf(CompactDataItem.class);
    }

    @Test
    void doesNotOfferCompactSelectionWithoutBulkActionsOrGridSelection() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        var workspace = new DataWorkspace<>(grid);
        workspace.setCompactItemRenderer(item -> new CompactDataItem(item.name()), Row::name);
        workspace.applyViewportWidth(390);

        workspace.setCompactSelectionMode(true);

        assertThat(workspace.isCompactSelectionMode()).isFalse();
        assertThat(workspace.compactSelectionTrigger().isVisible()).isFalse();
    }

    @Test
    void leavingTheCompactBreakpointRestoresOrdinaryCompactItems() {
        var grid = new Grid<Row>(Row.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        var row = new Row("Ada");
        var workspace = new DataWorkspace<>(grid);
        workspace.setCompactItemRenderer(item -> new CompactDataItem(item.name()), Row::name);
        workspace.addBulkAction(new Button("Disable"));
        workspace.applyViewportWidth(390);
        workspace.setCompactSelectionMode(true);

        workspace.applyViewportWidth(DataWorkspace.COMPACT_BREAKPOINT + 1);

        assertThat(workspace.isCompactSelectionMode()).isFalse();
        assertThat(workspace.selectionBar().getStyle().get("position")).isNull();
        assertThat(workspace.renderCompactItem(row)).isInstanceOf(CompactDataItem.class);

        workspace.applyViewportWidth(390);

        assertThat(workspace.isCompactSelectionMode()).isFalse();
        assertThat(workspace.renderCompactItem(row)).isInstanceOf(CompactDataItem.class);
    }

    private record Row(String name) { }
}
