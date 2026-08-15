package io.github.youngledo.vadmin.starter.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.flow.navigation.AdminIconCatalog;
import io.github.youngledo.vadmin.flow.navigation.AdminIcon;
import io.github.youngledo.vadmin.flow.navigation.AdminIconName;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;
import io.github.youngledo.vadmin.springflow.i18n.AdminLocalePreference;
import jakarta.annotation.security.PermitAll;
import java.util.Locale;

@Layout
@PermitAll
@Uses(UsersView.class)
@Uses(RolesView.class)
@Uses(PermissionsView.class)
@Uses(AuditView.class)
public final class DefaultMainLayout extends AppLayout implements AfterNavigationObserver, LocaleChangeObserver {
    private static final String COLOR_SCHEME_KEY = DefaultMainLayout.class.getName() + ".color-scheme";

    private final AdminModuleRegistry modules;
    private final CurrentUser user;
    private final AuthorizationService authorization;
    private final AdminLocalePreference localePreference;
    private final I18NProvider translations;
    private final AdminAppearanceProperties appearance;
    private final Span currentLocation = new Span();
    private final VerticalLayout drawer = new VerticalLayout();
    private final DrawerToggle toggle = new DrawerToggle();
    private final MenuBar userMenu;
    private final MenuBar languageMenu;
    private final MenuBar appearanceMenu;
    private final boolean authenticated;
    private String currentRoute = "";

    public DefaultMainLayout(AdminModuleRegistry modules, CurrentUserProvider currentUser,
                      AuthorizationService authorization, AdminLocalePreference localePreference,
                      I18NProvider translations, AdminAppearanceProperties appearance,
                      AuthenticationContext authenticationContext) {
        this.modules = modules;
        this.authorization = authorization;
        this.localePreference = localePreference;
        this.translations = translations;
        this.appearance = appearance;
        var productMark = AdminIcon.of(AdminIconName.CUBE);
        productMark.addClassName("admin-product-mark");
        var productName = new Span("VAdmin");
        productName.addClassName("admin-shell-brand");
        currentLocation.addClassName("admin-shell-location");

        var currentUserValue = currentUser.currentUser();
        authenticated = currentUserValue.isPresent();
        if (!authenticated) {
            user = null;
            userMenu = null;
            languageMenu = null;
            appearanceMenu = null;
            addHeader(productMark, productName);
            return;
        }

        user = currentUserValue.orElseThrow();
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        userMenu = createUserMenu(user.username(), authenticationContext);
        languageMenu = createUtilityMenu("admin-language-menu");
        appearanceMenu = createUtilityMenu("admin-appearance-menu");
        updateHeaderText();
        addHeader(toggle, productMark, productName, currentLocation, createUtilityControls());

        drawer.setPadding(false);
        drawer.setSpacing(true);
        drawer.addClassName("admin-drawer-content");
        rebuildDrawer();
        addToDrawer(drawer);
    }

    private void addHeader(Component... components) {
        var header = new HorizontalLayout(components);
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.setWrap(true);
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
        applyHostAppearance();
        applyColorScheme(sessionColorScheme());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        if (!authenticated) return;
        updateHeaderText();
        rebuildDrawer();
        updateCurrentLocation();
    }

    private MenuBar createUserMenu(String username, AuthenticationContext authenticationContext) {
        var menu = new MenuBar();
        menu.setOpenOnHover(false);
        menu.addClassName("admin-user-menu");
        var avatar = new Avatar(username);
        var trigger = menu.addItem(avatar);
        trigger.getSubMenu().addItem(text("system.shell.logout"), event -> {
            if (event.isFromClient()) authenticationContext.logout();
        });
        return menu;
    }

    private MenuBar createUtilityMenu(String className) {
        var menu = new MenuBar();
        menu.setOpenOnHover(false);
        menu.addClassNames("admin-shell-utility", className);
        return menu;
    }

    private HorizontalLayout createUtilityControls() {
        var utilities = new HorizontalLayout(languageMenu, appearanceMenu, userMenu);
        utilities.setPadding(false);
        utilities.setSpacing(false);
        utilities.setAlignItems(HorizontalLayout.Alignment.CENTER);
        utilities.addClassName("admin-shell-utilities");
        return utilities;
    }

