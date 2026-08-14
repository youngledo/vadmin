package io.github.vaadinadminstarter.starter;

import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.springflow.AdminFlowAutoConfiguration;
import io.github.vaadinadminstarter.starter.theme.AdminAppearanceProperties;
import io.github.vaadinadminstarter.starter.views.DefaultMainLayout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureBefore(AdminFlowAutoConfiguration.class)
@EnableConfigurationProperties(AdminAppearanceProperties.class)
public class DefaultAdminHostLayoutConfiguration {
    @Bean
    @ConditionalOnMissingBean(AdminHostLayout.class)
    AdminHostLayout adminHostLayout() {
        return new AdminHostLayout(DefaultMainLayout.class);
    }

}
