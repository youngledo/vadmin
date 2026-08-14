package io.github.youngledo.vadmin.starter.modules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.starter.views.AuditView;
import io.github.youngledo.vadmin.starter.views.PermissionsView;
import io.github.youngledo.vadmin.starter.views.RolesView;
import io.github.youngledo.vadmin.starter.views.UsersView;
import org.junit.jupiter.api.Test;

class SystemAdministrationModuleConfigurationTest {
    @Test
    void contributesAllSystemPagesAndTheirDeclaredPermissions() {
        var module = new SystemAdministrationModuleConfiguration().systemAdministration();

        assertThat(module.moduleId()).isEqualTo("system");
        assertThat(module.pages()).extracting(page -> page.route())
                .containsExactly("users", "roles", "permissions", "audit");
        assertThat(module.pages()).extracting(page -> page.viewType().getName())
                .containsExactly(UsersView.class.getName(), RolesView.class.getName(), PermissionsView.class.getName(),
                        AuditView.class.getName());
        assertThat(module.permissions()).contains(
                UsersView.REQUIRED_PERMISSION, RolesView.REQUIRED_PERMISSION,
                PermissionsView.REQUIRED_PERMISSION, AuditView.REQUIRED_PERMISSION);
        assertThat(module.messageBundles()).extracting(bundle -> bundle.baseName()).containsExactly("i18n.system");
    }
}
