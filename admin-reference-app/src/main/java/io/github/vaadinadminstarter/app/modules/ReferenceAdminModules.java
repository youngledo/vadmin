package io.github.vaadinadminstarter.app.modules;

import java.util.List;
import java.util.Map;
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
    private static final Map<String, String> LEGACY_LABELS = Map.ofEntries(
            Map.entry("system.navigation", "系统管理"),
            Map.entry("customers.navigation", "客户管理"),
            Map.entry("system.users.title", "用户"),
            Map.entry("system.users.intent", "管理可登录账户及其启用状态。"),
            Map.entry("system.roles.title", "角色"),
            Map.entry("system.roles.intent", "查看角色并授予已登记的系统权限。"),
            Map.entry("system.permissions.title", "权限目录"),
            Map.entry("system.permissions.intent", "查阅受系统管理的权限目录。"),
            Map.entry("system.audit.title", "审计日志"),
            Map.entry("system.audit.intent", "查看已记录的管理操作审计日志。"),
            Map.entry("customers.customers.title", "客户"),
            Map.entry("customers.customers.intent", "维护客户档案和受控附件。"));

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

    public static String legacyLabel(String key) {
        return LEGACY_LABELS.getOrDefault(key, key);
    }

    private static AdminPage page(String pageId, String groupId, String titleKey, String intentKey, String iconKey,
                                  int order, String route, PermissionCode requiredPermission,
                                  Class<? extends com.vaadin.flow.component.Component> viewType) {
        return new AdminPage(pageId, groupId, titleKey, intentKey, iconKey, order, route, requiredPermission, viewType);
    }
}
