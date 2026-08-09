package io.github.vaadinadminstarter.app.modules;

import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.vaadinadminstarter.app.views.AuditView;
import io.github.vaadinadminstarter.app.views.CustomersView;
import io.github.vaadinadminstarter.app.views.PermissionsView;
import io.github.vaadinadminstarter.app.views.RolesView;
import io.github.vaadinadminstarter.app.views.UsersView;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminNavigationGroup;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;

/** Built-in module contributions owned by the reference application's host. */
@Configuration(proxyBeanMethods = false)
public class ReferenceAdminModules {
    @Bean
    public AdminModule systemAdministration() {
        return AdminModule.of("system",
                List.of(new AdminNavigationGroup("system", "system.navigation", 100)),
                List.of(
                        page("system.users", "system", "system.users.title", "system.users.intent", "users", 100,
                                "users", UsersView.REQUIRED_PERMISSION, UsersView.class),
                        page("system.roles", "system", "system.roles.title", "system.roles.intent", "shield", 200,
                                "roles", RolesView.REQUIRED_PERMISSION, RolesView.class),
                        page("system.permissions", "system", "system.permissions.title", "system.permissions.intent", "key", 300,
                                "permissions", PermissionsView.REQUIRED_PERMISSION, PermissionsView.class),
                        page("system.audit", "system", "system.audit.title", "system.audit.intent", "clock", 400,
                                "audit", AuditView.REQUIRED_PERMISSION, AuditView.class)),
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

    @Bean
    public AdminModule customerAdministration() {
        return AdminModule.of("customers",
                List.of(new AdminNavigationGroup("customers", "customers.navigation", 200)),
                List.of(page("customers.customers", "customers", "customers.customers.title",
                        "customers.customers.intent", "briefcase", 100, "customers",
                        CustomersView.REQUIRED_PERMISSION, CustomersView.class)),
                Set.of(
                        CustomersView.REQUIRED_PERMISSION,
                        PermissionCode.of("customer:customer:create"),
                        PermissionCode.of("customer:customer:update"),
                        PermissionCode.of("customer:customer:delete"),
                        PermissionCode.of("customer:attachment:upload")),
                List.of(new AdminMessageBundle("customers", "i18n.customers")));
    }

    private static AdminPage page(String pageId, String groupId, String titleKey, String intentKey, String iconKey,
                                  int order, String route, PermissionCode requiredPermission,
                                  Class<? extends com.vaadin.flow.component.Component> viewType) {
        return new AdminPage(pageId, groupId, titleKey, intentKey, iconKey, order, route, requiredPermission, viewType);
    }
}
