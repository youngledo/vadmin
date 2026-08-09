package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import io.github.vaadinadminstarter.app.modules.ReferenceAdminModules;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.flow.navigation.AdminIconCatalog;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;
import jakarta.annotation.security.PermitAll;

@Layout
@PermitAll
public final class MainLayout extends AppLayout implements AfterNavigationObserver {
    private static final String THEME_MODE_KEY = MainLayout.class.getName() + ".theme-mode";

    private final AdminModuleRegistry modules;
    private final Span currentLocation = new Span("工作台");
    private final MenuItem themeModeItem;
    private final boolean authenticated;

    public MainLayout(AdminModuleRegistry modules, CurrentUserProvider currentUser, AuthorizationService authorization) {
        this.modules = modules;
        var productMark = new Span(VaadinIcon.CUBE.create());
        productMark.addClassName("admin-product-mark");
        var productName = new Span("Vaadin Admin Starter");
        productName.addClassName("admin-shell-brand");
        currentLocation.addClassName("admin-shell-location");

        var currentUserValue = currentUser.currentUser();
        authenticated = currentUserValue.isPresent();
        if (!authenticated) {
            themeModeItem = null;
            addHeader(productMark, productName);
            return;
        }

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
        var toggle = new DrawerToggle();
        toggle.setAriaLabel("切换导航");
        toggle.setTooltipText("切换导航");
        var user = currentUserValue.orElseThrow();
        var userMenu = createUserMenu(user.username());
        themeModeItem = userMenu.getItems().getFirst().getSubMenu().addItem("", event -> toggleThemeMode());
        updateThemeModeItem();

        addHeader(toggle, productMark, productName, currentLocation, userMenu);

        var drawer = new VerticalLayout();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.addClassName("admin-drawer-content");
        drawer.add(navigationGroup("工作空间", new SideNavItem("工作台", "", VaadinIcon.HOME.create())));
        var visiblePages = modules.pagesVisibleTo(user, authorization);
        modules.groupsVisibleTo(user, authorization).forEach(group -> addNavigationGroup(drawer,
                ReferenceAdminModules.legacyLabel(group.titleKey()), visiblePages.stream()
                        .filter(page -> page.groupId().equals(group.id()))
                        .toList()));
        addToDrawer(drawer);
    }

    private void addHeader(Component... components) {
        var header = new HorizontalLayout(components);
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.addClassName("admin-shell-header");
        addToNavbar(header);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (!authenticated) {
            return;
        }
        var route = event.getLocation().getPath();
        currentLocation.setText(modules.pages().stream()
                .filter(page -> page.route().equals(route))
                .findFirst()
                .map(this::titleFor)
                .orElse("工作台"));
        Component content = getContent();
        content.addClassName("admin-content-canvas");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyTheme(sessionThemeMode());
    }

    private MenuBar createUserMenu(String username) {
        var menu = new MenuBar();
        menu.setOpenOnHover(false);
        menu.getElement().setAttribute("aria-label", "当前用户菜单");
        menu.addClassName("admin-user-menu");
        var avatar = new Avatar(username);
        avatar.setAbbreviation(username.substring(0, 1).toUpperCase());
        avatar.getElement().setAttribute("title", username);
        var userControl = new HorizontalLayout(avatar, new Span(username));
        userControl.setPadding(false);
        userControl.setSpacing(true);
        userControl.setAlignItems(HorizontalLayout.Alignment.CENTER);
        menu.addItem(userControl);
        return menu;
    }

    private void addNavigationGroup(VerticalLayout drawer, String label, java.util.List<AdminPage> pages) {
        if (!pages.isEmpty()) {
            drawer.add(navigationGroup(label, pages.stream()
                    .map(page -> new SideNavItem(titleFor(page), page.route(), AdminIconCatalog.create(page.iconKey())))
                    .toArray(SideNavItem[]::new)));
        }
    }

    private Component[] navigationGroup(String label, SideNavItem... items) {
        var section = new Span(label);
        section.addClassName("admin-drawer-section");
        var navigation = new SideNav();
        navigation.getElement().setAttribute("aria-label", label);
        navigation.addClassName("admin-drawer-nav");
        navigation.addItem(items);
        return new Component[]{section, navigation};
    }

    private void toggleThemeMode() {
        var nextMode = sessionThemeMode().equals("dark") ? "light" : "dark";
        VaadinSession.getCurrent().setAttribute(THEME_MODE_KEY, nextMode);
        applyTheme(nextMode);
        updateThemeModeItem();
    }

    private String sessionThemeMode() {
        var mode = VaadinSession.getCurrent().getAttribute(THEME_MODE_KEY);
        return "dark".equals(mode) ? "dark" : "light";
    }

    private void applyTheme(String themeMode) {
        UI.getCurrent().getElement().getThemeList().set("dark", "dark".equals(themeMode));
    }

    private void updateThemeModeItem() {
        themeModeItem.setText(sessionThemeMode().equals("dark") ? "切换至浅色模式" : "切换至深色模式");
        themeModeItem.setAriaLabel("切换主题模式");
    }

    private String titleFor(AdminPage page) {
        return ReferenceAdminModules.legacyLabel(page.titleKey());
    }

}
