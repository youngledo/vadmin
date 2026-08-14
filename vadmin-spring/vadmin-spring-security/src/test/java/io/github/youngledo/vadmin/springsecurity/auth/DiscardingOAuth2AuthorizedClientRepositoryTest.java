package io.github.youngledo.vadmin.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

class DiscardingOAuth2AuthorizedClientRepositoryTest {
    private final DiscardingOAuth2AuthorizedClientRepository repository =
            new DiscardingOAuth2AuthorizedClientRepository();

    @Test
    void doesNotRetainTokensAfterSuccessfulLocalUserMapping() {
        var request = new MockHttpServletRequest();
        var authentication = new TestingAuthenticationToken("external-subject", "n/a", "ROLE_USER");
        repository.saveAuthorizedClient(authorizedClient(), authentication, request, new MockHttpServletResponse());

        OAuth2AuthorizedClient storedClient = repository.loadAuthorizedClient("oidc", authentication, request);
        assertThat(storedClient).isNull();
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void doesNotRetainTokensWhenLocalUserMappingDeniesAccess() {
        var request = new MockHttpServletRequest();
        var authentication = new TestingAuthenticationToken("unmapped-subject", "n/a", "ROLE_USER");

        // OAuth2LoginAuthenticationFilter stores the authorized client before the mapping handler denies access.
        repository.saveAuthorizedClient(authorizedClient(), authentication, request, new MockHttpServletResponse());

        OAuth2AuthorizedClient storedClient = repository.loadAuthorizedClient("oidc", authentication, request);
        assertThat(storedClient).isNull();
        assertThat(request.getSession(false)).isNull();
    }

    private OAuth2AuthorizedClient authorizedClient() {
        var registration = ClientRegistration.withRegistrationId("oidc")
                .clientId("client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://issuer.example/authorize")
                .tokenUri("https://issuer.example/token")
                .build();
        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token", Instant.now(),
                Instant.now().plusSeconds(300));
        return new OAuth2AuthorizedClient(registration, "external-subject", token,
                new OAuth2RefreshToken("refresh-token", Instant.now()));
    }
}
