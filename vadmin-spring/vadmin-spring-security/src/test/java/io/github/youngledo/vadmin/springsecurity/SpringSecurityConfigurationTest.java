package io.github.youngledo.vadmin.springsecurity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.github.youngledo.vadmin.contracts.auth.ExternalIdentityMapper;
import io.github.youngledo.vadmin.contracts.auth.LocalUserAccountLookup;
import io.github.youngledo.vadmin.springsecurity.auth.DiscardingOAuth2AuthorizedClientRepository;
import io.github.youngledo.vadmin.springsecurity.auth.OidcLocalUserAuthenticationSuccessHandler;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.util.ReflectionTestUtils;

class SpringSecurityConfigurationTest {
    private final SpringSecurityConfiguration configuration = new SpringSecurityConfiguration();
    private final ApplicationContextRunner propertiesContext = new ApplicationContextRunner()
            .withUserConfiguration(OidcPropertiesConfiguration.class);

    @Test
    void bindsTheStarterNamespacedRegistrationId() {
        propertiesContext.withPropertyValues("vaadin-admin.oidc.registration-id=corp-sso")
                .run(context -> assertThat(context.getBean(OidcLoginProperties.class).registrationId())
                        .isEqualTo("corp-sso"));
    }

    @Test
    void keepsPasswordLoginWhenNoClientRegistrationExists() {
        var availability = configuration.oidcLoginAvailability(this.<ClientRegistrationRepository>provider(),
                this.<ExternalIdentityMapper>provider(), defaultProperties());

        assertThat(availability.isAvailable()).isFalse();
        assertThat(availability.registrationId()).isEqualTo("oidc");
    }

    @Test
    void exposesOidcOnlyForAConfiguredClientAndUniqueMapper() {
        var availability = configuration.oidcLoginAvailability(provider(registrationRepository()),
                provider((ExternalIdentityMapper) identity -> java.util.Optional.empty()), defaultProperties());

        assertThat(availability.isAvailable()).isTrue();
        assertThat(availability.registrationId()).isEqualTo("oidc");
    }

    @Test
    void usesTheConfiguredGenericRegistrationId() {
        var availability = configuration.oidcLoginAvailability(provider(registrationRepository("corp-sso")),
                provider((ExternalIdentityMapper) identity -> java.util.Optional.empty()),
                new OidcLoginProperties("corp-sso"));

        assertThat(availability.isAvailable()).isTrue();
        assertThat(availability.registrationId()).isEqualTo("corp-sso");
    }

    @Test
    void rejectsAClientRegistrationWithoutAnIdentityMapper() {
        assertThatIllegalStateException().isThrownBy(() ->
                        configuration.oidcLoginAvailability(provider(registrationRepository()),
                                this.<ExternalIdentityMapper>provider(), defaultProperties()))
                .withMessageContaining("ExternalIdentityMapper")
                .withMessageNotContaining("secret");
    }

    @Test
    void rejectsAClientRegistrationWithMultipleIdentityMappers() {
        ExternalIdentityMapper first = identity -> java.util.Optional.empty();
        ExternalIdentityMapper second = identity -> java.util.Optional.empty();

        assertThatIllegalStateException().isThrownBy(() ->
                        configuration.oidcLoginAvailability(provider(registrationRepository()), provider(first, second),
                                defaultProperties()))
                .withMessageContaining("exactly one")
                .withMessageNotContaining("secret");
    }

    @Test
    void bindsTheOidcSuccessHandlerToTheVaadinRequestCache() {
        var requestCache = new VaadinDefaultRequestCache();
        var handler = configuration.oidcSuccessHandler(identity -> java.util.Optional.empty(),
                mock(LocalUserAccountLookup.class), requestCache);

        var savedRequestHandler = ReflectionTestUtils.getField(handler, "successHandler");
        assertThat(savedRequestHandler).isInstanceOf(VaadinSavedRequestAwareAuthenticationSuccessHandler.class);
        assertThat(ReflectionTestUtils.getField(savedRequestHandler, "requestCache")).isSameAs(requestCache);
    }

    @Test
    void configuresOidcLoginToDiscardAuthorizedClientTokens() {
        assertThat(configuration.discardingAuthorizedClientRepository())
                .isInstanceOf(DiscardingOAuth2AuthorizedClientRepository.class);
    }

    private ClientRegistrationRepository registrationRepository() {
        return registrationRepository("oidc");
    }

    private OidcLoginProperties defaultProperties() {
        return new OidcLoginProperties(null);
    }

    private ClientRegistrationRepository registrationRepository(String registrationId) {
        var repository = mock(ClientRegistrationRepository.class);
        when(repository.findByRegistrationId(registrationId)).thenReturn(ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client")
                .clientSecret("not-reported")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://issuer.example/authorize")
                .tokenUri("https://issuer.example/token")
                .userInfoUri("https://issuer.example/userinfo")
                .userNameAttributeName("sub")
                .issuerUri("https://issuer.example")
                .build());
        return repository;
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> provider(T... values) {
        var provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(values.length == 0 ? null : values[0]);
        when(provider.orderedStream()).thenReturn(Stream.of(values));
        return provider;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OidcLoginProperties.class)
    static class OidcPropertiesConfiguration {
    }
}
