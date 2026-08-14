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
public final class PermissionsView extends PermissionProtectedView implements LocaleChangeObserver, HasDynamicTitle {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("system:permission:read");
    private final Grid<AdministrationQueryService.PermissionRow> grid = new Grid<>(AdministrationQueryService.PermissionRow.class, false);
    private final Grid.Column<AdministrationQueryService.PermissionRow> codeColumn;
    private final Grid.Column<AdministrationQueryService.PermissionRow> sourceColumn;
    private final PagedGrid<AdministrationQueryService.PermissionRow> pages;

    public PermissionsView(CurrentUserProvider currentUser, AuthorizationService authorization,
                           AdministrationQueryService queries) {
        super(currentUser, authorization);
        codeColumn = grid.addColumn(AdministrationQueryService.PermissionRow::code).setAutoWidth(true);
        sourceColumn = grid.addColumn(permission -> permission.systemManaged() ? getTranslation("system.permissions.system-managed") : getTranslation("system.permissions.custom"));
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, queries::permissions, "code");
        var header = PageHeader.translated("system.permissions.title", "system.permissions.intent");
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

    @Override public String getPageTitle() { return getTranslation("system.permissions.title"); }

    private void updateText() {
        codeColumn.setHeader(getTranslation("system.permissions.code"));
        sourceColumn.setHeader(getTranslation("system.permissions.source"));
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
