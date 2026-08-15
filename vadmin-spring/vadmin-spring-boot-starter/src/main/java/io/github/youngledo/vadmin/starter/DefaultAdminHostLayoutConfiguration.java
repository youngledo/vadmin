package io.github.youngledo.vadmin.starter;

import io.github.youngledo.vadmin.flow.navigation.AdminHostLayout;
import io.github.youngledo.vadmin.springflow.AdminFlowAutoConfiguration;
import io.github.youngledo.vadmin.starter.brand.AdminBrandProperties;
import io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties;
import io.github.youngledo.vadmin.starter.views.DefaultMainLayout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureBefore(AdminFlowAutoConfiguration.class)
@EnableConfigurationProperties({AdminAppearanceProperties.class, AdminBrandProperties.class})
public class DefaultAdminHostLayoutConfiguration {
    @Bean
    @ConditionalOnMissingBean(AdminHostLayout.class)
    AdminHostLayout adminHostLayout() {
        return new AdminHostLayout(DefaultMainLayout.class);
    }

}
