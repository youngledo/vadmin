package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.app.administration.UserAdministrationService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("用户")
public final class UsersView extends SecuredView {
    private static final PermissionCode CREATE = PermissionCode.of("system:user:create");
    private static final PermissionCode UPDATE = PermissionCode.of("system:user:update");

    private final UserAdministrationService commands;
    private final Grid<AdministrationQueryService.UserRow> grid = new Grid<>();
    private final TextField filter = new TextField("搜索用户");
    private final PagedGrid<AdministrationQueryService.UserRow> pages;

    public UsersView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, UserAdministrationService commands) {
        super(currentUser, authorization);
        this.commands = commands;
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);

        grid.addColumn(AdministrationQueryService.UserRow::username).setHeader("用户名").setAutoWidth(true);
        grid.addColumn(user -> user.enabled() ? "启用" : "停用").setHeader("状态");
        grid.addColumn(AdministrationQueryService.UserRow::authVersion).setHeader("认证版本");
        grid.addComponentColumn(user -> action(user, authorization)).setHeader("操作");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::users, () -> Map.of("q", filter.getValue()), "username");
        filter.addValueChangeListener(event -> pages.refresh());

        var header = new PageHeader("用户", "管理可登录账户及其启用状态。");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "users-toolbar");
        toolbar.addFilter(filter);
        var create = new Button("新增用户", VaadinIcon.PLUS.create(), event -> createUser());
        create.setVisible(authorization.hasPermission(requireCurrentUser(), CREATE));
        toolbar.setPrimaryAction(create);

        var workspace = new DataWorkspace<>(grid);
        workspace.getElement().setAttribute("data-testid", "users-workspace");
        var enableSelected = bulkAction(VaadinIcon.PLAY, "启用所选用户", true, authorization);
        var disableSelected = bulkAction(VaadinIcon.PAUSE, "停用所选用户", false, authorization);
        var canUpdate = authorization.hasPermission(requireCurrentUser(), UPDATE);
        workspace.addBulkAction(enableSelected, () -> canUpdate);
        workspace.addBulkAction(disableSelected, () -> canUpdate);

        add(header, toolbar, workspace);
        expand(workspace);
    }

    @Override
    PermissionCode requiredPermission() {
        return PermissionCode.of("system:user:read");
    }

    private Button action(AdministrationQueryService.UserRow user, AuthorizationService authorization) {
        var enabled = new Button(user.enabled() ? VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create(), event -> {
            commands.setEnabled(requireCurrentUser(), user.id(), !user.enabled());
            pages.refresh();
        });
        enabled.setTooltipText(user.enabled() ? "停用用户" : "启用用户");
        enabled.setAriaLabel(user.enabled() ? "停用用户" : "启用用户");
        enabled.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        return enabled;
    }

    private Button bulkAction(VaadinIcon icon, String label, boolean enabled, AuthorizationService authorization) {
        var action = new Button(icon.create(), event -> {
            commands.setEnabled(requireCurrentUser(), grid.getSelectedItems().stream()
                    .map(AdministrationQueryService.UserRow::id)
                    .collect(Collectors.toSet()), enabled);
            grid.deselectAll();
            pages.refresh();
        });
        action.setTooltipText(label);
        action.setAriaLabel(label);
        action.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        return action;
    }

    private void createUser() {
        var username = new TextField("用户名");
        username.setRequired(true);
        var password = new PasswordField("初始密码");
        password.setRequired(true);
        var dialog = new EditorDialog("新增用户", "保存", () -> { });
        dialog.getPrimaryAction().addClickListener(event -> {
            if (username.getValue().isBlank() || password.getValue().isBlank()) {
                dialog.showValidationMessage("用户名和初始密码均为必填项。");
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
        dialog.getCancelAction().setText("取消");
        dialog.addField(username, password);
        dialog.open();
    }

    private String userValidationMessage(BusinessFailure failure) {
        if ("already-exists".equals(failure.fieldErrors().get("username"))) {
            return "用户名已存在。";
        }
        return "用户名和初始密码均为必填项。";
    }
}
