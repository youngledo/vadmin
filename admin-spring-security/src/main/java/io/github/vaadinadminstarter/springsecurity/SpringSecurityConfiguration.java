package io.github.vaadinadminstarter.springsecurity;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.LocalUserSessionLookup;
import io.github.vaadinadminstarter.springsecurity.auth.AuthenticationVersionFilter;
import io.github.vaadinadminstarter.springsecurity.auth.LocalUserDetailsService;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import io.github.vaadinadminstarter.springsecurity.auth.SpringAuthorizationService;
import io.github.vaadinadminstarter.springsecurity.ui.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
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
    SecurityContextCurrentUserProvider currentUserProvider() {
        return new SecurityContextCurrentUserProvider();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider,
                                            LocalUserSessionLookup sessionLookup) throws Exception {
        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(new AuthenticationVersionFilter(sessionLookup), AuthorizationFilter.class);
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));
        return http.build();
    }
}
