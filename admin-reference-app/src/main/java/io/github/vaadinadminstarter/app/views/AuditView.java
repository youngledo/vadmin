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
        grid.setSizeFull();
        new PagedGrid<>(grid, queries::audit, "occurred_at");
        add(new H1("审计日志"), grid);
    }

    @Override PermissionCode requiredPermission() { return PermissionCode.of("system:audit:read"); }
}
