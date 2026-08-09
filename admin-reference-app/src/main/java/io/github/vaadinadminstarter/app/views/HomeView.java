package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.app.modules.ReferenceAdminModules;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("工作台")
@PermitAll
public final class HomeView extends VerticalLayout {
    public HomeView(AdminModuleRegistry pages, CurrentUserProvider currentUser,
                    AuthorizationService authorization) {
        setSizeFull();
        setPadding(true);

        var user = currentUser.currentUser().orElseThrow();
        var header = new PageHeader("工作台", "从当前账户已授权的工作区开始处理日常管理任务。");
        header.setLocation(new Span("当前账户：" + user.username()));

        var shortcuts = new VerticalLayout();
        shortcuts.setPadding(false);
        shortcuts.setSpacing(true);
        shortcuts.getElement().setAttribute("data-testid", "workplace-shortcuts");
        shortcuts.add(new H2("可访问的工作区"));
        pages.pagesVisibleTo(user, authorization).forEach(page -> shortcuts.add(shortcut(page)));

        add(header, shortcuts);
    }

    private Anchor shortcut(AdminPage page) {
        var title = ReferenceAdminModules.legacyLabel(page.titleKey());
        var shortcut = new Anchor(page.route());
        shortcut.getElement().setAttribute("aria-label", title);
        shortcut.getElement().setAttribute("data-testid", "workplace-entry");
        shortcut.addClassName("admin-workplace-entry");
        var entryTitle = new Span(title);
        entryTitle.addClassName("admin-workplace-entry-title");
        var intent = new Span(ReferenceAdminModules.legacyLabel(page.intentKey()));
        intent.addClassName("admin-workplace-entry-intent");
        shortcut.add(new Div(entryTitle, intent));
        return shortcut;
    }

}
