package io.github.vaadinadminstarter.springsecurity.auth;

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.github.vaadinadminstarter.contracts.auth.ExternalIdentityMapper;
import io.github.vaadinadminstarter.contracts.auth.LocalUserAccountLookup;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

public final class OidcLocalUserAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ExternalIdentityMapper identityMapper;
    private final LocalUserAccountLookup accountLookup;
    private final OidcExternalIdentityFactory identityFactory;
    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationSuccessHandler successHandler;
    private final OidcAccessDeniedFailureHandler deniedFailureHandler;

    public OidcLocalUserAuthenticationSuccessHandler(ExternalIdentityMapper identityMapper,
                                                     LocalUserAccountLookup accountLookup) {
        this(identityMapper, accountLookup, new OidcExternalIdentityFactory(),
                new HttpSessionSecurityContextRepository(), new VaadinSavedRequestAwareAuthenticationSuccessHandler(),
                new OidcAccessDeniedFailureHandler());
    }

    OidcLocalUserAuthenticationSuccessHandler(ExternalIdentityMapper identityMapper,
                                              LocalUserAccountLookup accountLookup,
                                              OidcExternalIdentityFactory identityFactory,
                                              SecurityContextRepository securityContextRepository,
                                              AuthenticationSuccessHandler successHandler,
                                              OidcAccessDeniedFailureHandler deniedFailureHandler) {
        this.identityMapper = Objects.requireNonNull(identityMapper, "identityMapper");
        this.accountLookup = Objects.requireNonNull(accountLookup, "accountLookup");
        this.identityFactory = Objects.requireNonNull(identityFactory, "identityFactory");
        this.securityContextRepository = Objects.requireNonNull(securityContextRepository, "securityContextRepository");
        this.successHandler = Objects.requireNonNull(successHandler, "successHandler");
        this.deniedFailureHandler = Objects.requireNonNull(deniedFailureHandler, "deniedFailureHandler");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var localAccount = resolveEnabledLocalAccount(authentication);
        if (localAccount.isEmpty()) {
            deniedFailureHandler.deny(request, response);
            return;
        }

        var principal = new LocalUserPrincipal(localAccount.orElseThrow());
        var localAuthentication = UsernamePasswordAuthenticationToken.authenticated(principal, null,
                principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(localAuthentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        successHandler.onAuthenticationSuccess(request, response, localAuthentication);
    }

    private java.util.Optional<io.github.vaadinadminstarter.contracts.auth.LocalUserAccount>
            resolveEnabledLocalAccount(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            return java.util.Optional.empty();
        }
        try {
            return identityMapper.map(identityFactory.from(oidcUser))
                    .flatMap(currentUser -> accountLookup.findByUserId(currentUser.userId()))
                    .filter(io.github.vaadinadminstarter.contracts.auth.LocalUserAccount::enabled);
        } catch (RuntimeException exception) {
            return java.util.Optional.empty();
        }
    }
}
