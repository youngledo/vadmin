package io.github.vaadinadminstarter.springsecurity.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.github.vaadinadminstarter.springsecurity.OidcLoginAvailability;

@Route("login")
@AnonymousAllowed
public final class LoginView extends VerticalLayout implements BeforeEnterObserver, LocaleChangeObserver, HasDynamicTitle {
    private final H1 heading = new H1();
    private final Span deniedMessage = new Span();
    private final Anchor oidcLogin;

    public LoginView(OidcLoginAvailability oidcLoginAvailability) {
        var login = new LoginForm();
        login.setAction("login");
        oidcLogin = oidcLoginAvailability.isAvailable()
                ? new Anchor(oidcAuthorizationUrl(oidcLoginAvailability.registrationId())) : null;
        deniedMessage.setVisible(false);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(heading, deniedMessage, login);
        if (oidcLogin != null) {
            add(oidcLogin);
        }
        updateText();
    }
    @Override public void beforeEnter(BeforeEnterEvent event) {
        var denied = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("error", java.util.List.of()).contains("access-denied");
        deniedMessage.setVisible(denied);
        if (denied) deniedMessage.setText(getTranslation("flow.login.denied"));
    }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }
    @Override public String getPageTitle() { return getTranslation("flow.login.title"); }
    private void updateText() {
        heading.setText(getTranslation("flow.login.heading"));
        if (oidcLogin != null) oidcLogin.setText(getTranslation("flow.login.sso"));
        if (deniedMessage.isVisible()) deniedMessage.setText(getTranslation("flow.login.denied"));
    }

    private static String oidcAuthorizationUrl(String registrationId) {
        var request = VaadinRequest.getCurrent();
        var contextPath = request == null ? "" : request.getContextPath();
        return contextPath + "/oauth2/authorization/" + registrationId;
    }

    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
