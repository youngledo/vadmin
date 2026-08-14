package io.github.youngledo.vadmin.flow.navigation;

import java.util.Objects;

/** Immutable metadata for a top-level administration navigation group. */
public record AdminNavigationGroup(String id, String titleKey, int order) {
    public AdminNavigationGroup {
        id = requireText(id, "id");
        titleKey = requireText(titleKey, "titleKey");
    }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
