package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Test-only route that exposes real DataWorkspace state presentations to browser assertions. */
@PermitAll
@Component
@Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class WorkspaceStateTestView extends VerticalLayout implements BeforeEnterObserver {
    private final DataWorkspace<String> workspace;

    public WorkspaceStateTestView() {
        var grid = new Grid<String>();
        grid.addColumn(value -> value).setHeader("Value");
        grid.setSizeFull();
        workspace = new DataWorkspace<>(grid);
        workspace.getElement().setAttribute("data-testid", "workspace-state-workspace");
        var frame = new AdminPageFrame(new PageHeader("Workspace states"), null, workspace);
        add(frame);
        expand(frame);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if ("failure".equals(event.getLocation().getQueryParameters().getSingleParameter("state").orElse("busy"))) {
            workspace.showFailure("Test workspace failure");
        } else {
            workspace.setBusy(true);
        }
    }
}
