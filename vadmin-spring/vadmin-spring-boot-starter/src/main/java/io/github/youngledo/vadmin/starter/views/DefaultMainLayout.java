package io.github.youngledo.vadmin.starter.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
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
    private final Button userAvatar;
    private final Popover userPopover;
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
        var productMark = AdminIcon.of(AdminIconName.CUBE);
        productMark.addClassName("admin-product-mark");
        var productName = new Span(brandProperties.name());
        productName.addClassName("admin-shell-brand");
        var brand = createBrand(productMark, productName);

        var currentUserValue = currentUser.currentUser();
        authenticated = currentUserValue.isPresent();
        if (!authenticated) {
            user = null;
            userAvatar = null;
            userPopover = null;
            addHeader(brand);
            return;
        }

        user = currentUserValue.orElseThrow();
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);
        // AppLayout exposes drawer width through this documented component property.
        getStyle().set("--vaadin-app-layout-drawer-width", "20rem");
        toggle.setAriaLabel(text("system.shell.navigation-toggle"));
        toggle.setTooltipText(text("system.shell.navigation-toggle"));
        userAvatar = createUserAvatar();
        userPopover = createUserPopover();
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

    private void addHeader(Component... components) {
        var header = new HorizontalLayout(components);
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        header.addClassName("admin-shell-header");
        addToNavbar(header);
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

    private Button createUserAvatar() {
        var avatar = new Avatar(user.username());
        var trigger = new Button(avatar);
        trigger.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        trigger.addClassName("admin-user-avatar");
        trigger.setAriaLabel(text("system.shell.current-user"));
        trigger.setTooltipText(text("system.shell.current-user"));
        return trigger;
    }

    private Popover createUserPopover() {
        var popover = new Popover();
        popover.setTarget(userAvatar);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.setAriaLabel(text("system.shell.current-user"));
        updateUserPopover(popover);
        return popover;
    }

    private HorizontalLayout createUtilityControls() {
        var utilities = new HorizontalLayout(userAvatar);
        utilities.setPadding(false);
        utilities.setSpacing(false);
        utilities.setAlignItems(HorizontalLayout.Alignment.CENTER);
        utilities.addClassName("admin-shell-utilities");
        return utilities;
    }

    private void rebuildDrawer() {
        drawer.removeAll();
        if (shell.workplaceEnabled()) {
            drawer.add(navigationGroup(text("system.shell.workspace"),
                    new SideNavItem(text("system.shell.home"), "", AdminIcon.of(AdminIconName.HOME))));
        }
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
        navigation.setWidthFull();
        navigation.addClassName("admin-drawer-nav");
        navigation.addItem(items);
        return new Component[]{section, navigation};
    }

    private void selectColorScheme(String colorScheme) {
        VaadinSession.getCurrent().setAttribute(COLOR_SCHEME_KEY, colorScheme);
        applyColorScheme(colorScheme);
        updateUserPopover();
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
        userAvatar.setAriaLabel(text("system.shell.current-user"));
        userAvatar.setTooltipText(text("system.shell.current-user"));
        userPopover.setAriaLabel(text("system.shell.current-user"));
        updateUserPopover();
    }

    private void updateUserPopover() {
        updateUserPopover(userPopover);
    }

    private void updateUserPopover(Popover popover) {
        popover.removeAll();
        var content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();
        if (translations.getProvidedLocales().size() > 1) {
            var language = new Select<Locale>();
            language.setLabel(text("system.shell.language"));
            language.setItems(translations.getProvidedLocales());
            language.setItemLabelGenerator(locale -> text("system.shell.language." + locale.toLanguageTag()));
            language.setValue(UI.getCurrent().getLocale());
            language.addValueChangeListener(event -> {
                if (event.isFromClient() && event.getValue() != null) selectLanguage(event.getValue());
            });
            content.add(language);
        }

        var colorScheme = new RadioButtonGroup<String>();
        colorScheme.setLabel(text("system.shell.appearance"));
        colorScheme.setItems("system", "light", "dark");
        colorScheme.setItemLabelGenerator(value -> text("system.shell.appearance." + value));
        colorScheme.setValue(sessionColorScheme());
        colorScheme.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) selectColorScheme(event.getValue());
        });
        var logout = new Button(text("system.shell.logout"), event -> logout());
        content.add(colorScheme, logout);
        popover.add(content);
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
        updateUserPopover();
    }

    private String titleFor(AdminPage page) { return text(page.titleKey()); }
    private String text(String key, Object... parameters) { return getTranslation(key, parameters); }
}
