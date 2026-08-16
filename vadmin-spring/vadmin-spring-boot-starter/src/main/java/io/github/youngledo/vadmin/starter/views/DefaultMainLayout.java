package io.github.youngledo.vadmin.starter.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import io.github.youngledo.vadmin.starter.brand.AdminBrandProperties;
import io.github.youngledo.vadmin.starter.shell.AdminShellProperties;
import io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties;
import io.github.youngledo.vadmin.starter.theme.AdminVisualLanguage;
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
import org.springframework.web.context.support.WebApplicationContextUtils;

@Layout
@PermitAll
@Uses(UsersView.class)
@Uses(RolesView.class)
@Uses(PermissionsView.class)
@Uses(AuditView.class)
public final class DefaultMainLayout extends AppLayout implements LocaleChangeObserver {
    private static final String COLOR_SCHEME_KEY = DefaultMainLayout.class.getName() + ".color-scheme";

    private final AdminModuleRegistry modules;
    private final CurrentUser user;
    private final AuthorizationService authorization;
    private final AdminLocalePreference localePreference;
    private final I18NProvider translations;
    private final AdminAppearanceProperties appearance;
    private final AdminShellProperties shell;
    private final VerticalLayout drawer = new VerticalLayout();
    private final DrawerToggle toggle = new DrawerToggle();
    private final MenuBar languageMenu;
    private final MenuBar userMenu;
    private final boolean authenticated;

    public DefaultMainLayout(AdminModuleRegistry modules, CurrentUserProvider currentUser,
                      AuthorizationService authorization, AdminLocalePreference localePreference,
                      I18NProvider translations, AdminAppearanceProperties appearance,
                      AdminBrandProperties brandProperties, AdminShellProperties shell) {
        this.modules = modules;
        this.authorization = authorization;
        this.localePreference = localePreference;
        this.translations = translations;
        this.appearance = appearance;
        this.shell = shell;
        var currentUserValue = currentUser.currentUser();
        authenticated = currentUserValue.isPresent();
        if (!authenticated) {
            user = null;
            languageMenu = null;
            userMenu = null;
            return;
        }

        user = currentUserValue.orElseThrow();
        var productMark = AdminIcon.of(AdminIconName.CUBE);
        productMark.addClassName("admin-product-mark");
        var productName = new Span(brandProperties.name());
        productName.addClassName("admin-shell-brand");
        var brand = createBrand(productMark, productName);
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
        // AppLayout exposes drawer width through this documented component property.
        getStyle().set("--vaadin-app-layout-drawer-width", "20rem");
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        languageMenu = translations.getProvidedLocales().size() > 1 ? createLanguageMenu() : null;
        userMenu = createUserMenu();
        updateHeaderText();
        addHeader(createNavigation(brand), createUtilityControls());

        drawer.setPadding(true);
        drawer.setSpacing(true);
        drawer.setWidthFull();
        drawer.setAlignItems(VerticalLayout.Alignment.STRETCH);
        drawer.addClassName("admin-drawer-content");
        rebuildDrawer();
        addToDrawer(drawer);
    }

    private HorizontalLayout createBrand(Component productMark, Component productName) {
        var brand = new HorizontalLayout(productMark, productName);
        brand.setPadding(false);
        brand.setSpacing(true);
        brand.setAlignItems(HorizontalLayout.Alignment.CENTER);
        return brand;
    }

    private HorizontalLayout createNavigation(Component brand) {
        var navigation = new HorizontalLayout(toggle, brand);
        navigation.setPadding(false);
        navigation.setSpacing(true);
        navigation.setAlignItems(HorizontalLayout.Alignment.CENTER);
        return navigation;
    }

    private void addHeader(HorizontalLayout navigation, HorizontalLayout utilities) {
        // Flow's full-width navbar children otherwise extend through AppLayout's slot inset.
        navigation.getElement().getStyle().set("margin-inline-start", "var(--lumo-space-m)");
        utilities.getElement().getStyle().set("margin-inline-end", "var(--lumo-space-m)");
        navigation.getElement().getStyle().set("flex-grow", "1");
        addToNavbar(navigation, utilities);
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
    }

    private HorizontalLayout createUtilityControls() {
        var utilities = new HorizontalLayout();
        utilities.setPadding(false);
        utilities.setSpacing(true);
        utilities.setAlignItems(HorizontalLayout.Alignment.CENTER);
        if (languageMenu != null) utilities.add(languageMenu);
        utilities.add(userMenu);
        utilities.addClassName("admin-shell-utilities");
        return utilities;
    }

