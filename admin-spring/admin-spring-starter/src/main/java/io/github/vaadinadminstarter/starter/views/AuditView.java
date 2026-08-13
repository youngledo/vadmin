package io.github.vaadinadminstarter.starter.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import io.github.vaadinadminstarter.starter.administration.AdministrationQueryService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import jakarta.annotation.security.PermitAll;

@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class AuditView extends PermissionProtectedView implements LocaleChangeObserver, HasDynamicTitle {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:audit:read");
    private final Grid<AdministrationQueryService.AuditRow> grid = new Grid<>(AdministrationQueryService.AuditRow.class, false);
    private final Grid.Column<AdministrationQueryService.AuditRow> timeColumn;
    private final Grid.Column<AdministrationQueryService.AuditRow> actionColumn;
    private final Grid.Column<AdministrationQueryService.AuditRow> targetTypeColumn;
    private final Grid.Column<AdministrationQueryService.AuditRow> targetIdColumn;
    private final Grid.Column<AdministrationQueryService.AuditRow> outcomeColumn;
    private final PagedGrid<AdministrationQueryService.AuditRow> pages;

    public AuditView(CurrentUserProvider currentUser, AuthorizationService authorization,
                     AdministrationQueryService queries) {
        super(currentUser, authorization);
        timeColumn = grid.addColumn(AdministrationQueryService.AuditRow::occurredAt).setAutoWidth(true);
        actionColumn = grid.addColumn(AdministrationQueryService.AuditRow::action);
        targetTypeColumn = grid.addColumn(AdministrationQueryService.AuditRow::targetType);
        targetIdColumn = grid.addColumn(AdministrationQueryService.AuditRow::targetId);
        outcomeColumn = grid.addColumn(AdministrationQueryService.AuditRow::outcome);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::audit, "occurred_at");
        var header = PageHeader.translated("system.audit.title", "system.audit.intent");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "read-only-workspace");
        workspace.setFooter(pages.getPaginationBar());
        var frame = new AdminPageFrame(header, null, workspace);
        add(frame);
        expand(frame);
        updateText();
    }

    @Override protected PermissionCode requiredPermission() { return REQUIRED_PERMISSION; }

    @Override public void localeChange(LocaleChangeEvent event) { updateText(); pages.refresh(); updateBrowserTitle(); }

    @Override public String getPageTitle() { return getTranslation("system.audit.title"); }

    private void updateText() {
        timeColumn.setHeader(getTranslation("system.audit.time"));
        actionColumn.setHeader(getTranslation("system.audit.action"));
        targetTypeColumn.setHeader(getTranslation("system.audit.target-type"));
        targetIdColumn.setHeader(getTranslation("system.audit.target-id"));
        outcomeColumn.setHeader(getTranslation("system.audit.outcome"));
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
