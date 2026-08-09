package io.github.vaadinadminstarter.flow.navigation;

import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/** Stable catalog of navigation icons that administration modules may declare. */
public final class AdminIconCatalog {
    private static final Map<String, VaadinIcon> ICONS = Map.of(
            "briefcase", VaadinIcon.BRIEFCASE,
            "clock", VaadinIcon.CLOCK,
            "history", VaadinIcon.TIME_BACKWARD,
            "key", VaadinIcon.KEY,
            "shield", VaadinIcon.SHIELD,
            "shopping-cart", VaadinIcon.CART,
            "users", VaadinIcon.USERS);

    private AdminIconCatalog() {
    }

    public static boolean isSupported(String iconKey) {
        return ICONS.containsKey(Objects.requireNonNull(iconKey, "iconKey"));
    }

    public static Icon create(String iconKey) {
        var icon = ICONS.get(Objects.requireNonNull(iconKey, "iconKey"));
        if (icon == null) {
            throw new IllegalArgumentException("Unsupported administration icon key '" + iconKey + "'");
        }
        return icon.create();
    }
}
