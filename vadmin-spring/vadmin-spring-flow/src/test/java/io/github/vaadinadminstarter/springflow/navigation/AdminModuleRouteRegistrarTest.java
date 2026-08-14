package io.github.vaadinadminstarter.springflow.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;

import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminNavigationGroup;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.sample.ExternalSampleView;
import io.github.vaadinadminstarter.sample.SampleService;

@SpringBootTest(classes = AdminModuleRouteRegistrarTest.TestApplication.class)
class AdminModuleRouteRegistrarTest {
    @Autowired
    private AdminModuleRouteRegistrar registrar;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private AdminModuleRegistry modules;

    @Autowired
    private PermissionCatalog permissionCatalog;

    @Autowired
    private I18NProvider i18NProvider;

    @Test
    void registersAnExternalSpringViewUnderTheHostLayout() {
        var vaadinContext = new TestVaadinContext();

        registrar.register(vaadinContext);
        registrar.register(vaadinContext);

        var routes = RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(vaadinContext));
        assertThat(routes.getRoute("sample")).contains(ExternalSampleView.class);
        assertThat(routes.getAvailableRoutes())
                .filteredOn(route -> route.getTemplate().equals("sample"))
                .singleElement()
                .extracting(route -> route.getParentLayouts())
                .isEqualTo(List.of(TestHostLayout.class));
        assertThat(routes.getAvailableRoutes()).filteredOn(route -> route.getTemplate().equals("sample")).hasSize(1);
        assertThat(applicationContext.getBean(ExternalSampleView.class).service()).isSameAs(sampleService);
        assertThat(modules.pages()).extracting(AdminPage::route).containsExactly("sample");
        assertThat(modules.pages()).extracting(AdminPage::iconKey).containsExactly("briefcase");
        assertThat(permissionCatalog.all()).containsExactly(PermissionCode.of("sample:record:read"));
        assertThat(i18NProvider.getTranslation("sample.navigation", Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("示例管理");
        assertThat(i18NProvider.getTranslation("sample.list.title", Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("示例");
    }

    @Test
    void rejectsAnOccupiedRouteBeforeRegisteringAnyModulePage() {
        var vaadinContext = new TestVaadinContext();
        var routes = RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(vaadinContext));
        routes.setRoute("sample.history", OccupiedView.class);
        var atomicRegistrar = new AdminModuleRouteRegistrar(new AdminModuleRegistry(List.of(new AdminModule("sample",
                List.of(new AdminNavigationGroup("sample", "sample.navigation", 100)),
                List.of(
                        new AdminPage("sample.list", "sample", "sample.list.title", "sample.list.intent",
                                "briefcase", 100, "sample", PermissionCode.of("sample:record:read"), ExternalSampleView.class),
                        new AdminPage("sample.history", "sample", "sample.history.title", "sample.history.intent",
                                "history", 200, "sample.history", PermissionCode.of("sample:record:read"), HistoryView.class)),
                Set.of(PermissionCode.of("sample:record:read")),
                List.of(new AdminMessageBundle("sample", "sample.i18n.messages"))))), new AdminHostLayout(TestHostLayout.class));

        assertThatThrownBy(() -> atomicRegistrar.register(vaadinContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sample.history");

        assertThat(routes.getRoute("sample")).isEmpty();
        assertThat(routes.getRoute("sample.history")).contains(OccupiedView.class);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = SpringBootAutoConfiguration.class)
    @Import({TestModuleConfiguration.class, ExternalSampleView.class})
    static class TestApplication {
    }

    @Configuration(proxyBeanMethods = false)
    static class TestModuleConfiguration {
        @Bean
        AdminHostLayout adminHostLayout() {
            return new AdminHostLayout(TestHostLayout.class);
        }

        @Bean
        AdminModule sampleModule() {
            return new AdminModule("sample",
                    List.of(new AdminNavigationGroup("sample", "sample.navigation", 100)),
                    List.of(new AdminPage("sample.list", "sample", "sample.list.title", "sample.list.intent",
                            "briefcase", 100, "sample", PermissionCode.of("sample:record:read"), ExternalSampleView.class)),
                    Set.of(PermissionCode.of("sample:record:read")),
                    List.of(new AdminMessageBundle("sample", "sample.i18n.messages")));
        }

        @Bean
        SampleService sampleService() {
            return new SampleService();
        }
    }

    static final class TestHostLayout extends VerticalLayout implements RouterLayout {
    }

    static final class OccupiedView extends Div {
    }

    static final class HistoryView extends Div {
    }

    private static final class TestVaadinContext implements VaadinContext {
        private final Map<Class<?>, Object> attributes = new HashMap<>();

        @Override
        public <T> T getAttribute(Class<T> type, java.util.function.Supplier<T> defaultValueSupplier) {
            var value = attributes.get(type);
            if (value == null && defaultValueSupplier != null) {
                value = defaultValueSupplier.get();
                attributes.put(type, value);
            }
            return type.cast(value);
        }

        @Override
        public <T> void setAttribute(Class<T> type, T value) {
            if (value == null) {
                attributes.remove(type);
            } else {
                attributes.put(type, value);
            }
        }

        @Override
        public void removeAttribute(Class<?> type) {
            attributes.remove(type);
        }

        @Override
        public java.util.Enumeration<String> getContextParameterNames() {
            return Collections.emptyEnumeration();
        }

        @Override
        public String getContextParameter(String name) {
            return null;
        }
    }
}
