package com.example.orders.admin;

import java.util.List;
import java.util.Set;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminNavigationGroup;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;

/** Metadata contributed by the independent orders administration module. */
public final class OrdersAdminModule {
    public static final PermissionCode ORDERS_READ = PermissionCode.of("orders:order:read");

    private OrdersAdminModule() {
    }

    public static AdminModule create() {
        return AdminModule.of("orders",
                List.of(new AdminNavigationGroup("business", "orders.nav.group", 300)),
                List.of(new AdminPage("orders.list", "business", "orders.title", "orders.intent", "shopping-cart",
                        100, "orders", ORDERS_READ, OrdersView.class)),
                Set.of(ORDERS_READ),
                List.of(new AdminMessageBundle("orders", "orders.i18n.messages")));
    }
}
