package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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

    private record Row(String name) { }
}
