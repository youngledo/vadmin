package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.app.administration.UserAdministrationService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("用户")
public final class UsersView extends SecuredView {
    private final AdministrationQueryService queries;
    private final UserAdministrationService commands;
    private final Grid<AdministrationQueryService.UserRow> grid = new Grid<>();
    private final TextField filter = new TextField("搜索用户");
    private final PagedGrid<AdministrationQueryService.UserRow> pages;

    public UsersView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries, UserAdministrationService commands) {
        super(currentUser, authorization);
        this.queries = queries;
        this.commands = commands;
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        grid.addColumn(AdministrationQueryService.UserRow::username).setHeader("用户名").setAutoWidth(true);
        grid.addColumn(user -> user.enabled() ? "启用" : "停用").setHeader("状态");
        grid.addColumn(AdministrationQueryService.UserRow::authVersion).setHeader("认证版本");
        grid.addComponentColumn(user -> action(user, authorization)).setHeader("操作");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::users, () -> Map.of("q", filter.getValue()), "username");
        filter.addValueChangeListener(event -> pages.refresh());
        var enableSelected = bulkAction(VaadinIcon.PLAY, "启用所选用户", true, authorization);
        var disableSelected = bulkAction(VaadinIcon.PAUSE, "停用所选用户", false, authorization);
        grid.addSelectionListener(event -> {
            var enabled = !event.getAllSelectedItems().isEmpty();
            enableSelected.setEnabled(enabled);
            disableSelected.setEnabled(enabled);
        });
        add(new H1("用户"), filter, enableSelected, disableSelected, grid);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:user:read"); }

    private Button action(AdministrationQueryService.UserRow user, AuthorizationService authorization) {
        var enabled = new Button(user.enabled() ? VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create(), event -> {
            commands.setEnabled(requireCurrentUser(), user.id(), !user.enabled());
            pages.refresh();
        });
        enabled.setTooltipText(user.enabled() ? "停用用户" : "启用用户");
        enabled.setVisible(authorization.hasPermission(requireCurrentUser(), PermissionCode.of("system:user:update")));
        return enabled;
    }

    private Button bulkAction(VaadinIcon icon, String tooltip, boolean enabled, AuthorizationService authorization) {
        var action = new Button(icon.create(), event -> {
            commands.setEnabled(requireCurrentUser(), grid.getSelectedItems().stream()
                    .map(AdministrationQueryService.UserRow::id)
                    .collect(Collectors.toSet()), enabled);
            grid.deselectAll();
            pages.refresh();
        });
        action.setTooltipText(tooltip);
        action.setEnabled(false);
        action.setVisible(authorization.hasPermission(requireCurrentUser(), PermissionCode.of("system:user:update")));
        return action;
    }

}
