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
                module("orders", new AdminNavigationGroup("business", "navigation.business", 200),
                        page("orders.list", "business", 200, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("finance", "navigation.finance", 100),
                        page("invoices.list", "finance", 100, "invoices", INVOICES_READ), INVOICES_READ),
                module("returns", new AdminNavigationGroup("business", "navigation.business", 200),
                        page("returns.list", "business", 100, "returns", RETURNS_READ), RETURNS_READ)));

        var user = userWith(ORDERS_READ, RETURNS_READ);

        assertThat(registry.groupsVisibleTo(user, authorization()))
                .extracting(AdminNavigationGroup::id)
                .containsExactly("business");
        assertThat(registry.pagesVisibleTo(user, authorization()))
                .extracting(AdminPage::route)
                .containsExactly("returns", "orders");
        assertThat(registry.pages()).extracting(AdminPage::route)
                .containsExactly("invoices", "returns", "orders");
    }

    @Test
    void reportsBothModulesWhenRoutesCollide() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("business", "navigation.business", 100),
                        page("orders.list", "business", 100, "work", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("finance", "navigation.finance", 100),
                        page("invoices.list", "finance", 100, "work", INVOICES_READ), INVOICES_READ))))
                .withMessageContaining("work")
                .withMessageContaining("orders")
                .withMessageContaining("invoices");
    }

    @Test
    void rejectsIncompatibleNavigationGroupReuse() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdminModuleRegistry(List.of(
                module("orders", new AdminNavigationGroup("business", "navigation.business", 100),
                        page("orders.list", "business", 100, "orders", ORDERS_READ), ORDERS_READ),
                module("invoices", new AdminNavigationGroup("business", "navigation.finance", 100),
                        page("invoices.list", "business", 100, "invoices", INVOICES_READ), INVOICES_READ))))
                .withMessageContaining("business")
                .withMessageContaining("orders")
                .withMessageContaining("invoices");
    }

    private static AdminModule module(String id, AdminNavigationGroup group, AdminPage page, PermissionCode permission) {
        return AdminModule.of(id, List.of(group), List.of(page), Set.of(permission),
                List.of(new AdminMessageBundle(id, id + ".i18n.messages")));
    }

    private static AdminPage page(String id, String groupId, int order, String route, PermissionCode permission) {
        return new AdminPage(id, groupId, id + ".title", id + ".intent", "briefcase", order, route, permission,
                TestView.class);
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
