package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import jakarta.annotation.security.PermitAll;

@PageTitle("Audit log")
@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class AuditView extends PermissionProtectedView {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:audit:read");

    public AuditView(CurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.AuditRow.class, false);
        grid.addColumn(AdministrationQueryService.AuditRow::occurredAt).setHeader(getTranslation("system.audit.time")).setAutoWidth(true);
        grid.addColumn(AdministrationQueryService.AuditRow::action).setHeader(getTranslation("system.audit.action"));
        grid.addColumn(AdministrationQueryService.AuditRow::targetType).setHeader(getTranslation("system.audit.target-type"));
        grid.addColumn(AdministrationQueryService.AuditRow::targetId).setHeader(getTranslation("system.audit.target-id"));
        grid.addColumn(AdministrationQueryService.AuditRow::outcome).setHeader(getTranslation("system.audit.outcome"));
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::audit, "occurred_at");
        var header = PageHeader.translated("system.audit.title", "system.audit.intent");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "read-only-workspace");
        add(header, workspace);
        expand(workspace);
    }

    @Override protected PermissionCode requiredPermission() { return REQUIRED_PERMISSION; }
}
