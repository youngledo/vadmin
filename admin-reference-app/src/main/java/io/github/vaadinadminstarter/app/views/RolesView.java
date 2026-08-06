package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.DetailDialog;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.OperationFeedback;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.platform.access.GrantPermissionCommand;
import io.github.vaadinadminstarter.platform.access.GrantPermissionUseCase;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import java.util.Comparator;

@Route(value = "roles", layout = MainLayout.class)
@PageTitle("角色")
public final class RolesView extends SecuredView {
    private static final PermissionCode GRANT = PermissionCode.of("system:role:grant");

    public RolesView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, PermissionCatalog catalog, GrantPermissionUseCase grants) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.RoleRow.class, false);
        grid.addColumn(AdministrationQueryService.RoleRow::code).setHeader("角色代码").setAutoWidth(true);
        grid.addColumn(AdministrationQueryService.RoleRow::permissionCount).setHeader("权限数量");
        grid.addComponentColumn(this::detailsAction).setHeader("操作").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        var pages = new PagedGrid<>(grid, queries::roles, "code");

        var grantDialog = grantDialog(queries, catalog, grants, pages, new OperationFeedback());
        var grant = new Button("授予权限", event -> grantDialog.open());
        grant.setVisible(authorization.hasPermission(requireCurrentUser(), GRANT));

        var header = new PageHeader("角色", "查看角色并授予已登记的系统权限。");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "roles-toolbar");
        toolbar.setPrimaryAction(grant);
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "roles-workspace");

        add(header, toolbar, workspace);
        expand(workspace);
    }

    private Button detailsAction(AdministrationQueryService.RoleRow role) {
        var details = new Button(VaadinIcon.EYE.create(), event -> showDetails(role));
        details.setTooltipText("查看角色详情");
        details.setAriaLabel("查看角色详情：" + role.code());
        return details;
    }

    private void showDetails(AdministrationQueryService.RoleRow role) {
        var dialog = new DetailDialog("角色详情");
        dialog.getCloseAction().setText("关闭");
        dialog.addField("角色代码", role.code());
        dialog.addField("权限数量", Long.toString(role.permissionCount()));
        dialog.addField("已授予权限", String.join(", ", role.permissionCodes()));
        dialog.open();
    }

    @Override
    PermissionCode requiredPermission() {
        return PermissionCode.of("system:role:read");
    }

    private EditorDialog grantDialog(AdministrationQueryService queries, PermissionCatalog catalog,
                                     GrantPermissionUseCase grants,
                                     PagedGrid<AdministrationQueryService.RoleRow> pages, OperationFeedback feedback) {
        var role = new ComboBox<String>("角色");
        role.setItems(queries.roles().stream().map(AdministrationQueryService.RoleRow::code).toList());
        var permission = new ComboBox<PermissionCode>("权限");
        permission.setItems(catalog.all().stream().sorted(Comparator.comparing(PermissionCode::value)).toList());
        permission.setItemLabelGenerator(PermissionCode::value);
        var dialog = new EditorDialog("授予权限", "保存授权", () -> { });
        dialog.getCancelAction().setText("取消");
        dialog.getPrimaryAction().setEnabled(false);
        dialog.getPrimaryAction().addClickListener(event -> {
            grants.grant(requireCurrentUser(), new GrantPermissionCommand(role.getValue(), permission.getValue()));
            dialog.close();
            pages.refresh();
            feedback.success("权限已授予。");
        });
        role.addValueChangeListener(event -> updateGrantAvailability(dialog, role, permission));
        permission.addValueChangeListener(event -> updateGrantAvailability(dialog, role, permission));
        dialog.addField(role, permission);
        return dialog;
    }

    private void updateGrantAvailability(EditorDialog dialog, ComboBox<String> role,
                                         ComboBox<PermissionCode> permission) {
        dialog.getPrimaryAction().setEnabled(role.getValue() != null && permission.getValue() != null);
    }
}
