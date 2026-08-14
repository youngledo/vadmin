package io.github.vaadinadminstarter.springsecurity.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import io.github.vaadinadminstarter.springsecurity.OidcLoginAvailability;
import io.github.vaadinadminstarter.springsecurity.auth.LocalLoginAuthenticator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginViewTest {
    private VaadinService service;
    private LocalLoginAuthenticator localLoginAuthenticator;

    @BeforeEach
    void installEnglishTranslations() {
        var translations = mock(I18NProvider.class);
        when(translations.getDefaultLocale()).thenReturn(Locale.US);
        when(translations.getProvidedLocales()).thenReturn(List.of(Locale.US));
        when(translations.getTranslation("flow.login.heading", Locale.US)).thenReturn("Vaadin Admin Starter");
        when(translations.getTranslation("flow.login.title", Locale.US)).thenReturn("Sign in");
        when(translations.getTranslation("flow.login.sso", Locale.US)).thenReturn("Continue with single sign-on");
        when(translations.getTranslation("flow.login.denied", Locale.US)).thenReturn("Sign-in was not permitted");
        var instantiator = mock(Instantiator.class);
        when(instantiator.getI18NProvider()).thenReturn(translations);
        service = mock(VaadinService.class, CALLS_REAL_METHODS);
        when(service.getInstantiator()).thenReturn(instantiator);
        var deploymentConfiguration = mock(DeploymentConfiguration.class);
        when(deploymentConfiguration.getUrlSafeSchemes()).thenReturn(Set.of("http", "https"));
        when(service.getDeploymentConfiguration()).thenReturn(deploymentConfiguration);
        VaadinService.setCurrent(service);
        localLoginAuthenticator = mock(LocalLoginAuthenticator.class);
    }

    @AfterEach
    void clearVaadinService() {
        service.setCurrentInstances(null, null);
        VaadinService.setCurrent(null);
    }

    @Test
    void rendersOneProviderNeutralExternalEntryWhenOidcIsAvailable() {
        var view = new LoginView(new OidcLoginAvailability(true, "oidc"), localLoginAuthenticator);

        assertThat(view.getChildren().filter(Anchor.class::isInstance))
                .singleElement()
                .isInstanceOfSatisfying(Anchor.class, anchor -> {
                    assertThat(anchor.getHref()).isEqualTo("/oauth2/authorization/oidc");
                    assertThat(anchor.getElement().hasAttribute("router-ignore")).isTrue();
                    assertThat(anchor.getText()).isEqualTo("Continue with single sign-on");
                });
        assertThat(view.getChildren()).anyMatch(LoginForm.class::isInstance);
    }

    @Test
    void usesTheConfiguredRegistrationIdForTheExternalEntry() {
        var view = new LoginView(new OidcLoginAvailability(true, "corp-sso"), localLoginAuthenticator);

        assertThat(view.getChildren().filter(Anchor.class::isInstance))
                .singleElement()
                .isInstanceOfSatisfying(Anchor.class,
                        anchor -> assertThat(anchor.getHref()).isEqualTo("/oauth2/authorization/corp-sso"));
    }

    @Test
    void prefixesTheExternalEntryWithTheCurrentContextPath() {
        var request = mock(VaadinRequest.class);
        when(request.getContextPath()).thenReturn("/admin");
        service.setCurrentInstances(request, null);

        var view = new LoginView(new OidcLoginAvailability(true, "corp-sso"), localLoginAuthenticator);

        assertThat(view.getChildren().filter(Anchor.class::isInstance))
                .singleElement()
                .isInstanceOfSatisfying(Anchor.class,
                        anchor -> assertThat(anchor.getHref()).isEqualTo("/admin/oauth2/authorization/corp-sso"));
    }

    @Test
    void keepsTheExistingLocalLoginWithoutAnExternalEntryWhenOidcIsUnavailable() {
        var view = new LoginView(new OidcLoginAvailability(false, "oidc"), localLoginAuthenticator);

        assertThat(view.getChildren()).noneMatch(Anchor.class::isInstance);
        assertThat(view.getChildren().filter(LoginForm.class::isInstance))
                .singleElement()
                .isInstanceOfSatisfying(LoginForm.class, login -> assertThat(login.getAction()).isEmpty());
    }

    @Test
    void showsTheGenericLoginFormErrorAfterLocalCredentialsAreRejected() {
        var view = new LoginView(new OidcLoginAvailability(false, "oidc"), localLoginAuthenticator);
        var event = mock(com.vaadin.flow.router.BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location("login", QueryParameters.of("error", "")));

        view.beforeEnter(event);

        assertThat(view.getChildren().filter(LoginForm.class::isInstance))
                .singleElement()
                .isInstanceOfSatisfying(LoginForm.class, login -> assertThat(login.isError()).isTrue());
    }
}
