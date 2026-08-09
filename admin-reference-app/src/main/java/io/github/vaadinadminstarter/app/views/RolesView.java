package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
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
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import jakarta.annotation.security.PermitAll;
import java.util.Comparator;

@PageTitle("Roles")
@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RolesView extends PermissionProtectedView {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:role:read");
    private static final PermissionCode GRANT = PermissionCode.of("system:role:grant");

    public RolesView(CurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, PermissionCatalog catalog, GrantPermissionUseCase grants) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.RoleRow.class, false);
        grid.addColumn(AdministrationQueryService.RoleRow::code).setHeader(getTranslation("system.roles.code")).setAutoWidth(true);
        grid.addColumn(AdministrationQueryService.RoleRow::permissionCount).setHeader(getTranslation("system.roles.permission-count"));
        grid.addComponentColumn(this::detailsAction).setHeader(getTranslation("system.roles.actions")).setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        var pages = new PagedGrid<>(grid, queries::roles, "code");

        var grantDialog = grantDialog(queries, catalog, grants, pages, new OperationFeedback());
        var grant = new Button(getTranslation("system.roles.grant"), event -> grantDialog.open());
        grant.setVisible(authorization.hasPermission(requireCurrentUser(), GRANT));

        var header = PageHeader.translated("system.roles.title", "system.roles.intent");
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
        details.setTooltipText(getTranslation("system.roles.details"));
        details.setAriaLabel(getTranslation("system.roles.details-aria", role.code()));
        return details;
    }

    private void showDetails(AdministrationQueryService.RoleRow role) {
        var dialog = DetailDialog.translated("system.roles.details-title");
        dialog.addField(getTranslation("system.roles.code"), role.code());
        dialog.addField(getTranslation("system.roles.permission-count"), Long.toString(role.permissionCount()));
        dialog.addField(getTranslation("system.roles.granted-permissions"), String.join(", ", role.permissionCodes()));
        dialog.open();
    }

    @Override
    protected PermissionCode requiredPermission() {
        return REQUIRED_PERMISSION;
    }

    private EditorDialog grantDialog(AdministrationQueryService queries, PermissionCatalog catalog,
                                     GrantPermissionUseCase grants,
                                     PagedGrid<AdministrationQueryService.RoleRow> pages, OperationFeedback feedback) {
        var role = new ComboBox<String>(getTranslation("system.roles.role"));
        role.setItems(queries.roles().stream().map(AdministrationQueryService.RoleRow::code).toList());
        var permission = new ComboBox<PermissionCode>(getTranslation("system.roles.permission"));
        permission.setItems(catalog.all().stream().sorted(Comparator.comparing(PermissionCode::value)).toList());
        permission.setItemLabelGenerator(PermissionCode::value);
        var dialog = EditorDialog.translated("system.roles.grant", "system.roles.save-grant", () -> { });
        dialog.getPrimaryAction().setEnabled(false);
        dialog.getPrimaryAction().addClickListener(event -> {
            grants.grant(requireCurrentUser(), new GrantPermissionCommand(role.getValue(), permission.getValue()));
            dialog.close();
            pages.refresh();
            feedback.success(getTranslation("system.roles.granted-success"));
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
