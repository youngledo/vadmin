package io.github.vaadinadminstarter.flow.navigation;

import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.icon.Icon;

/** Stable catalog of navigation icons that administration modules may declare. */
public final class AdminIconCatalog {
    private static final Map<String, AdminIconName> ICONS = Map.of(
            "briefcase", AdminIconName.BRIEFCASE,
            "clock", AdminIconName.CLOCK,
            "history", AdminIconName.HISTORY,
            "key", AdminIconName.KEY,
            "shield", AdminIconName.SHIELD,
            "shopping-cart", AdminIconName.SHOPPING_CART,
            "users", AdminIconName.USERS);

    private AdminIconCatalog() {
    }

    public static boolean isSupported(String iconKey) {
        return ICONS.containsKey(Objects.requireNonNull(iconKey, "iconKey"));
    }

    public static Icon create(String iconKey) {
        return iconName(iconKey).vaadinIcon().create();
    }

    public static AdminIcon createAdminIcon(String iconKey) {
        return AdminIcon.of(iconName(iconKey));
    }

    public static AdminIconName iconName(String iconKey) {
        var icon = ICONS.get(Objects.requireNonNull(iconKey, "iconKey"));
        if (icon == null) throw new IllegalArgumentException("Unsupported administration icon key '" + iconKey + "'");
        return icon;
    }
}
