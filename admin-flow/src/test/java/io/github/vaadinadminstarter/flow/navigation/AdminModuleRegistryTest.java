package io.github.vaadinadminstarter.flow.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

class AdminModuleRegistryTest {
    private static final PermissionCode ORDERS_READ = PermissionCode.of("orders:order:read");
    private static final PermissionCode RETURNS_READ = PermissionCode.of("orders:return:read");
    private static final PermissionCode INVOICES_READ = PermissionCode.of("billing:invoice:read");

    @Test
    void returnsOnlyAccessibleGroupsAndPagesInDeterministicOrder() {
        var registry = new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 200),
                        page("orders.list", "orders.business", 200, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("invoices.finance", "invoices.navigation.finance", 100),
                        page("invoices.list", "invoices.finance", 100, "invoices", INVOICES_READ), INVOICES_READ),
                module("returns", new AdminNavigationGroup("returns.business", "returns.navigation.business", 100),
                        page("returns.list", "returns.business", 100, "returns", RETURNS_READ), RETURNS_READ)));

        var user = userWith(ORDERS_READ, RETURNS_READ);

        assertThat(registry.groupsVisibleTo(user, authorization()))
                .extracting(AdminNavigationGroup::id)
                .containsExactly("returns.business", "orders.business");
        assertThat(registry.pagesVisibleTo(user, authorization()))
                .extracting(AdminPage::route)
                .containsExactly("returns", "orders");
        assertThat(registry.pages()).extracting(AdminPage::route)
                .containsExactly("invoices", "returns", "orders");
    }

    @Test
    void reportsBothModulesWhenRoutesCollide() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 100),
                        page("orders.list", "orders.business", 100, "work", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("invoices.finance", "invoices.navigation.finance", 100),
                        page("invoices.list", "invoices.finance", 100, "work", INVOICES_READ), INVOICES_READ))))
                .withMessageContaining("work")
                .withMessageContaining("orders")
                .withMessageContaining("invoices");
    }

    @Test
    void rejectsNavigationGroupReuseAcrossModules() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("business", "orders.navigation.business", 100),
                        page("orders.list", "business", 100, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("business", "invoices.navigation.business", 100),
                        page("invoices.list", "business", 100, "invoices", INVOICES_READ), INVOICES_READ))))
                .withMessageContaining("business")
                .withMessageContaining("orders")
                .withMessageContaining("invoices");
    }

    @Test
    void rejectsTranslationKeysOutsideTheModuleNamespace() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdminModule("orders",
                List.of(new AdminNavigationGroup("orders.business", "navigation.business", 100)),
                List.of(page("orders.list", "orders.business", 100, "orders", ORDERS_READ)),
                Set.of(ORDERS_READ), List.of(new AdminMessageBundle("orders", "orders.i18n.messages"))))
                .withMessageContaining("orders")
                .withMessageContaining("navigation.business");
    }

    @Test
    void reportsBothModulesWhenModuleIdsCollide() {
        assertCollision("orders", () -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 100),
                        page("orders.list", "orders.business", 100, "orders", ORDERS_READ), ORDERS_READ),
                module("orders", new AdminNavigationGroup("orders.billing", "orders.navigation.billing", 200),
                        page("orders.billing", "orders.billing", 100, "orders-billing", INVOICES_READ), INVOICES_READ))),
                "orders", "orders");
    }

    @Test
    void reportsBothModulesWhenPageIdsCollide() {
        assertCollision("shared.page", () -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 100),
                        pageFor("orders", "shared.page", "orders.business", 100, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("invoices.finance", "invoices.navigation.finance", 100),
                        pageFor("invoices", "shared.page", "invoices.finance", 100, "invoices", INVOICES_READ), INVOICES_READ))),
                "orders", "invoices");
    }

    @Test
    void reportsBothModulesWhenOwnedPermissionsCollide() {
        assertCollision("orders:order:read", () -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 100),
                        page("orders.list", "orders.business", 100, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("invoices.finance", "invoices.navigation.finance", 100),
                        page("invoices.list", "invoices.finance", 100, "invoices", ORDERS_READ), ORDERS_READ))),
                "orders", "invoices");
    }

    @Test
    void reportsBothModulesWhenMessageBundleDescriptorsCollide() {
        assertCollision("shared.i18n.messages", () -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("orders.business", "orders.navigation.business", 100),
                        page("orders.list", "orders.business", 100, "orders", ORDERS_READ), ORDERS_READ,
                        "shared.i18n.messages"),
                module("invoices", new AdminNavigationGroup("invoices.finance", "invoices.navigation.finance", 100),
                        page("invoices.list", "invoices.finance", 100, "invoices", INVOICES_READ), INVOICES_READ,
                        "shared.i18n.messages"))), "orders", "invoices");
    }

    private static AdminModule module(String id, AdminNavigationGroup group, AdminPage page, PermissionCode permission) {
        return module(id, group, page, permission, id + ".i18n.messages");
    }

    private static AdminModule module(String id, AdminNavigationGroup group, AdminPage page, PermissionCode permission,
                                      String messageBundleBaseName) {
        return AdminModule.of(id, List.of(group), List.of(page), Set.of(permission),
                List.of(new AdminMessageBundle(id, messageBundleBaseName)));
    }

    private static AdminPage page(String id, String groupId, int order, String route, PermissionCode permission) {
        var moduleId = id.substring(0, id.indexOf('.'));
        return pageFor(moduleId, id, groupId, order, route, permission);
    }

    private static AdminPage pageFor(String moduleId, String id, String groupId, int order, String route,
                                     PermissionCode permission) {
        return new AdminPage(id, groupId, moduleId + ".page." + id.replace('.', '-') + ".title",
                moduleId + ".page." + id.replace('.', '-') + ".intent", "briefcase", order, route, permission,
                TestView.class);
    }

    private static void assertCollision(String value, org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
                                        String firstModuleId, String secondModuleId) {
        assertThatIllegalArgumentException().isThrownBy(action)
                .withMessageContaining(value)
                .withMessageContaining(firstModuleId)
                .withMessageContaining(secondModuleId);
    }

    private static CurrentUser userWith(PermissionCode... permissions) {
        return new CurrentUser(UUID.randomUUID(), "operator", Set.of(permissions), 1);
    }

    private static AuthorizationService authorization() {
        return new AuthorizationService() {
            @Override
            public boolean hasPermission(CurrentUser user, PermissionCode permission) {
                return user.permissions().contains(permission);
            }

            @Override
            public void requirePermission(CurrentUser user, PermissionCode permission) {
                throw new UnsupportedOperationException();
            }
        };
    }

    static final class TestView extends Div {
    }
}
