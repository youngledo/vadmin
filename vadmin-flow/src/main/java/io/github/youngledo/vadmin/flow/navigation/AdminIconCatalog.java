package io.github.youngledo.vadmin.flow.navigation;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.icon.Icon;

/** Stable catalog of navigation icons that administration modules may declare. */
public final class AdminIconCatalog {
    private static final Map<String, AdminIconName> ICONS = Arrays.stream(AdminIconName.values())
            .collect(Collectors.toUnmodifiableMap(AdminIconName::cssValue, icon -> icon));

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
