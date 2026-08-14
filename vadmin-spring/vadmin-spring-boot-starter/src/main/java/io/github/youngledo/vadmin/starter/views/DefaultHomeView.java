package io.github.youngledo.vadmin.starter.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUser;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;
import io.github.youngledo.vadmin.flow.patterns.AdminPageFrame;
import io.github.youngledo.vadmin.flow.patterns.PageHeader;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = DefaultMainLayout.class)
@PermitAll
public final class DefaultHomeView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
    private final AdminModuleRegistry pages;
    private final CurrentUser user;
    private final AuthorizationService authorization;
    private final PageHeader header = PageHeader.translated("system.home.title", "system.home.description");
    private final Span account = new Span();
    private final VerticalLayout shortcuts = new VerticalLayout();
    private final H2 available = new H2();

    public DefaultHomeView(AdminModuleRegistry pages, CurrentUserProvider currentUser, AuthorizationService authorization) {
        this.pages = pages;
        user = currentUser.currentUser().orElseThrow();
        this.authorization = authorization;
        setSizeFull();
        setPadding(true);
        header.setLocation(account);
        shortcuts.setPadding(false);
        shortcuts.setSpacing(true);
        shortcuts.getElement().setAttribute("data-testid", "workplace-shortcuts");
        var frame = new AdminPageFrame(header, null, shortcuts);
        add(frame);
        expand(frame);
        updateText();
    }

    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }

    @Override public String getPageTitle() { return getTranslation("system.home.title"); }

    private void updateText() {
        account.setText(getTranslation("system.home.account", user.username()));
        available.setText(getTranslation("system.home.available"));
        shortcuts.removeAll();
        shortcuts.add(available);
        pages.pagesVisibleTo(user, authorization).forEach(page -> shortcuts.add(shortcut(page)));
    }

    private Anchor shortcut(AdminPage page) {
        var title = getTranslation(page.titleKey());
        var shortcut = new Anchor(page.route());
        shortcut.getElement().setAttribute("aria-label", title);
        shortcut.getElement().setAttribute("data-testid", "workplace-entry");
        shortcut.addClassName("admin-workplace-entry");
        var entryTitle = new Span(title);
        entryTitle.addClassName("admin-workplace-entry-title");
        var intent = new Span(getTranslation(page.intentKey()));
        intent.addClassName("admin-workplace-entry-intent");
        shortcut.add(new Div(entryTitle, intent));
        return shortcut;
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
