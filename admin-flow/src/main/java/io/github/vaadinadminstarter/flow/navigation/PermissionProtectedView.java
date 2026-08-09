package io.github.vaadinadminstarter.flow.navigation;

import java.util.Objects;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import jakarta.annotation.security.PermitAll;

/** Base class for external administration pages that require a single permission to enter. */
@PermitAll
public abstract class PermissionProtectedView extends VerticalLayout implements BeforeEnterObserver {
    private final CurrentUserProvider currentUserProvider;
    private final AuthorizationService authorization;

    protected PermissionProtectedView(CurrentUserProvider currentUserProvider, AuthorizationService authorization) {
        this.currentUserProvider = Objects.requireNonNull(currentUserProvider, "currentUserProvider");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
        setSizeFull();
        setPadding(true);
    }

    protected abstract PermissionCode requiredPermission();

    protected final CurrentUser requireCurrentUser() {
        return currentUserProvider.currentUser().orElseThrow();
    }

    @Override
    public final void beforeEnter(BeforeEnterEvent event) {
        var user = currentUserProvider.currentUser();
        if (user.isEmpty()) {
            event.rerouteTo("login");
        } else if (!authorization.hasPermission(user.get(), requiredPermission())) {
            event.rerouteTo("access-denied");
        }
    }
}
