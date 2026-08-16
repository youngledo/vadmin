package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;

/// Composes the standard header, query controls, and data area of an administration page.
///
/// Domain views provide their fields, commands, and content. This pattern owns the shared page
/// hierarchy and full-height behavior so an application does not recreate an administration shell.
public final class AdminDataPage {

    private AdminDataPage() {
    }

    /**
     * Creates the standard administration query row, with fields preceding the search and reset actions.
     *
     * @param search the command that applies the supplied filters
     * @param reset the command that clears the supplied filters
     * @param fields domain-owned filter fields
     * @return a shared page toolbar
     */
    public static PageToolbar queryToolbar(Button search, Button reset, Component... fields) {
        var toolbar = new PageToolbar();
        toolbar.getFilters().setAlignItems(HorizontalLayout.Alignment.END);
        for (var field : fields) {
            toolbar.addFilter(field);
        }
        toolbar.addAction(search);
        toolbar.setResetAction(reset);
        return toolbar;
    }

    /**
     * Composes a full-height administration data page from a title, page action, query controls, and workspace.
     *
     * @param title page title
     * @param primaryAction optional page-level command
     * @param toolbar shared filter and command toolbar
     * @param content domain-owned data area and optional footer
     * @return the standard administration page frame
     */
    public static AdminPageFrame compose(String title, Component primaryAction, PageToolbar toolbar,
                                         Component... content) {
        var workspace = new VerticalLayout(content);
        workspace.setPadding(false);
        workspace.setSpacing(true);
        workspace.setSizeFull();
        for (var component : content) {
            if (component instanceof Grid<?>) {
                workspace.setFlexGrow(1, component);
            }
        }
        var header = new PageHeader(title);
        if (primaryAction != null) {
            header.addAction(primaryAction);
        }
        return new AdminPageFrame(header, toolbar, workspace);
    }

    /** Replaces the items in a native Vaadin grid. */
    public static <T> void setItems(Grid<T> grid, List<T> items) {
        grid.setItems(List.copyOf(items));
    }
}
