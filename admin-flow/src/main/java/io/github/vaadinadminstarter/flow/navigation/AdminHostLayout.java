package io.github.vaadinadminstarter.flow.navigation;

import java.util.Objects;

import com.vaadin.flow.router.RouterLayout;

/** Flow-only host layout declaration used when externally packaged pages are registered. */
public record AdminHostLayout(Class<? extends RouterLayout> layoutType) {
    public AdminHostLayout {
        layoutType = Objects.requireNonNull(layoutType, "layoutType");
    }
}
