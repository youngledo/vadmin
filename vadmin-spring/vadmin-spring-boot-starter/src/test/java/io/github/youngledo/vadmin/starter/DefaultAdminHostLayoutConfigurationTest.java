package io.github.youngledo.vadmin.starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.applayout.AppLayout;
import io.github.youngledo.vadmin.flow.navigation.AdminHostLayout;
import io.github.youngledo.vadmin.starter.views.DefaultMainLayout;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class DefaultAdminHostLayoutConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DefaultAdminHostLayoutConfiguration.class));

    @Test
    void providesTheDefaultHostLayoutWhenTheConsumerDoesNotProvideOne() {
        context.run(application -> assertThat(application.getBean(AdminHostLayout.class).layoutType())
                .isEqualTo(DefaultMainLayout.class));
    }

    @Test
    void backsOffWhenTheConsumerProvidesItsOwnHostLayout() {
        context.withUserConfiguration(ConsumerHostConfiguration.class).run(application ->
                assertThat(application.getBean(AdminHostLayout.class).layoutType()).isEqualTo(ConsumerLayout.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerHostConfiguration {
        @Bean
        AdminHostLayout consumerHostLayout() {
            return new AdminHostLayout(ConsumerLayout.class);
        }
    }

    static class ConsumerLayout extends AppLayout {
    }
}
