package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;

abstract class SecuredView extends VerticalLayout implements BeforeEnterObserver {
    private final SecurityContextCurrentUserProvider currentUser;
    private final AuthorizationService authorization;

    SecuredView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization) {
        this.currentUser = currentUser;
        this.authorization = authorization;
        setSizeFull();
        setPadding(true);
    }

    abstract PermissionCode requiredPermission();

    protected final CurrentUser requireCurrentUser() {
        return currentUser.currentUser().orElseThrow();
    }

    @Override
    public final void beforeEnter(BeforeEnterEvent event) {
        var user = currentUser.currentUser();
        if (user.isEmpty()) {
            event.rerouteTo("login");
        } else if (!authorization.hasPermission(user.get(), requiredPermission())) {
            event.rerouteTo("access-denied");
        }
    }
}
