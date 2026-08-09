package io.github.vaadinadminstarter.app.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.vaadinadminstarter.app.views.AuditView;
import io.github.vaadinadminstarter.app.views.CustomersView;
import io.github.vaadinadminstarter.app.views.PermissionsView;
import io.github.vaadinadminstarter.app.views.RolesView;
import io.github.vaadinadminstarter.app.views.UsersView;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.springsecurity.auth.SpringAuthorizationService;

class ReferenceAdminModulesTest {
    @Test
    void contributesBuiltInPagesInVisibleNavigationOrder() {
        var descriptors = new ReferenceAdminModules();
        var registry = new AdminModuleRegistry(java.util.List.of(
                descriptors.systemAdministration(), descriptors.customerAdministration()));
        var administrator = new CurrentUser(UUID.randomUUID(), "admin", registry.permissionCatalog(), 0);

        assertThat(registry.groupsVisibleTo(administrator, new SpringAuthorizationService()))
                .extracting(group -> group.id())
                .containsExactly("system", "customers");
        assertThat(registry.pagesVisibleTo(administrator, new SpringAuthorizationService()))
                .extracting(page -> page.route())
                .containsExactly("users", "roles", "permissions", "audit", "customers");
    }

    @Test
    void usesTheProtectedViewPermissionAsThePagePermissionSource() {
        var descriptors = new ReferenceAdminModules();
        var registry = new AdminModuleRegistry(java.util.List.of(
                descriptors.systemAdministration(), descriptors.customerAdministration()));
        Map<Class<?>, io.github.vaadinadminstarter.contracts.auth.PermissionCode> permissionsByView = Map.of(
                UsersView.class, UsersView.REQUIRED_PERMISSION,
                RolesView.class, RolesView.REQUIRED_PERMISSION,
                PermissionsView.class, PermissionsView.REQUIRED_PERMISSION,
                AuditView.class, AuditView.REQUIRED_PERMISSION,
                CustomersView.class, CustomersView.REQUIRED_PERMISSION);

        assertThat(registry.pages()).allSatisfy(page -> assertThat(permissionsByView)
                .containsEntry(page.viewType(), page.requiredPermission()));
        assertThat(permissionsByView.keySet()).containsExactlyInAnyOrderElementsOf(registry.pages().stream()
                .map(page -> page.viewType())
                .collect(Collectors.toSet()));
    }
}
