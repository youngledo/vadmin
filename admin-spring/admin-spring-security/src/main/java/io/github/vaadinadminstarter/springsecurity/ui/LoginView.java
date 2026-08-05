package io.github.vaadinadminstarter.springsecurity.ui;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("登录")
@AnonymousAllowed
public final class LoginView extends VerticalLayout {
    public LoginView() {
        var login = new LoginForm();
        login.setAction("login");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(login);
    }
}
