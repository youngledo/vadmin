package io.github.vaadinadminstarter.springflow;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;

import io.github.vaadinadminstarter.contracts.auth.PermissionCatalog;
import io.github.vaadinadminstarter.flow.navigation.AdminHostLayout;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.flow.navigation.AdminModuleRegistry;
import io.github.vaadinadminstarter.springflow.navigation.AdminModuleRouteRegistrar;
import io.github.vaadinadminstarter.springflow.navigation.SpringAdminModuleAssembler;
import io.github.vaadinadminstarter.springflow.i18n.AdminLocalePreference;
import io.github.vaadinadminstarter.springflow.i18n.CompositeAdminI18NProvider;

/** Spring Boot assembly for Flow administration modules hosted by an application layout. */
@AutoConfiguration
@AutoConfigureBefore(SpringBootAutoConfiguration.class)
@ConditionalOnBean(AdminHostLayout.class)
public class AdminFlowAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    SpringAdminModuleAssembler springAdminModuleAssembler(List<AdminModule> modules) {
        return new SpringAdminModuleAssembler(modules);
    }

    @Bean
    @ConditionalOnMissingBean
    AdminModuleRegistry adminModuleRegistry(SpringAdminModuleAssembler assembler) {
        return assembler.registry();
    }

    @Bean
    @ConditionalOnMissingBean
    PermissionCatalog permissionCatalog(SpringAdminModuleAssembler assembler) {
        return assembler.permissionCatalog();
    }

    @Bean
    @ConditionalOnMissingBean
    I18NProvider adminI18NProvider(AdminModuleRegistry modules) {
        return new CompositeAdminI18NProvider(modules.messageBundles());
    }

    @Bean
    @ConditionalOnMissingBean
    AdminLocalePreference adminLocalePreference() {
        return new AdminLocalePreference();
    }

    @Bean
    @ConditionalOnMissingBean
    AdminModuleRouteRegistrar adminModuleRouteRegistrar(AdminModuleRegistry modules, AdminHostLayout hostLayout) {
        return new AdminModuleRouteRegistrar(modules, hostLayout);
    }

    @Bean
    VaadinServiceInitListener adminModuleRouteRegistration(AdminModuleRouteRegistrar registrar) {
        return event -> registrar.register(event.getSource().getContext());
    }
}
