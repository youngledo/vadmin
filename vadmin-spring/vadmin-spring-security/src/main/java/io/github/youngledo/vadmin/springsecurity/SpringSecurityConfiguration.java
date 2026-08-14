package io.github.youngledo.vadmin.springsecurity;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.github.youngledo.vadmin.contracts.auth.AuthorizationService;
import io.github.youngledo.vadmin.contracts.auth.CurrentUserProvider;
import io.github.youngledo.vadmin.contracts.auth.ExternalIdentityMapper;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.contracts.auth.LocalUserSessionLookup;
import io.github.youngledo.vadmin.springsecurity.auth.AuthenticationVersionFilter;
import io.github.youngledo.vadmin.springsecurity.auth.DiscardingOAuth2AuthorizedClientRepository;
import io.github.youngledo.vadmin.springsecurity.auth.LocalUserDetailsService;
import io.github.youngledo.vadmin.springsecurity.auth.LocalLoginAuthenticator;
import io.github.youngledo.vadmin.springsecurity.auth.OidcAccessDeniedFailureHandler;
import io.github.youngledo.vadmin.springsecurity.auth.OidcLocalUserAuthenticationSuccessHandler;
import io.github.youngledo.vadmin.springsecurity.auth.SecurityContextCurrentUserProvider;
import io.github.youngledo.vadmin.springsecurity.auth.SpringAuthorizationService;
import io.github.youngledo.vadmin.springsecurity.ui.LoginView;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(OidcLoginProperties.class)
public class SpringSecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    LocalUserDetailsService localUserDetailsService(LocalUserAccountLookup accountLookup) {
        return new LocalUserDetailsService(accountLookup);
    }

    @Bean
    DaoAuthenticationProvider localAuthenticationProvider(LocalUserDetailsService userDetailsService,
                                                          PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    LocalLoginAuthenticator localLoginAuthenticator(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return new LocalLoginAuthenticator(authenticationConfiguration.getAuthenticationManager(),
                new ChangeSessionIdAuthenticationStrategy(), new HttpSessionSecurityContextRepository());
    }

    @Bean
    AuthorizationService authorizationService() {
        return new SpringAuthorizationService();
    }

    @Bean
    CurrentUserProvider currentUserProvider() {
        return new SecurityContextCurrentUserProvider();
    }

    @Bean
    OidcLoginAvailability oidcLoginAvailability(
            ObjectProvider<ClientRegistrationRepository> registrations,
            ObjectProvider<ExternalIdentityMapper> identityMappers,
            OidcLoginProperties properties) {
        var registrationId = properties.registrationId();
        var registrationRepository = registrations.getIfAvailable();
        if (registrationRepository == null) {
            return new OidcLoginAvailability(false, registrationId);
        }

        List<ExternalIdentityMapper> mappers = identityMappers.orderedStream().toList();
        if (mappers.size() != 1) {
            throw new IllegalStateException("OIDC client registrations require exactly one ExternalIdentityMapper bean "
                    + "to resolve external subjects to existing local users");
        }
        if (registrationRepository.findByRegistrationId(registrationId) == null) {
            throw new IllegalStateException("OIDC client registrations must include the default registration id '"
                    + registrationId + "'");
        }
        return new OidcLoginAvailability(true, registrationId);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider,
                                            LocalUserSessionLookup sessionLookup,
                                            LocalUserAccountLookup accountLookup,
                                            OidcLoginAvailability oidcLoginAvailability,
                                            ObjectProvider<ExternalIdentityMapper> identityMappers,
                                            VaadinDefaultRequestCache requestCache) throws Exception {
        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(new AuthenticationVersionFilter(sessionLookup), AuthorizationFilter.class);
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));
        if (oidcLoginAvailability.isAvailable()) {
            var mapper = identityMappers.getIfUnique();
            if (mapper == null) {
                throw new IllegalStateException("OIDC login requires exactly one ExternalIdentityMapper bean");
            }
            http.oauth2Login(configurer -> configurer
                    .loginPage("/login")
                    .authorizedClientRepository(discardingAuthorizedClientRepository())
                    .successHandler(oidcSuccessHandler(mapper, accountLookup, requestCache))
                    .failureHandler(new OidcAccessDeniedFailureHandler()));
        }
        return http.build();
    }

    OidcLocalUserAuthenticationSuccessHandler oidcSuccessHandler(ExternalIdentityMapper identityMapper,
                                                                   LocalUserAccountLookup accountLookup,
                                                                   VaadinDefaultRequestCache requestCache) {
        var savedRequestHandler = new VaadinSavedRequestAwareAuthenticationSuccessHandler();
        savedRequestHandler.setRequestCache(requestCache);
        return new OidcLocalUserAuthenticationSuccessHandler(identityMapper, accountLookup, savedRequestHandler);
    }

    DiscardingOAuth2AuthorizedClientRepository discardingAuthorizedClientRepository() {
        return new DiscardingOAuth2AuthorizedClientRepository();
    }
}
