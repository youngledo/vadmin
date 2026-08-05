package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
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
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::permissions, "code");
        add(new H1("权限目录"), grid);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:permission:read"); }
}
