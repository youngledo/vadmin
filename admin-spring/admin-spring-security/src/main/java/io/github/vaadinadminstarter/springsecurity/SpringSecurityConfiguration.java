package io.github.vaadinadminstarter.springsecurity;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.ExternalIdentityMapper;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import io.github.vaadinadminstarter.springsecurity.auth.AuthenticationVersionFilter;
import io.github.vaadinadminstarter.springsecurity.auth.DiscardingOAuth2AuthorizedClientRepository;
import io.github.vaadinadminstarter.springsecurity.auth.LocalUserDetailsService;
import io.github.vaadinadminstarter.springsecurity.auth.OidcAccessDeniedFailureHandler;
import io.github.vaadinadminstarter.springsecurity.auth.OidcLocalUserAuthenticationSuccessHandler;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import io.github.vaadinadminstarter.springsecurity.auth.SpringAuthorizationService;
import io.github.vaadinadminstarter.springsecurity.ui.LoginView;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

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
