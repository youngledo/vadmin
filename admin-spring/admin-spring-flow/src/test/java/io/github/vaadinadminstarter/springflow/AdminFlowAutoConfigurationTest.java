package io.github.vaadinadminstarter.springflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;

import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminNavigationGroup;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;

class AdminFlowAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AdminFlowAutoConfiguration.class))
            .withUserConfiguration(ModuleConfiguration.class);

    @Test
    void rejectsAHostPermissionCatalogInsteadOfIgnoringModulePermissions() {
        contextRunner.withUserConfiguration(LegacyCatalogConfiguration.class).run(context ->
                assertThat(context.getStartupFailure())
                        .hasMessageContaining("host PermissionCatalog")
                        .hasMessageContaining("adminModulePermissionCatalog"));
    }

    @Configuration(proxyBeanMethods = false)
    static class ModuleConfiguration {
        @Bean
        AdminHostLayout adminHostLayout() {
            return new AdminHostLayout(TestLayout.class);
        }

        @Bean
        AdminModule ordersModule() {
            return new AdminModule("orders",
                    List.of(new AdminNavigationGroup("business", "orders.navigation.business", 100)),
                    List.of(new AdminPage("orders.list", "business", "orders.list.title", "orders.list.intent",
                            "briefcase", 100, "orders", PermissionCode.of("orders:order:read"), TestView.class)),
                    Set.of(PermissionCode.of("orders:order:read")),
                    List.of(new AdminMessageBundle("orders", "orders.i18n.messages")));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class LegacyCatalogConfiguration {
        @Bean
        PermissionCatalog legacyPermissionCatalog() {
            return new PermissionCatalog(Set.of(PermissionCode.of("legacy:catalog:read")));
        }
    }

    static final class TestLayout extends VerticalLayout implements RouterLayout {
    }

    static final class TestView extends Div {
    }
}
