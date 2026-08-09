package io.github.vaadinadminstarter.flow.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

/** Validated, deterministic aggregation of all administration module metadata. */
public final class AdminModuleRegistry {
    private static final Comparator<AdminNavigationGroup> GROUP_ORDER = Comparator
            .comparingInt(AdminNavigationGroup::order)
            .thenComparing(AdminNavigationGroup::id);

    private final List<AdminNavigationGroup> groups;
    private final List<AdminPage> pages;
    private final Set<PermissionCode> permissionCatalog;
    private final List<AdminMessageBundle> messageBundles;
    private final Map<String, AdminNavigationGroup> groupsById;

    public AdminModuleRegistry(Collection<AdminModule> modules) {
        Objects.requireNonNull(modules, "modules");
        var moduleList = List.copyOf(modules);
        if (moduleList.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("modules must not contain null elements");
        }

        var moduleIds = new LinkedHashMap<String, String>();
        var groupOwners = new LinkedHashMap<String, String>();
        var groupMetadata = new LinkedHashMap<String, AdminNavigationGroup>();
        var pageOwners = new HashMap<String, String>();
        var routeOwners = new HashMap<String, String>();
        var permissionOwners = new HashMap<PermissionCode, String>();
        var bundleOwners = new HashMap<String, String>();
        var allPages = new ArrayList<AdminPage>();
        var allBundles = new ArrayList<AdminMessageBundle>();
        var allPermissions = new java.util.LinkedHashSet<PermissionCode>();

        for (var module : moduleList) {
            rejectDuplicate(moduleIds, module.moduleId(), module.moduleId(), "module id");
            for (var group : module.navigationGroups()) {
                var existing = groupMetadata.putIfAbsent(group.id(), group);
                if (existing == null) {
                    groupOwners.put(group.id(), module.moduleId());
                } else if (!existing.equals(group)) {
                    throw collision("navigation group", group.id(), groupOwners.get(group.id()), module.moduleId());
                }
            }
            for (var page : module.pages()) {
                rejectDuplicate(pageOwners, page.pageId(), module.moduleId(), "page id");
                rejectDuplicate(routeOwners, page.route(), module.moduleId(), "route");
                allPages.add(page);
            }
            for (var permission : module.permissions()) {
                rejectDuplicate(permissionOwners, permission, module.moduleId(), "permission");
                allPermissions.add(permission);
            }
            for (var bundle : module.messageBundles()) {
                rejectDuplicate(bundleOwners, bundle.baseName(), module.moduleId(), "message bundle base name");
                allBundles.add(bundle);
            }
        }

        groups = groupMetadata.values().stream().sorted(GROUP_ORDER).toList();
        groupsById = Map.copyOf(groupMetadata);
        pages = allPages.stream().sorted(pageComparator(groupsById)).toList();
        permissionCatalog = Set.copyOf(allPermissions);
        messageBundles = allBundles.stream().sorted(Comparator.comparing(AdminMessageBundle::baseName)
                .thenComparing(AdminMessageBundle::moduleId)).toList();
    }

    public List<AdminPage> pages() {
        return pages;
    }

    public List<AdminMessageBundle> messageBundles() {
        return messageBundles;
    }

    public Set<PermissionCode> permissionCatalog() {
        return permissionCatalog;
    }

    public List<AdminPage> pagesVisibleTo(CurrentUser user, AuthorizationService authorization) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(authorization, "authorization");
        return pages.stream().filter(page -> authorization.hasPermission(user, page.requiredPermission())).toList();
    }

    public List<AdminNavigationGroup> groupsVisibleTo(CurrentUser user, AuthorizationService authorization) {
        var visibleGroupIds = pagesVisibleTo(user, authorization).stream()
                .map(AdminPage::groupId)
                .collect(Collectors.toSet());
        return groups.stream().filter(group -> visibleGroupIds.contains(group.id())).toList();
    }

    private static Comparator<AdminPage> pageComparator(Map<String, AdminNavigationGroup> groups) {
        return Comparator.comparing((AdminPage page) -> groups.get(page.groupId()), GROUP_ORDER)
                .thenComparingInt(AdminPage::order)
                .thenComparing(AdminPage::pageId);
    }

    private static <T> void rejectDuplicate(Map<T, String> owners, T value, String moduleId, String kind) {
        var firstOwner = owners.putIfAbsent(value, moduleId);
        if (firstOwner != null) {
            throw collision(kind, String.valueOf(value), firstOwner, moduleId);
        }
    }

    private static IllegalArgumentException collision(String kind, String value, String firstModuleId, String secondModuleId) {
        return new IllegalArgumentException("Duplicate " + kind + " '" + value + "' contributed by modules '"
                + firstModuleId + "' and '" + secondModuleId + "'");
    }
}
