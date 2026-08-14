package io.github.vaadinadminstarter.starter.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import io.github.vaadinadminstarter.starter.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.starter.administration.UserAdministrationService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.ConfirmationDialog;
import io.github.vaadinadminstarter.flow.patterns.DetailDialog;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.OperationFeedback;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import io.github.vaadinadminstarter.flow.navigation.AdminIcon;
import io.github.vaadinadminstarter.flow.navigation.AdminIconName;
import jakarta.annotation.security.PermitAll;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class UsersView extends PermissionProtectedView implements LocaleChangeObserver, HasDynamicTitle {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:user:read");
    private static final PermissionCode CREATE = PermissionCode.of("system:user:create");
    private static final PermissionCode UPDATE = PermissionCode.of("system:user:update");

    private final UserAdministrationService commands;
    private final Grid<AdministrationQueryService.UserRow> grid = new Grid<>();
    private final TextField filter = new TextField();
    private final PagedGrid<AdministrationQueryService.UserRow> pages;
    private final OperationFeedback feedback = new OperationFeedback();
    private final Grid.Column<AdministrationQueryService.UserRow> usernameColumn;
    private final Grid.Column<AdministrationQueryService.UserRow> statusColumn;
    private final Grid.Column<AdministrationQueryService.UserRow> authVersionColumn;
    private final Grid.Column<AdministrationQueryService.UserRow> actionsColumn;
    private final Button createAction;
    private final Button enableSelectedAction;
    private final Button disableSelectedAction;

    public UsersView(CurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, UserAdministrationService commands) {
        super(currentUser, authorization);
        this.commands = commands;
        filter.setLabel(getTranslation("system.users.filter"));
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);

        usernameColumn = grid.addColumn(AdministrationQueryService.UserRow::username).setAutoWidth(true);
        statusColumn = grid.addColumn(user -> user.enabled() ? getTranslation("system.users.enabled") : getTranslation("system.users.disabled"));
        authVersionColumn = grid.addColumn(AdministrationQueryService.UserRow::authVersion);
        actionsColumn = grid.addComponentColumn(user -> action(user, authorization));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::users, () -> Map.of("q", filter.getValue()), "username");
        filter.addValueChangeListener(event -> pages.refresh());

        var header = PageHeader.translated("system.users.title", "system.users.intent");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "users-toolbar");
        toolbar.addFilter(filter);
        createAction = new Button(AdminIcon.of(AdminIconName.ADD), event -> createUser());
        createAction.setVisible(authorization.hasPermission(requireCurrentUser(), CREATE));
        toolbar.setPrimaryAction(createAction);

        var workspace = new DataWorkspace<>(grid);
        workspace.getElement().setAttribute("data-testid", "users-workspace");
        workspace.setFooter(pages.getPaginationBar());
        enableSelectedAction = bulkAction(AdminIconName.PLAY, true, authorization);
        disableSelectedAction = bulkAction(AdminIconName.PAUSE, false, authorization);
        var canUpdate = authorization.hasPermission(requireCurrentUser(), UPDATE);
        workspace.addBulkAction(enableSelectedAction, () -> canUpdate);
        workspace.addBulkAction(disableSelectedAction, () -> canUpdate);

        var frame = new AdminPageFrame(header, toolbar, workspace);
        add(frame);
        expand(frame);
        updateText();
    }

    @Override
    protected PermissionCode requiredPermission() {
        return REQUIRED_PERMISSION;
    }

    private HorizontalLayout action(AdministrationQueryService.UserRow user, AuthorizationService authorization) {
        var details = new Button(AdminIcon.of(AdminIconName.EYE), event -> showDetails(user));
        details.setTooltipText(getTranslation("system.users.details"));
        details.setAriaLabel(getTranslation("system.users.details-aria", user.username()));
        var actionLabel = getTranslation(user.enabled() ? "system.users.disable" : "system.users.enable");
        var enabled = new Button(AdminIcon.of(user.enabled() ? AdminIconName.PAUSE : AdminIconName.PLAY),
                event -> confirmStatusChange(Set.of(user.id()), user.enabled()));
        enabled.setTooltipText(actionLabel);
        enabled.setAriaLabel(getTranslation(user.enabled() ? "system.users.disable-aria" : "system.users.enable-aria", user.username()));
        enabled.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        var actions = new HorizontalLayout(details, enabled);
        actions.setPadding(false);
        actions.setSpacing(true);
        return actions;
    }

    private Button bulkAction(AdminIconName icon, boolean enabled, AuthorizationService authorization) {
        var action = new Button(AdminIcon.of(icon), event -> confirmStatusChange(grid.getSelectedItems().stream()
                .map(AdministrationQueryService.UserRow::id)
                .collect(Collectors.toSet()), !enabled));
        action.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        return action;
    }

    private void showDetails(AdministrationQueryService.UserRow user) {
        var dialog = DetailDialog.translated("system.users.details-title");
        dialog.addField(getTranslation("system.users.username"), user.username());
        dialog.addField(getTranslation("system.users.status"), getTranslation(user.enabled() ? "system.users.enabled" : "system.users.disabled"));
        dialog.addField(getTranslation("system.users.auth-version"), Long.toString(user.authVersion()));
        dialog.open();
    }

    private void confirmStatusChange(Set<java.util.UUID> userIds, boolean currentlyEnabled) {
        var enabling = !currentlyEnabled;
        var confirmation = ConfirmationDialog.translated(enabling ? "system.users.enable-title" : "system.users.disable-title",
                enabling ? "system.users.enable-consequence" : "system.users.disable-consequence",
                enabling ? "system.users.enabled" : "system.users.disabled", () -> {
            commands.setEnabled(requireCurrentUser(), userIds, enabling);
            grid.deselectAll();
            pages.refresh();
            feedback.success(getTranslation(enabling ? "system.users.enabled-success" : "system.users.disabled-success"));
        });
        confirmation.open();
    }

    private void createUser() {
        var username = new TextField(getTranslation("system.users.username"));
        username.setRequired(true);
        var password = new PasswordField(getTranslation("system.users.initial-password"));
        password.setRequired(true);
        var dialog = EditorDialog.translated("system.users.create", "system.users.save", () -> { });
        dialog.getPrimaryAction().addClickListener(event -> {
            if (username.getValue().isBlank() || password.getValue().isBlank()) {
                dialog.showValidationMessage(getTranslation("system.users.required"));
                return;
            }
            try {
                commands.create(requireCurrentUser(), username.getValue(), password.getValue());
                dialog.close();
                pages.refresh();
            } catch (BusinessFailure failure) {
                ViewBusinessFailureHandler.handle(failure,
                        validationFailure -> dialog.showValidationMessage(userValidationMessage(validationFailure)));
            }
        });
        dialog.addField(username, password);
        dialog.open();
    }

    private String userValidationMessage(BusinessFailure failure) {
        if ("already-exists".equals(failure.fieldErrors().get("username"))) {
            return getTranslation("system.users.already-exists");
        }
        return getTranslation("system.users.required");
    }

    @Override public void localeChange(LocaleChangeEvent event) { updateText(); pages.refresh(); updateBrowserTitle(); }

    @Override public String getPageTitle() { return getTranslation("system.users.title"); }

    private void updateText() {
        filter.setLabel(getTranslation("system.users.filter"));
        usernameColumn.setHeader(getTranslation("system.users.username"));
        statusColumn.setHeader(getTranslation("system.users.status"));
        authVersionColumn.setHeader(getTranslation("system.users.auth-version"));
        actionsColumn.setHeader(getTranslation("system.users.actions"));
        createAction.setText(getTranslation("system.users.create"));
        setActionText(enableSelectedAction, "system.users.enable-selected");
        setActionText(disableSelectedAction, "system.users.disable-selected");
    }

    private void setActionText(Button action, String key) {
        var label = getTranslation(key);
        action.setTooltipText(label);
        action.setAriaLabel(label);
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
