package io.github.youngledo.vadmin.flow.navigation;

import java.io.Serializable;

/** Immutable descriptor for translation resources contributed by an administration module. */
public record AdminMessageBundle(String moduleId, String baseName) implements Serializable {
    public AdminMessageBundle {
        moduleId = AdminNavigationGroup.requireText(moduleId, "moduleId");
        baseName = AdminNavigationGroup.requireText(baseName, "baseName");
    }
}
