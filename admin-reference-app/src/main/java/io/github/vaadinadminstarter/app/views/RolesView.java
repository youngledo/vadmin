package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.platform.access.GrantPermissionCommand;
import io.github.vaadinadminstarter.platform.access.GrantPermissionUseCase;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;

@Route(value = "roles", layout = MainLayout.class)
@PageTitle("角色")
public final class RolesView extends SecuredView {
    public RolesView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, PermissionCatalog catalog, GrantPermissionUseCase grants) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.RoleRow.class, false);
        grid.addColumn(AdministrationQueryService.RoleRow::code).setHeader("角色代码").setAutoWidth(true);
        grid.addColumn(AdministrationQueryService.RoleRow::permissionCount).setHeader("权限数量");
        grid.setSizeFull();
        var pages = new PagedGrid<>(grid, queries::roles, "code");
        var role = new ComboBox<String>("角色");
        role.setItems(queries.roles().stream().map(AdministrationQueryService.RoleRow::code).toList());
        var permission = new ComboBox<PermissionCode>("权限");
        permission.setItems(catalog.all().stream().sorted(java.util.Comparator.comparing(PermissionCode::value)).toList());
        permission.setItemLabelGenerator(PermissionCode::value);
        var grant = new Button("授予权限", event -> {
            if (role.getValue() == null || permission.getValue() == null) {
                return;
            }
            grants.grant(requireCurrentUser(), new GrantPermissionCommand(role.getValue(), permission.getValue()));
            pages.refresh();
        });
        grant.setEnabled(false);
        role.addValueChangeListener(event -> grant.setEnabled(role.getValue() != null && permission.getValue() != null));
        permission.addValueChangeListener(event -> grant.setEnabled(role.getValue() != null && permission.getValue() != null));
        grant.setVisible(authorization.hasPermission(requireCurrentUser(), PermissionCode.of("system:role:grant")));
        add(new H1("角色"), new HorizontalLayout(role, permission, grant), grid);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:role:read"); }
}
