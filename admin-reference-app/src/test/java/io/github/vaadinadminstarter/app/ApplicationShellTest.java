package io.github.vaadinadminstarter.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orders.admin.OrdersView;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import io.github.vaadinadminstarter.app.views.AuditView;
import io.github.vaadinadminstarter.app.views.CustomersView;
import io.github.vaadinadminstarter.app.views.MainLayout;
import io.github.vaadinadminstarter.app.views.PermissionsView;
import io.github.vaadinadminstarter.app.views.RolesView;
import io.github.vaadinadminstarter.app.views.UsersView;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApplicationShellTest {
    @Test
    void registersTheThemeOnADedicatedApplicationShell() {
        assertThat(Application.class.isAnnotationPresent(Theme.class)).isFalse();

        assertThat(AppShellConfigurator.class.isAssignableFrom(ApplicationShell.class)).isTrue();
        assertThat(ApplicationShell.class.getAnnotation(Theme.class)).isNotNull();
        assertThat(ApplicationShell.class.getAnnotation(Theme.class).value()).isEqualTo("admin-theme");
    }

    @Test
    void declaresEveryDynamicallyRegisteredAdministrationViewForTheProductionBundle() {
        assertThat(MainLayout.class.getAnnotationsByType(Uses.class))
                .extracting(Uses::value)
                .containsExactlyInAnyOrder(
                        UsersView.class,
                        RolesView.class,
                        PermissionsView.class,
                        AuditView.class,
                        CustomersView.class,
                        OrdersView.class);
    }

    @Test
    void keepsInterfacePreferencesInDedicatedShellUtilityMenus() {
        assertThat(MainLayout.class.getDeclaredFields())
                .extracting(Field::getName)
                .contains("languageMenu", "appearanceMenu")
                .doesNotContain("languageSelector", "themeModeItem");
        assertThat(MainLayout.class.getDeclaredFields())
                .filteredOn(field -> field.getName().equals("languageMenu") || field.getName().equals("appearanceMenu"))
                .extracting(Field::getType)
                .containsOnly(MenuBar.class);
    }

    @Test
    void usesProfileNeutralIconsForTheShellAndReferenceWorkflows() throws IOException {
        assertUsesSemanticIcons("src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java",
                "AdminIcon.of(AdminIconName.GLOBE)", "AdminIconCatalog.createAdminIcon(page.iconKey())");
        assertUsesSemanticIcons("src/main/java/io/github/vaadinadminstarter/app/views/UsersView.java",
                "AdminIcon.of(AdminIconName.ADD)");
        assertUsesSemanticIcons("src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java",
                "AdminIcon.of(AdminIconName.ATTACHMENT)");
        assertUsesSemanticIcons("src/main/java/io/github/vaadinadminstarter/app/views/RolesView.java",
                "AdminIcon.of(AdminIconName.EYE)");
        assertUsesSemanticIcons("../admin-examples/admin-example-orders/src/main/java/com/example/orders/admin/OrdersView.java",
                "AdminIcon.of(AdminIconName.EYE)");
    }

    private void assertUsesSemanticIcons(String relativePath, String... expectedUsages) throws IOException {
        var source = Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);

        assertThat(source).contains("import io.github.vaadinadminstarter.flow.navigation.AdminIcon;");
        assertThat(source).doesNotContain("import com.vaadin.flow.component.icon.VaadinIcon;");
        assertThat(source).contains(expectedUsages);
    }
}