    private void rebuildDrawer() {
        drawer.removeAll();
        drawer.add(navigationGroup(text("system.shell.workspace"),
                new SideNavItem(text("system.shell.home"), "", AdminIcon.of(AdminIconName.HOME))));
        var visiblePages = modules.pagesVisibleTo(user, authorization);
        modules.groupsVisibleTo(user, authorization).forEach(group -> addNavigationGroup(drawer, text(group.titleKey()),
                visiblePages.stream().filter(page -> page.groupId().equals(group.id())).toList()));
    }

    private void addNavigationGroup(VerticalLayout target, String label, java.util.List<AdminPage> pages) {
        if (!pages.isEmpty()) target.add(navigationGroup(label, pages.stream()
                .map(page -> new SideNavItem(titleFor(page), page.route(), AdminIconCatalog.createAdminIcon(page.iconKey())))
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

    private void selectColorScheme(String colorScheme) {
        VaadinSession.getCurrent().setAttribute(COLOR_SCHEME_KEY, colorScheme);
        applyColorScheme(colorScheme);
        updateAppearanceMenu();
    }

    private String sessionColorScheme() {
        var scheme = VaadinSession.getCurrent().getAttribute(COLOR_SCHEME_KEY);
        if ("light".equals(scheme) || "dark".equals(scheme)) return (String) scheme;
        return "system";
    }

    private void applyHostAppearance() {
        var root = UI.getCurrent().getElement();
        root.setAttribute("data-vadmin-visual-language", appearance.visualLanguage().cssValue());
    }

    private void applyColorScheme(String colorScheme) {
        var value = switch (colorScheme) {
            case "light" -> ColorScheme.Value.LIGHT;
            case "dark" -> ColorScheme.Value.DARK;
            default -> ColorScheme.Value.SYSTEM;
        };
        UI.getCurrent().getPage().setColorScheme(value);
    }

    private void updateHeaderText() {
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        userMenu.getElement().setAttribute("aria-label", text("system.shell.current-user"));
        updateLanguageMenu();
        updateAppearanceMenu();
    }

    private void updateCurrentLocation() {
        currentLocation.setText(modules.pages().stream().filter(page -> page.route().equals(currentRoute)).findFirst()
                .map(this::titleFor).orElse(text("system.shell.home")));
    }

    private void updateLanguageMenu() {
        languageMenu.removeAll();
        languageMenu.getElement().setAttribute("aria-label", text("system.shell.language-menu"));
        languageMenu.setVisible(translations.getProvidedLocales().size() > 1);
        var trigger = languageMenu.addItem(AdminIcon.of(AdminIconName.GLOBE));
        trigger.setAriaLabel(text("system.shell.language"));
        trigger.setTooltipText(text("system.shell.language"));
        translations.getProvidedLocales().forEach(locale -> addLanguageChoice(trigger, locale));
    }

    private void addLanguageChoice(MenuItem trigger, Locale locale) {
        var choice = trigger.getSubMenu().addItem(text("system.shell.language." + locale.toLanguageTag()), event -> {
            if (event.isFromClient()) selectLanguage(locale);
        });
        choice.setCheckable(true);
        choice.setChecked(locale.equals(UI.getCurrent().getLocale()));
    }

    private void selectLanguage(Locale locale) {
        localePreference.select(UI.getCurrent(), locale);
        updateLanguageMenu();
    }

    private void updateAppearanceMenu() {
        appearanceMenu.removeAll();
        appearanceMenu.getElement().setAttribute("aria-label", text("system.shell.appearance-menu"));
        var trigger = appearanceMenu.addItem(AdminIcon.of(AdminIconName.PALETTE));
        trigger.setAriaLabel(text("system.shell.appearance"));
        trigger.setTooltipText(text("system.shell.appearance"));
        addColorSchemeChoice(trigger, "system");
        addColorSchemeChoice(trigger, "light");
        addColorSchemeChoice(trigger, "dark");
    }

    private void addColorSchemeChoice(MenuItem trigger, String colorScheme) {
        var choice = trigger.getSubMenu().addItem(text("system.shell.appearance." + colorScheme), event -> {
            if (event.isFromClient()) selectColorScheme(colorScheme);
        });
        choice.setCheckable(true);
        choice.setChecked(sessionColorScheme().equals(colorScheme));
    }

    private String titleFor(AdminPage page) { return text(page.titleKey()); }
    private String text(String key, Object... parameters) { return getTranslation(key, parameters); }
}
