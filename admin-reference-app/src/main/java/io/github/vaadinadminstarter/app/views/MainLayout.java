package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.example.orders.admin.OrdersView;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.flow.navigation.AdminIconCatalog;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;
import io.github.vaadinadminstarter.springflow.i18n.AdminLocalePreference;
import jakarta.annotation.security.PermitAll;
import java.util.Locale;

@Layout
@PermitAll
@Uses(UsersView.class)
@Uses(RolesView.class)
@Uses(PermissionsView.class)
@Uses(AuditView.class)
@Uses(CustomersView.class)
@Uses(OrdersView.class)
public final class MainLayout extends AppLayout implements AfterNavigationObserver, LocaleChangeObserver {
    private static final String THEME_MODE_KEY = MainLayout.class.getName() + ".theme-mode";

    private final AdminModuleRegistry modules;
    private final CurrentUser user;
    private final AuthorizationService authorization;
    private final AdminLocalePreference localePreference;
    private final I18NProvider translations;
    private final Span currentLocation = new Span();
    private final MenuItem themeModeItem;
    private final VerticalLayout drawer = new VerticalLayout();
    private final DrawerToggle toggle = new DrawerToggle();
    private final MenuBar userMenu;
    private final Select<Locale> languageSelector;
    private final boolean authenticated;
    private String currentRoute = "";

    public MainLayout(AdminModuleRegistry modules, CurrentUserProvider currentUser,
                      AuthorizationService authorization, AdminLocalePreference localePreference,
                      I18NProvider translations) {
        this.modules = modules;
        this.authorization = authorization;
        this.localePreference = localePreference;
        this.translations = translations;
        var productMark = new Span(VaadinIcon.CUBE.create());
        productMark.addClassName("admin-product-mark");
        var productName = new Span("Vaadin Admin Starter");
        productName.addClassName("admin-shell-brand");
        currentLocation.addClassName("admin-shell-location");

        var currentUserValue = currentUser.currentUser();
        authenticated = currentUserValue.isPresent();
        if (!authenticated) {
            user = null;
            themeModeItem = null;
            userMenu = null;
            languageSelector = null;
            addHeader(productMark, productName);
            return;
        }

        user = currentUserValue.orElseThrow();
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        userMenu = createUserMenu(user.username());
        themeModeItem = userMenu.getItems().getFirst().getSubMenu().addItem("", event -> toggleThemeMode());
        languageSelector = createLanguageSelector();
        updateHeaderText();
        addHeader(toggle, productMark, productName, currentLocation, languageSelector, userMenu);

        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.addClassName("admin-drawer-content");
        rebuildDrawer();
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
        if (!authenticated) return;
        currentRoute = event.getLocation().getPath();
        updateCurrentLocation();
        getContent().addClassName("admin-content-canvas");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyTheme(sessionThemeMode());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        if (!authenticated) return;
        updateHeaderText();
        rebuildDrawer();
        updateCurrentLocation();
    }

    private MenuBar createUserMenu(String username) {
        var menu = new MenuBar();
        menu.setOpenOnHover(false);
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

    private Select<Locale> createLanguageSelector() {
        var selector = new Select<Locale>();
        selector.setItems(translations.getProvidedLocales());
        selector.setValue(UI.getCurrent().getLocale());
        selector.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                localePreference.select(UI.getCurrent(), event.getValue());
            }
        });
        selector.setVisible(translations.getProvidedLocales().size() > 1);
        selector.addClassName("admin-language-control");
        return selector;
    }

    private void rebuildDrawer() {
        drawer.removeAll();
        drawer.add(navigationGroup(text("system.shell.workspace"),
                new SideNavItem(text("system.shell.home"), "", VaadinIcon.HOME.create())));
        var visiblePages = modules.pagesVisibleTo(user, authorization);
        modules.groupsVisibleTo(user, authorization).forEach(group -> addNavigationGroup(drawer, text(group.titleKey()),
                visiblePages.stream().filter(page -> page.groupId().equals(group.id())).toList()));
    }

    private void addNavigationGroup(VerticalLayout target, String label, java.util.List<AdminPage> pages) {
        if (!pages.isEmpty()) target.add(navigationGroup(label, pages.stream()
                .map(page -> new SideNavItem(titleFor(page), page.route(), AdminIconCatalog.create(page.iconKey())))
                .toArray(SideNavItem[]::new)));
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

    private void applyTheme(String themeMode) { UI.getCurrent().getElement().getThemeList().set("dark", "dark".equals(themeMode)); }

    private void updateHeaderText() {
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        userMenu.getElement().setAttribute("aria-label", text("system.shell.current-user"));
        languageSelector.setLabel(text("system.shell.language"));
        languageSelector.setItemLabelGenerator(locale -> text("system.shell.language." + locale.toLanguageTag()));
        languageSelector.setValue(UI.getCurrent().getLocale());
        updateThemeModeItem();
    }

    private void updateCurrentLocation() {
        currentLocation.setText(modules.pages().stream().filter(page -> page.route().equals(currentRoute)).findFirst()
                .map(this::titleFor).orElse(text("system.shell.home")));
    }

    private void updateThemeModeItem() {
        themeModeItem.setText(text(sessionThemeMode().equals("dark") ? "system.shell.theme-light" : "system.shell.theme-dark"));
        themeModeItem.setAriaLabel(text("system.shell.theme-toggle"));
    }

    private String titleFor(AdminPage page) { return text(page.titleKey()); }
    private String text(String key, Object... parameters) { return getTranslation(key, parameters); }
}