    private MenuBar createLanguageMenu() {
        var menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_TERTIARY_INLINE);
        rebuildLanguageMenu(menu);
        return menu;
    }

    private MenuBar createUserMenu() {
        var menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_TERTIARY_INLINE);
        rebuildUserMenu(menu);
        return menu;
    }

    private void rebuildDrawer() {
        drawer.removeAll();
        if (shell.workplaceEnabled()) {
            drawer.add(navigationGroup(text("system.shell.workspace"),
                    new SideNavItem(text("system.shell.home"), "", navigationIcon(AdminIconName.HOME.cssValue()))));
        }
        var visiblePages = modules.pagesVisibleTo(user, authorization);
        var visibleGroups = modules.groupsVisibleTo(user, authorization).stream()
                .map(group -> new NavigationGroup(group.titleKey(), visiblePages.stream()
                        .filter(page -> page.groupId().equals(group.id()))
                        .toList()))
                .filter(group -> !group.pages().isEmpty())
                .toList();
        var showGroupLabels = visibleGroups.size() > 1;
        visibleGroups.forEach(group -> addNavigationGroup(drawer, text(group.titleKey()), group.pages(), showGroupLabels));
    }

    private void addNavigationGroup(VerticalLayout target, String label, java.util.List<AdminPage> pages, boolean showLabel) {
        var navigation = new SideNav();
        navigation.getElement().setAttribute("aria-label", label);
        navigation.addClassName("admin-drawer-nav");
        navigation.addItem(pages.stream()
                .map(page -> new SideNavItem(titleFor(page), page.route(), navigationIcon(page.iconKey())))
                .toArray(SideNavItem[]::new));
        if (showLabel) {
            var section = new Span(label);
            section.addClassName("admin-drawer-section");
            target.add(section, navigation);
            return;
        }
        target.add(navigation);
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

    private Component navigationIcon(String iconKey) {
        if (appearance.visualLanguage() == AdminVisualLanguage.ANT) return AdminIconCatalog.createAdminIcon(iconKey);
        return AdminIconCatalog.create(iconKey);
    }

    private void selectColorScheme(String colorScheme) {
        VaadinSession.getCurrent().setAttribute(COLOR_SCHEME_KEY, colorScheme);
        applyColorScheme(colorScheme);
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
        rebuildLanguageMenu(languageMenu);
        rebuildUserMenu(userMenu);
    }

    private void rebuildLanguageMenu(MenuBar menu) {
        if (menu == null) return;
        menu.removeAll();
        var trigger = menu.addItem(VaadinIcon.GLOBE.create());
        trigger.setAriaLabel(text("system.shell.language"));
        trigger.setTooltipText(text("system.shell.language"));
        var selectedLocale = UI.getCurrent().getLocale();
        translations.getProvidedLocales().forEach(locale -> {
            var option = trigger.getSubMenu().addItem(languageLabel(locale), event -> selectLanguage(locale));
            option.setCheckable(true);
            option.setChecked(locale.equals(selectedLocale));
        });
    }

    private void rebuildUserMenu(MenuBar menu) {
        menu.removeAll();
        var trigger = menu.addItem(new Avatar(user.username()));
        trigger.setAriaLabel(text("system.shell.current-user"));
        trigger.setTooltipText(text("system.shell.current-user"));
        var actions = trigger.getSubMenu();
        actions.addItem(text("system.shell.profile"), event -> openPersonalSettings());
        actions.addItem(text("system.shell.theme"), event -> openThemeSettings());
        actions.addSeparator();
        actions.addItem(text("system.shell.logout"), event -> logout());
    }

    private void openPersonalSettings() {
        var dialog = new Dialog();
        dialog.setHeaderTitle(text("system.shell.profile"));
        var form = new FormLayout();
        var username = new TextField(text("system.shell.username"));
        username.setValue(user.username());
        username.setReadOnly(true);
        form.add(username);
        dialog.add(form);
        dialog.open();
    }

    private void openThemeSettings() {
        var dialog = new Dialog();
        dialog.setHeaderTitle(text("system.shell.theme"));
        var colorScheme = new Select<String>();
        colorScheme.setLabel(text("system.shell.appearance"));
        colorScheme.setItems("system", "light", "dark");
        colorScheme.setItemLabelGenerator(value -> switch (value) {
            case "light" -> text("system.shell.appearance.light");
            case "dark" -> text("system.shell.appearance.dark");
            default -> text("system.shell.appearance.system");
        });
        colorScheme.setValue(sessionColorScheme());
        colorScheme.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) selectColorScheme(event.getValue());
        });
        dialog.add(colorScheme);
        dialog.getFooter().add(new Button(text("system.shell.close"), event -> dialog.close()));
        dialog.open();
    }

    private void logout() {
        var request = VaadinServletRequest.getCurrent();
        if (request == null) return;
        WebApplicationContextUtils.getRequiredWebApplicationContext(request.getHttpServletRequest().getServletContext())
                .getBean(AuthenticationContext.class)
                .logout();
    }

    private void selectLanguage(Locale locale) {
        localePreference.select(UI.getCurrent(), locale);
    }

    private String titleFor(AdminPage page) { return text(page.titleKey()); }
    private String languageLabel(Locale locale) {
        return switch (locale.toLanguageTag()) {
            case "zh-CN" -> "简体中文";
            case "en-US" -> "English";
            default -> locale.getDisplayLanguage(locale);
        };
    }
    private String text(String key, Object... parameters) { return getTranslation(key, parameters); }

    private record NavigationGroup(String titleKey, java.util.List<AdminPage> pages) {
    }
}
