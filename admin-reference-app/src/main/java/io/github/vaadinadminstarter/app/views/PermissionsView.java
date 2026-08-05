package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;

@Route(value = "permissions", layout = MainLayout.class)
@PageTitle("权限目录")
public final class PermissionsView extends SecuredView {
    public PermissionsView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                           AdministrationQueryService queries) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.PermissionRow.class, false);
        grid.addColumn(AdministrationQueryService.PermissionRow::code).setHeader("权限代码").setAutoWidth(true);
        grid.addColumn(permission -> permission.systemManaged() ? "系统管理" : "自定义").setHeader("来源");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::permissions, "code");
        var header = new PageHeader("权限目录", "查看系统中可授予的权限及其来源。");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "read-only-workspace");
        add(header, workspace);
        expand(workspace);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:permission:read"); }
}
