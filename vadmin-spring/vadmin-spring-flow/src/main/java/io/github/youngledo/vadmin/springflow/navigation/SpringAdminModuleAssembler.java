package io.github.youngledo.vadmin.springflow.navigation;

import java.util.List;
import java.util.Objects;

import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;

/** Assembles Spring-contributed module descriptors into the Spring-free registry. */
public final class SpringAdminModuleAssembler {
    private final AdminModuleRegistry registry;

    public SpringAdminModuleAssembler(List<AdminModule> modules) {
        registry = new AdminModuleRegistry(Objects.requireNonNull(modules, "modules"));
    }

    public AdminModuleRegistry registry() {
        return registry;
    }

    public PermissionCatalog permissionCatalog() {
        return new PermissionCatalog(registry.permissionCatalog());
    }

    public List<AdminMessageBundle> messageBundles() {
        return registry.messageBundles();
    }
}
