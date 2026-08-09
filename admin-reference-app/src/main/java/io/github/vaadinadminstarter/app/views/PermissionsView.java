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

@PageTitle("Permission catalog")
@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class PermissionsView extends PermissionProtectedView {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:permission:read");

    public PermissionsView(CurrentUserProvider currentUser, AuthorizationService authorization,
                           AdministrationQueryService queries) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.PermissionRow.class, false);
        grid.addColumn(AdministrationQueryService.PermissionRow::code).setHeader(getTranslation("system.permissions.code")).setAutoWidth(true);
        grid.addColumn(permission -> permission.systemManaged() ? getTranslation("system.permissions.system-managed") : getTranslation("system.permissions.custom")).setHeader(getTranslation("system.permissions.source"));
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::permissions, "code");
        var header = PageHeader.translated("system.permissions.title", "system.permissions.intent");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "read-only-workspace");
        add(header, workspace);
        expand(workspace);
    }

    @Override protected PermissionCode requiredPermission() { return REQUIRED_PERMISSION; }
}
