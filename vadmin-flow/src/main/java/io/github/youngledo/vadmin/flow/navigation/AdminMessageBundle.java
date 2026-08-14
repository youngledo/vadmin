package io.github.youngledo.vadmin.flow.navigation;

/** Immutable descriptor for translation resources contributed by an administration module. */
public record AdminMessageBundle(String moduleId, String baseName) {
    public AdminMessageBundle {
        moduleId = AdminNavigationGroup.requireText(moduleId, "moduleId");
        baseName = AdminNavigationGroup.requireText(baseName, "baseName");
    }
}
