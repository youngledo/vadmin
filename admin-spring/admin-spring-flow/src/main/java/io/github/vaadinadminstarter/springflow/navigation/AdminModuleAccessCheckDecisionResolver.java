package io.github.vaadinadminstarter.springflow.navigation;

import com.vaadin.flow.server.auth.AccessCheckDecisionResolver;
import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.DefaultAccessCheckDecisionResolver;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import java.util.List;
import java.util.Objects;

/** Gives module-declared permissions precedence over view annotations for dynamic routes. */
public final class AdminModuleAccessCheckDecisionResolver implements AccessCheckDecisionResolver {
    private final NavigationAccessChecker moduleChecker;
    private final DefaultAccessCheckDecisionResolver delegate = new DefaultAccessCheckDecisionResolver();

    public AdminModuleAccessCheckDecisionResolver(NavigationAccessChecker moduleChecker) {
        this.moduleChecker = Objects.requireNonNull(moduleChecker, "moduleChecker");
    }

    @Override
    public AccessCheckResult resolve(List<AccessCheckResult> results, NavigationContext context) {
        var moduleResult = moduleChecker.check(context);
        return moduleResult.decision() == com.vaadin.flow.server.auth.AccessCheckDecision.NEUTRAL
                ? delegate.resolve(results, context)
                : moduleResult;
    }
}
