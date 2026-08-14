package io.github.youngledo.vadmin.springflow;

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

import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.flow.navigation.AdminHostLayout;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminNavigationGroup;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;

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

    @Test
    void rejectsAMessageBundleWithoutAChineseDefaultResourceAtStartup() {
        contextRunner.withUserConfiguration(MissingBundleConfiguration.class).run(context ->
                assertThat(context.getStartupFailure())
                        .hasMessageContaining("module 'missing'")
                        .hasMessageContaining("base name 'i18n.missing'"));
    }

    @Test
    void rejectsAMetadataKeyMissingFromTheChineseDefaultResourceAtStartup() {
        contextRunner.withUserConfiguration(IncompleteBundleConfiguration.class).run(context ->
                assertThat(context.getStartupFailure())
                        .hasMessageContaining("module 'incomplete'")
                        .hasMessageContaining("key 'incomplete.list.title'")
                        .hasMessageContaining("base name 'i18n.incomplete'"));
    }

    @Configuration(proxyBeanMethods = false)
    static class ModuleConfiguration {
        @Bean
        AdminHostLayout adminHostLayout() {
            return new AdminHostLayout(TestLayout.class);
        }

        @Bean
        AdminModule sampleModule() {
            return new AdminModule("sample",
                    List.of(new AdminNavigationGroup("sample", "sample.navigation", 100)),
                    List.of(new AdminPage("sample.list", "sample", "sample.list.title", "sample.list.intent",
                            "briefcase", 100, "sample", PermissionCode.of("sample:record:read"), TestView.class)),
                    Set.of(PermissionCode.of("sample:record:read")),
                    List.of(new AdminMessageBundle("sample", "i18n.sample")));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class LegacyCatalogConfiguration {
        @Bean
        PermissionCatalog legacyPermissionCatalog() {
            return new PermissionCatalog(Set.of(PermissionCode.of("legacy:catalog:read")));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MissingBundleConfiguration {
        @Bean
        AdminModule missingModule() {
            return module("missing", "i18n.missing");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class IncompleteBundleConfiguration {
        @Bean
        AdminModule incompleteModule() {
            return module("incomplete", "i18n.incomplete");
        }
    }

    private static AdminModule module(String moduleId, String bundleBaseName) {
        var permission = PermissionCode.of(moduleId + ":record:read");
        return new AdminModule(moduleId,
                List.of(new AdminNavigationGroup("business-" + moduleId, moduleId + ".navigation", 100)),
                List.of(new AdminPage(moduleId + ".list", "business-" + moduleId, moduleId + ".list.title",
                        moduleId + ".list.intent", "briefcase", 100, moduleId, permission, TestView.class)),
                Set.of(permission), List.of(new AdminMessageBundle(moduleId, bundleBaseName)));
    }

    static final class TestLayout extends VerticalLayout implements RouterLayout {
    }

    static final class TestView extends Div {
    }
}
