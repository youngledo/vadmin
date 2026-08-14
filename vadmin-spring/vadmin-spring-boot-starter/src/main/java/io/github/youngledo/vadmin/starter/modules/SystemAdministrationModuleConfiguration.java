package io.github.youngledo.vadmin.starter.modules;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminNavigationGroup;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;
import io.github.youngledo.vadmin.starter.views.AuditView;
import io.github.youngledo.vadmin.starter.views.PermissionsView;
import io.github.youngledo.vadmin.starter.views.RolesView;
import io.github.youngledo.vadmin.starter.views.UsersView;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SystemAdministrationModuleConfiguration {
    @Bean
    public AdminModule systemAdministration() {
        return AdminModule.of("system",
                List.of(new AdminNavigationGroup("system", "system.navigation", 100)),
                List.of(
                        page("system.users", "system.users.title", "system.users.intent", "users", 100, "users",
                                UsersView.REQUIRED_PERMISSION, UsersView.class),
                        page("system.roles", "system.roles.title", "system.roles.intent", "shield", 200, "roles",
                                RolesView.REQUIRED_PERMISSION, RolesView.class),
                        page("system.permissions", "system.permissions.title", "system.permissions.intent", "key", 300,
                                "permissions", PermissionsView.REQUIRED_PERMISSION, PermissionsView.class),
                        page("system.audit", "system.audit.title", "system.audit.intent", "clock", 400, "audit",
                                AuditView.REQUIRED_PERMISSION, AuditView.class)),
                Set.of(
                        UsersView.REQUIRED_PERMISSION,
                        PermissionCode.of("system:user:create"),
                        PermissionCode.of("system:user:update"),
                        RolesView.REQUIRED_PERMISSION,
                        PermissionCode.of("system:role:grant"),
                        PermissionsView.REQUIRED_PERMISSION,
                        AuditView.REQUIRED_PERMISSION),
                List.of(new AdminMessageBundle("system", "i18n.system")));
    }

    private static AdminPage page(String pageId, String titleKey, String intentKey, String iconKey, int order,
                                  String route, PermissionCode permission,
                                  Class<? extends com.vaadin.flow.component.Component> viewType) {
        return new AdminPage(pageId, "system", titleKey, intentKey, iconKey, order, route, permission, viewType);
    }
}
