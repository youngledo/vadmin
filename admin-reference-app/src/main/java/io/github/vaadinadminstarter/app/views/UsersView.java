package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.app.administration.UserAdministrationService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.ConfirmationDialog;
import io.github.vaadinadminstarter.flow.patterns.DetailDialog;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.OperationFeedback;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import jakarta.annotation.security.PermitAll;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("用户")
@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class UsersView extends PermissionProtectedView {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:user:read");
    private static final PermissionCode CREATE = PermissionCode.of("system:user:create");
    private static final PermissionCode UPDATE = PermissionCode.of("system:user:update");

    private final UserAdministrationService commands;
    private final Grid<AdministrationQueryService.UserRow> grid = new Grid<>();
    private final TextField filter = new TextField("搜索用户");
    private final PagedGrid<AdministrationQueryService.UserRow> pages;
    private final OperationFeedback feedback = new OperationFeedback();

    public UsersView(CurrentUserProvider currentUser, AuthorizationService authorization,
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
    protected PermissionCode requiredPermission() {
        return REQUIRED_PERMISSION;
    }

    private HorizontalLayout action(AdministrationQueryService.UserRow user, AuthorizationService authorization) {
        var details = new Button(VaadinIcon.EYE.create(), event -> showDetails(user));
        details.setTooltipText("查看用户详情");
        details.setAriaLabel("查看用户详情：" + user.username());
        var actionLabel = user.enabled() ? "停用用户" : "启用用户";
        var enabled = new Button(user.enabled() ? VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create(),
                event -> confirmStatusChange(Set.of(user.id()), user.enabled()));
        enabled.setTooltipText(actionLabel);
        enabled.setAriaLabel(actionLabel + "：" + user.username());
        enabled.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        var actions = new HorizontalLayout(details, enabled);
        actions.setPadding(false);
        actions.setSpacing(true);
        return actions;
    }

    private Button bulkAction(VaadinIcon icon, String label, boolean enabled, AuthorizationService authorization) {
        var action = new Button(icon.create(), event -> confirmStatusChange(grid.getSelectedItems().stream()
                .map(AdministrationQueryService.UserRow::id)
                .collect(Collectors.toSet()), !enabled));
        action.setTooltipText(label);
        action.setAriaLabel(label);
        action.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        return action;
    }

    private void showDetails(AdministrationQueryService.UserRow user) {
        var dialog = new DetailDialog("用户详情");
        dialog.getCloseAction().setText("关闭");
        dialog.addField("用户名", user.username());
        dialog.addField("状态", user.enabled() ? "启用" : "停用");
        dialog.addField("认证版本", Long.toString(user.authVersion()));
        dialog.open();
    }

    private void confirmStatusChange(Set<java.util.UUID> userIds, boolean currentlyEnabled) {
        var enabling = !currentlyEnabled;
        var title = enabling ? "启用用户" : "停用用户";
        var consequence = enabling ? "该用户将恢复登录权限。" : "该用户将无法登录。";
        var confirmation = new ConfirmationDialog(title, consequence, enabling ? "启用" : "停用", () -> {
            commands.setEnabled(requireCurrentUser(), userIds, enabling);
            grid.deselectAll();
            pages.refresh();
            feedback.success(enabling ? "用户已启用。" : "用户已停用。");
        });
        confirmation.getCancelAction().setText("取消");
        confirmation.open();
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
