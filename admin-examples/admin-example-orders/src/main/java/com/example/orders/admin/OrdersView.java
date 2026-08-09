package com.example.orders.admin;

import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.DetailDialog;
import io.github.vaadinadminstarter.flow.patterns.OperationFeedback;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;

/** Read-only orders worklist supplied by the external example module. */
public final class OrdersView extends PermissionProtectedView implements LocaleChangeObserver, HasDynamicTitle {
    private final Grid<OrderRow> grid = new Grid<>(OrderRow.class, false);
    private final PagedGrid<OrderRow> pages;
    private final OperationFeedback feedback = new OperationFeedback();
    private final Grid.Column<OrderRow> numberColumn;
    private final Grid.Column<OrderRow> customerColumn;
    private final Grid.Column<OrderRow> statusColumn;
    private final Grid.Column<OrderRow> totalColumn;
    private final Grid.Column<OrderRow> placedOnColumn;
    private final Grid.Column<OrderRow> actionsColumn;

    public OrdersView(CurrentUserProvider currentUser, AuthorizationService authorization,
                      OrderQueryService orders) {
        super(currentUser, authorization);
        numberColumn = grid.addColumn(OrderRow::number).setAutoWidth(true);
        customerColumn = grid.addColumn(OrderRow::customer).setAutoWidth(true);
        statusColumn = grid.addColumn(OrderRow::status);
        totalColumn = grid.addColumn(order -> NumberFormat.getCurrencyInstance(locale()).format(order.total()));
        placedOnColumn = grid.addColumn(OrderRow::placedOn);
        actionsColumn = grid.addComponentColumn(this::actions).setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, orders::orders, "number");

        var header = PageHeader.translated("orders.title", "orders.intent");
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "orders-workspace");
        var frame = new AdminPageFrame(header, null, workspace);
        add(frame);
        expand(frame);
        updateText();
    }

    @Override
    protected PermissionCode requiredPermission() {
        return OrdersAdminModule.ORDERS_READ;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateText();
        pages.refresh();
        updateBrowserTitle();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("orders.title");
    }

    private HorizontalLayout actions(OrderRow order) {
        var details = new Button(VaadinIcon.EYE.create(), event -> showDetails(order));
        details.setTooltipText(getTranslation("orders.details"));
        details.setAriaLabel(getTranslation("orders.details-aria", order.number()));
        var actions = new HorizontalLayout(details);
        actions.setPadding(false);
        actions.setSpacing(true);
        return actions;
    }

    private void showDetails(OrderRow order) {
        var dialog = DetailDialog.translated("orders.details-title");
        dialog.addField(getTranslation("orders.number"), order.number());
        dialog.addField(getTranslation("orders.customer"), order.customer());
        dialog.addField(getTranslation("orders.status"), order.status());
        dialog.addField(getTranslation("orders.total"), NumberFormat.getCurrencyInstance(locale()).format(order.total()));
        dialog.addField(getTranslation("orders.placed-on"), order.placedOn().toString());
        dialog.open();
        feedback.success(getTranslation("orders.details-opened"));
    }

    private void updateText() {
        numberColumn.setHeader(getTranslation("orders.number"));
        customerColumn.setHeader(getTranslation("orders.customer"));
        statusColumn.setHeader(getTranslation("orders.status"));
        totalColumn.setHeader(getTranslation("orders.total"));
        placedOnColumn.setHeader(getTranslation("orders.placed-on"));
        actionsColumn.setHeader(getTranslation("orders.actions"));
    }

    private Locale locale() {
        return getUI().map(ui -> ui.getLocale()).orElse(Locale.SIMPLIFIED_CHINESE);
    }

    private void updateBrowserTitle() {
        getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle()));
    }
}
