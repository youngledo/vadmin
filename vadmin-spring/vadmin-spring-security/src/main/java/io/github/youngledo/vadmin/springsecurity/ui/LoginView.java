package io.github.youngledo.vadmin.springsecurity.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.github.youngledo.vadmin.springsecurity.OidcLoginAvailability;
import io.github.youngledo.vadmin.springsecurity.auth.LocalLoginAuthenticator;

@Route(value = "login", autoLayout = false)
@AnonymousAllowed
public final class LoginView extends com.vaadin.flow.component.orderedlayout.VerticalLayout implements BeforeEnterObserver, LocaleChangeObserver, HasDynamicTitle {
    private final Span deniedMessage = new Span();
    private final LoginOverlay login = new LoginOverlay();
    private final Anchor oidcLogin;

    public LoginView(OidcLoginAvailability oidcLoginAvailability, LocalLoginAuthenticator localLoginAuthenticator) {
        login.addLoginListener(event -> authenticateLocally(event, localLoginAuthenticator));
        oidcLogin = oidcLoginAvailability.isAvailable()
                ? new Anchor(oidcAuthorizationUrl(oidcLoginAvailability.registrationId())) : null;
        if (oidcLogin != null) {
            oidcLogin.getElement().setAttribute("router-ignore", true);
        }
        deniedMessage.setVisible(false);
        login.getCustomFormArea().add(deniedMessage);
        if (oidcLogin != null) login.getFooter().add(oidcLogin);
        add(login);
        updateText();
    }
    @Override protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        login.setOpened(true);
    }
    @Override public void beforeEnter(BeforeEnterEvent event) {
        var errors = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("error", java.util.List.of());
        var denied = errors.contains("access-denied");
        deniedMessage.setVisible(denied);
        login.setError(!errors.isEmpty() && !denied);
        if (denied) deniedMessage.setText(getTranslation("flow.login.denied"));
    }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }
    @Override public String getPageTitle() { return getTranslation("flow.login.title"); }
    private void updateText() {
        login.setI18n(loginI18n());
        if (oidcLogin != null) oidcLogin.setText(getTranslation("flow.login.sso"));
        if (deniedMessage.isVisible()) deniedMessage.setText(getTranslation("flow.login.denied"));
    }

    private LoginI18n loginI18n() {
        var i18n = LoginI18n.createDefault();
        var header = new LoginI18n.Header();
        header.setTitle(getTranslation("flow.login.heading"));
        header.setDescription("");
        i18n.setHeader(header);
        i18n.getForm().setTitle(getTranslation("flow.login.form.title"));
        i18n.getForm().setUsername(getTranslation("flow.login.form.username"));
        i18n.getForm().setPassword(getTranslation("flow.login.form.password"));
        i18n.getForm().setSubmit(getTranslation("flow.login.form.submit"));
        i18n.getForm().setForgotPassword(getTranslation("flow.login.form.forgot-password"));
        i18n.getErrorMessage().setTitle(getTranslation("flow.login.error.title"));
        i18n.getErrorMessage().setMessage(getTranslation("flow.login.error.message"));
        i18n.getErrorMessage().setUsername(getTranslation("flow.login.error.username"));
        i18n.getErrorMessage().setPassword(getTranslation("flow.login.error.password"));
        return i18n;
    }

    private void authenticateLocally(LoginEvent event, LocalLoginAuthenticator localLoginAuthenticator) {
        var request = VaadinServletRequest.getCurrent();
        var response = VaadinServletResponse.getCurrent();
        if (request == null || response == null || !localLoginAuthenticator.authenticate(event.getUsername(), event.getPassword(),
                request.getHttpServletRequest(), response.getHttpServletResponse())) {
            login.setError(true);
            return;
        }
        getUI().ifPresent(ui -> ui.getPage().setLocation(request.getContextPath() + "/"));
    }

    private static String oidcAuthorizationUrl(String registrationId) {
        var request = VaadinRequest.getCurrent();
        var contextPath = request == null ? "" : request.getContextPath();
        return contextPath + "/oauth2/authorization/" + registrationId;
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
