package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.flow.navigation.PageRegistry;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;

@Layout
public final class MainLayout extends AppLayout {
    public MainLayout(PageRegistry pages, SecurityContextCurrentUserProvider currentUser,
                      AuthorizationService authorization) {
        var header = new HorizontalLayout(new DrawerToggle(), new H2("Vaadin Admin Starter"));
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.getStyle().set("padding", "0 var(--lumo-space-m)");
        addToNavbar(header);

        var navigation = new SideNav();
        currentUser.currentUser().ifPresent(user -> pages.visibleTo(user, authorization)
                .forEach(page -> navigation.addItem(new SideNavItem(titleFor(page.pageId()), page.route()))));
        addToDrawer(navigation, new Span(currentUser.currentUser().map(user -> user.username()).orElse("")));
    }

    private String titleFor(String pageId) {
        return switch (pageId) {
            case "system-users" -> "用户";
            case "system-roles" -> "角色";
            case "system-permissions" -> "权限目录";
            case "system-audit" -> "审计日志";
            case "customers" -> "客户";
            default -> pageId;
        };
    }
}
