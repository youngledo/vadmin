package io.github.vaadinadminstarter.springsecurity.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public final class LoginView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
    private final H1 heading = new H1();
    public LoginView() {
        var login = new LoginForm();
        login.setAction("login");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(heading, login);
        updateText();
    }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }
    @Override public String getPageTitle() { return getTranslation("flow.login.title"); }
    private void updateText() { heading.setText(getTranslation("flow.login.heading")); }
    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
