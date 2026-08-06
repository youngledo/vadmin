package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.flow.navigation.PageDefinition;
import io.github.vaadinadminstarter.flow.navigation.PageRegistry;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("工作台")
@PermitAll
public final class HomeView extends VerticalLayout {
    public HomeView(PageRegistry pages, SecurityContextCurrentUserProvider currentUser,
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
        pages.visibleTo(user, authorization).forEach(page -> shortcuts.add(shortcut(page)));

        add(header, shortcuts);
    }

    private Anchor shortcut(PageDefinition page) {
        var title = titleFor(page);
        var shortcut = new Anchor(page.route());
        shortcut.getElement().setAttribute("aria-label", title);
        shortcut.getElement().setAttribute("data-testid", "workplace-entry");
        shortcut.addClassName("admin-workplace-entry");
        var entryTitle = new Span(title);
        entryTitle.addClassName("admin-workplace-entry-title");
        var intent = new Span(intentFor(page));
        intent.addClassName("admin-workplace-entry-intent");
        shortcut.add(new Div(entryTitle, intent));
        return shortcut;
    }

    private String intentFor(PageDefinition page) {
        return switch (page.pageId()) {
            case "system-users" -> "管理可登录账户及其启用状态。";
            case "system-roles" -> "查看角色并授予已登记的系统权限。";
            case "system-permissions" -> "查阅受系统管理的权限目录。";
            case "system-audit" -> "查看已记录的管理操作审计日志。";
            case "customers" -> "维护客户档案和受控附件。";
            default -> "进入已授权的管理工作区。";
        };
    }

    private String titleFor(PageDefinition page) {
        return switch (page.pageId()) {
            case "system-users" -> "用户";
            case "system-roles" -> "角色";
            case "system-permissions" -> "权限目录";
            case "system-audit" -> "审计日志";
            case "customers" -> "客户";
            default -> page.pageId();
        };
    }
}
