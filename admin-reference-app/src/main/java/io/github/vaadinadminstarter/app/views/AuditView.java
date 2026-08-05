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

@Route(value = "audit", layout = MainLayout.class)
@PageTitle("审计日志")
public final class AuditView extends SecuredView {
    public AuditView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries) {
        super(currentUser, authorization);
        var grid = new Grid<>(AdministrationQueryService.AuditRow.class, false);
        grid.addColumn(AdministrationQueryService.AuditRow::occurredAt).setHeader("时间").setAutoWidth(true);
        grid.addColumn(AdministrationQueryService.AuditRow::action).setHeader("操作");
        grid.addColumn(AdministrationQueryService.AuditRow::targetType).setHeader("对象类型");
        grid.addColumn(AdministrationQueryService.AuditRow::targetId).setHeader("对象 ID");
        grid.addColumn(AdministrationQueryService.AuditRow::outcome).setHeader("结果");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::audit, "occurred_at");
        var header = new PageHeader("审计日志", "按时间查看已记录的安全与管理操作。");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "read-only-workspace");
        add(header, workspace);
        expand(workspace);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:audit:read"); }
}
