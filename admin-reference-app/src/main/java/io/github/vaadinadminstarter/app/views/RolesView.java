package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
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
import io.github.vaadinadminstarter.flow.navigation.AdminIcon;
import io.github.vaadinadminstarter.flow.navigation.AdminIconName;
import jakarta.annotation.security.PermitAll;
import java.util.Comparator;

@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RolesView extends PermissionProtectedView implements LocaleChangeObserver, HasDynamicTitle {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:role:read");
    private static final PermissionCode GRANT = PermissionCode.of("system:role:grant");
    private final Grid<AdministrationQueryService.RoleRow> grid = new Grid<>(AdministrationQueryService.RoleRow.class, false);
    private final Grid.Column<AdministrationQueryService.RoleRow> codeColumn;
    private final Grid.Column<AdministrationQueryService.RoleRow> permissionCountColumn;
    private final Grid.Column<AdministrationQueryService.RoleRow> actionsColumn;
    private final PagedGrid<AdministrationQueryService.RoleRow> pages;
    private final Button grantAction;

    public RolesView(CurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, PermissionCatalog catalog, GrantPermissionUseCase grants) {
        super(currentUser, authorization);
        codeColumn = grid.addColumn(AdministrationQueryService.RoleRow::code).setAutoWidth(true);
        permissionCountColumn = grid.addColumn(AdministrationQueryService.RoleRow::permissionCount);
        actionsColumn = grid.addComponentColumn(this::detailsAction).setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::roles, "code");

        grantAction = new Button();
        grantAction.addClickListener(event -> grantDialog(queries, catalog, grants, pages, new OperationFeedback()).open());
        grantAction.setVisible(authorization.hasPermission(requireCurrentUser(), GRANT));

        var header = PageHeader.translated("system.roles.title", "system.roles.intent");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "roles-toolbar");
        toolbar.setPrimaryAction(grantAction);
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "roles-workspace");

        var frame = new AdminPageFrame(header, toolbar, workspace);
        add(frame);
        expand(frame);
        updateText();
    }

    private Button detailsAction(AdministrationQueryService.RoleRow role) {
        var details = new Button(AdminIcon.of(AdminIconName.EYE), event -> showDetails(role));
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

    @Override public void localeChange(LocaleChangeEvent event) { updateText(); pages.refresh(); updateBrowserTitle(); }

    @Override public String getPageTitle() { return getTranslation("system.roles.title"); }

    private void updateText() {
        codeColumn.setHeader(getTranslation("system.roles.code"));
        permissionCountColumn.setHeader(getTranslation("system.roles.permission-count"));
        actionsColumn.setHeader(getTranslation("system.roles.actions"));
        grantAction.setText(getTranslation("system.roles.grant"));
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
