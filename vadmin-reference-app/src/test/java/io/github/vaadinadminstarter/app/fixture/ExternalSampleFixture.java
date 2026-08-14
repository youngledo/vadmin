package io.github.vaadinadminstarter.app.fixture;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminNavigationGroup;
import io.github.vaadinadminstarter.flow.navigation.AdminPage;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.boot.test.context.TestConfiguration;

/** Test-only consumer module proving the starter's external module contract. */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalSampleFixture {
    static final PermissionCode READ = PermissionCode.of("sample:record:read");

    @Bean
    AdminModule externalSampleModule() {
        return new AdminModule("sample",
                List.of(new AdminNavigationGroup("sample", "sample.navigation", 500)),
                List.of(new AdminPage("sample.records", "sample", "sample.records.title", "sample.records.intent",
                        "briefcase", 500, "sample", READ, ExternalSampleScreen.class)),
                Set.of(READ), List.of(new AdminMessageBundle("sample", "i18n.external-sample")));
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    ExternalSampleScreen externalSampleScreen() {
        return new ExternalSampleScreen();
    }

    public static final class ExternalSampleScreen extends VerticalLayout {
        ExternalSampleScreen() {
            add(new H2("External sample records"));
            getElement().setAttribute("data-testid", "external-sample-workspace");
        }
    }
}
