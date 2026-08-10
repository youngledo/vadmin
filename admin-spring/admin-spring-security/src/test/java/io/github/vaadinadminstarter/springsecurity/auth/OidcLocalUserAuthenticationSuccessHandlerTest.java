package io.github.vaadinadminstarter.springsecurity.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccount;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

class OidcLocalUserAuthenticationSuccessHandlerTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void replacesTheOidcPrincipalOnlyAfterAMapperResolvesAnEnabledLocalUser() throws Exception {
        var account = account(true);
        var handler = new OidcLocalUserAuthenticationSuccessHandler(
                identity -> Optional.of(currentUser(account)), new AccountLookup(account));
        var request = new MockHttpServletRequest("GET", "/login/oauth2/code/test");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, oidcAuthentication());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isInstanceOf(LocalUserPrincipal.class);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("system:user:read");
        assertThat(request.getSession(false).getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();
    }

    @Test
    void deniesAnUnmappedExternalIdentityWithoutAuthenticatingTheSession() throws Exception {
        var handler = new OidcLocalUserAuthenticationSuccessHandler(identity -> Optional.empty(), new AccountLookup(account(true)));
        var request = new MockHttpServletRequest("GET", "/login/oauth2/code/test");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, oidcAuthentication());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=access-denied");
    }

    @Test
    void deniesAResolvedUserWhenTheCurrentLocalAccountIsDisabledOrMissing() throws Exception {
        var resolved = currentUser(account(true));
        var disabledHandler = new OidcLocalUserAuthenticationSuccessHandler(
                identity -> Optional.of(resolved), new AccountLookup(account(false)));
        var missingHandler = new OidcLocalUserAuthenticationSuccessHandler(identity -> Optional.of(resolved), new EmptyAccountLookup());

        var disabledResponse = new MockHttpServletResponse();
        disabledHandler.onAuthenticationSuccess(new MockHttpServletRequest(), disabledResponse, oidcAuthentication());
        var missingResponse = new MockHttpServletResponse();
        missingHandler.onAuthenticationSuccess(new MockHttpServletRequest(), missingResponse, oidcAuthentication());

        assertThat(disabledResponse.getRedirectedUrl()).isEqualTo("/login?error=access-denied");
        assertThat(missingResponse.getRedirectedUrl()).isEqualTo("/login?error=access-denied");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void restoresTheSavedRequestAfterReplacingTheExternalPrincipal() throws Exception {
        var account = account(true);
        var handler = new OidcLocalUserAuthenticationSuccessHandler(
                identity -> Optional.of(currentUser(account)), new AccountLookup(account));
        var request = new MockHttpServletRequest("GET", "/login/oauth2/code/test");
        var response = new MockHttpServletResponse();
        var savedRequest = new MockHttpServletRequest("GET", "/users");
        new HttpSessionRequestCache().saveRequest(savedRequest, response);
        request.setSession(savedRequest.getSession());

        handler.onAuthenticationSuccess(request, response, oidcAuthentication());

        assertThat(response.getRedirectedUrl()).contains("/users");
    }

    @Test
    void restoresTheFlowServerSideNavigationStoredInTheSession() throws Exception {
        var account = account(true);
        var handler = new OidcLocalUserAuthenticationSuccessHandler(
                identity -> Optional.of(currentUser(account)), new AccountLookup(account));
        var request = new MockHttpServletRequest("GET", "/login/oauth2/code/test");
        request.getSession().setAttribute(NavigationAccessControl.SESSION_STORED_REDIRECT_ABSOLUTE,
                "http://localhost/customers?status=active");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, oidcAuthentication());

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost/customers?status=active");
        assertThat(request.getSession(false).getAttribute(NavigationAccessControl.SESSION_STORED_REDIRECT_ABSOLUTE))
                .isNull();
    }

    @Test
    void clearsTheSessionAndDoesNotExposeExternalFailureDetailsWhenAccessIsDenied() throws Exception {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("access_token", "department"));
        var response = new MockHttpServletResponse();

        new OidcAccessDeniedFailureHandler().deny(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getSession(false).getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNull();
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=access-denied")
                .doesNotContain("access_token", "department");
    }

    private OAuth2AuthenticationToken oidcAuthentication() {
        var now = Instant.parse("2026-08-10T00:00:00Z");
        var user = new DefaultOidcUser(List.of(), new OidcIdToken("opaque-id-token", now, now.plusSeconds(300), Map.of(
                "iss", "https://issuer.example", "sub", "subject-42", "name", "Ada")));
        return new OAuth2AuthenticationToken(user, List.of(), "test");
    }

    private LocalUserAccount account(boolean enabled) {
        return new LocalUserAccount(UUID.fromString("0182da5f-22aa-7a6e-8171-6d2ce273bb69"), "ada", "stored-hash", enabled, 4,
                Set.of(PermissionCode.of("system:user:read")));
    }

    private CurrentUser currentUser(LocalUserAccount account) {
        return new CurrentUser(account.userId(), account.username(), account.permissions(), account.authVersion());
    }

    private record AccountLookup(LocalUserAccount account) implements LocalUserAccountLookup {
        @Override public Optional<LocalUserAccount> findByUsername(String username) { return Optional.of(account); }
        @Override public Optional<LocalUserAccount> findByUserId(UUID userId) { return Optional.of(account); }
    }

    private static final class EmptyAccountLookup implements LocalUserAccountLookup {
        @Override public Optional<LocalUserAccount> findByUsername(String username) { return Optional.empty(); }
        @Override public Optional<LocalUserAccount> findByUserId(UUID userId) { return Optional.empty(); }
    }
}
