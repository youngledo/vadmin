package io.github.youngledo.vadmin.springflow;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringSecurityAutoConfiguration;

import io.github.youngledo.vadmin.contracts.auth.PermissionCatalog;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.flow.navigation.AdminHostLayout;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminModuleRegistry;
import io.github.youngledo.vadmin.springflow.navigation.AdminModuleRouteRegistrar;
import io.github.youngledo.vadmin.springflow.navigation.AdminModuleNavigationAccessChecker;
import io.github.youngledo.vadmin.springflow.navigation.AdminModuleAccessCheckDecisionResolver;
import io.github.youngledo.vadmin.springflow.navigation.SpringAdminModuleAssembler;
import io.github.youngledo.vadmin.springflow.i18n.AdminLocalePreference;
import io.github.youngledo.vadmin.springflow.i18n.AdminMessageBundleValidator;
import io.github.youngledo.vadmin.springflow.i18n.CompositeAdminI18NProvider;

/** Spring Boot assembly for Flow administration modules hosted by an application layout. */
@AutoConfiguration
@AutoConfigureBefore({SpringBootAutoConfiguration.class, SpringSecurityAutoConfiguration.class})
@ConditionalOnBean(AdminHostLayout.class)
public class AdminFlowAutoConfiguration {
    static final String ADMIN_MODULE_PERMISSION_CATALOG_BEAN_NAME = "adminModulePermissionCatalog";

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

    /**
     * Publishes the authoritative permission catalog assembled from all administration modules.
     * Hosts must remove legacy catalog beans before enabling an {@link AdminHostLayout}.
     */
    @Bean(name = ADMIN_MODULE_PERMISSION_CATALOG_BEAN_NAME)
    PermissionCatalog adminModulePermissionCatalog(SpringAdminModuleAssembler assembler) {
        return assembler.permissionCatalog();
    }

    @Bean
    static BeanFactoryPostProcessor adminModulePermissionCatalogGuard() {
        return beanFactory -> {
            var hostCatalogs = java.util.Arrays.stream(beanFactory.getBeanNamesForType(PermissionCatalog.class, false, false))
                    .filter(name -> !ADMIN_MODULE_PERMISSION_CATALOG_BEAN_NAME.equals(name))
                    .toList();
            if (!hostCatalogs.isEmpty()) {
                throw new BeanDefinitionStoreException("A host PermissionCatalog cannot be used with assembled Flow "
                        + "administration modules. Remove host catalog bean(s) " + hostCatalogs
                        + " and use '" + ADMIN_MODULE_PERMISSION_CATALOG_BEAN_NAME + "'.");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    I18NProvider adminI18NProvider(AdminModuleRegistry modules, List<AdminModule> moduleDescriptors) {
        var bundles = new java.util.ArrayList<AdminMessageBundle>();
        bundles.add(new AdminMessageBundle("flow", "i18n.flow"));
        bundles.addAll(modules.messageBundles());
        AdminMessageBundleValidator.validate(bundles, moduleDescriptors);
        return new CompositeAdminI18NProvider(bundles);
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
    @ConditionalOnMissingBean
    @ConditionalOnBean({CurrentUserProvider.class, AuthorizationService.class})
    NavigationAccessChecker adminModuleNavigationAccessChecker(AdminModuleRegistry modules, CurrentUserProvider currentUser,
                                                                AuthorizationService authorization) {
        return new AdminModuleNavigationAccessChecker(modules, currentUser, authorization);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(NavigationAccessChecker.class)
    NavigationAccessControlConfigurer adminNavigationAccessControlConfigurer(
            @Qualifier("adminModuleNavigationAccessChecker") NavigationAccessChecker moduleChecker) {
        return new NavigationAccessControlConfigurer()
                .withAnnotatedViewAccessChecker()
                .withNavigationAccessChecker(moduleChecker)
                .withDecisionResolver(new AdminModuleAccessCheckDecisionResolver(moduleChecker));
    }

    @Bean
    VaadinServiceInitListener adminModuleRouteRegistration(AdminModuleRouteRegistrar registrar,
                                                           AdminLocalePreference localePreference) {
        return event -> {
            registrar.register(event.getSource().getContext());
            event.getSource().addUIInitListener(uiEvent -> {
                var ui = uiEvent.getUI();
                ui.setLocale(localePreference.selectInitialLocale(browserLocales(ui.getLocale())));
            });
        };
    }

    private static List<Locale> browserLocales(Locale uiLocale) {
        var request = VaadinServletRequest.getCurrent();
        if (request != null) {
            var requestedLocales = request.getLocales();
            if (requestedLocales != null) {
                var locales = Collections.list(requestedLocales);
                if (!locales.isEmpty()) {
                    return locales;
                }
            }
        }
        return uiLocale == null ? List.of() : List.of(uiLocale);
    }
}
