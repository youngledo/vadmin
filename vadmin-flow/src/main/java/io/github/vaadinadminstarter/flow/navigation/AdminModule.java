package io.github.vaadinadminstarter.flow.navigation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

/**
 * Spring-independent declaration of an administration module and the metadata it contributes.
 */
public record AdminModule(String moduleId, List<AdminNavigationGroup> navigationGroups, List<AdminPage> pages,
                          Set<PermissionCode> permissions, List<AdminMessageBundle> messageBundles) {
    public AdminModule {
        moduleId = AdminNavigationGroup.requireText(moduleId, "moduleId");
        navigationGroups = List.copyOf(Objects.requireNonNull(navigationGroups, "navigationGroups"));
        pages = List.copyOf(Objects.requireNonNull(pages, "pages"));
        permissions = Set.copyOf(Objects.requireNonNull(permissions, "permissions"));
        messageBundles = List.copyOf(Objects.requireNonNull(messageBundles, "messageBundles"));

        if (navigationGroups.stream().anyMatch(Objects::isNull)
                || pages.stream().anyMatch(Objects::isNull)
                || permissions.stream().anyMatch(Objects::isNull)
                || messageBundles.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("module metadata must not contain null elements");
        }
        if (navigationGroups.isEmpty()) {
            throw new IllegalArgumentException("Module '" + moduleId + "' must declare at least one navigation group");
        }
        var declaredModuleId = moduleId;
        var declaredPermissions = permissions;
        var groupIds = navigationGroups.stream().map(AdminNavigationGroup::id).collect(java.util.stream.Collectors.toSet());
        if (groupIds.size() != navigationGroups.size()) {
            throw new IllegalArgumentException("Module '" + moduleId + "' declares duplicate navigation group ids");
        }
        if (pages.stream().anyMatch(page -> !groupIds.contains(page.groupId()))) {
            throw new IllegalArgumentException("Module '" + moduleId + "' has a page assigned to an undeclared navigation group");
        }
        if (pages.stream().anyMatch(page -> !declaredPermissions.contains(page.requiredPermission()))) {
            throw new IllegalArgumentException("Module '" + moduleId + "' has a page permission that it does not declare");
        }
        if (messageBundles.stream().anyMatch(bundle -> !declaredModuleId.equals(bundle.moduleId()))) {
            throw new IllegalArgumentException("Module '" + moduleId + "' has a message bundle for a different module");
        }
        var namespace = moduleId + ".";
        for (var page : pages) {
            requireModuleKey(moduleId, namespace, "page title key", page.titleKey());
            requireModuleKey(moduleId, namespace, "page intent key", page.intentKey());
        }
    }

    public static AdminModule of(String moduleId, List<AdminNavigationGroup> navigationGroups, List<AdminPage> pages,
                                 Set<PermissionCode> permissions, List<AdminMessageBundle> messageBundles) {
        return new AdminModule(moduleId, navigationGroups, pages, permissions, messageBundles);
    }

    private static void requireModuleKey(String moduleId, String namespace, String kind, String key) {
        if (!key.startsWith(namespace)) {
            throw new IllegalArgumentException("Module '" + moduleId + "' has " + kind + " '" + key
                    + "' outside namespace '" + namespace + "'");
        }
    }
}
