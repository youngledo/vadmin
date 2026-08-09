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
import java.lang.reflect.Field;
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
}
