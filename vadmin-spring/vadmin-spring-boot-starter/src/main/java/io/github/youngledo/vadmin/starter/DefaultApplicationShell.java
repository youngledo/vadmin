package io.github.youngledo.vadmin.starter;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.theme.Theme;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Theme("admin-theme")
public final class DefaultApplicationShell implements AppShellConfigurator {
    @Override
    public void configurePage(com.vaadin.flow.server.AppShellSettings settings) {
        if (!(settings.getRequest().getService().getContext() instanceof VaadinServletContext context)) {
            return;
        }
        var applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
                context.getContext());
        var appearance = applicationContext.getBean(io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties.class);
        settings.addInlineWithContents("document.documentElement.setAttribute('data-admin-visual-language','%s');"
                        .formatted(appearance.visualLanguage().cssValue())
                        + "document.documentElement.setAttribute('data-admin-density','%s');"
                        .formatted(appearance.density().cssValue()), Inline.Wrapping.JAVASCRIPT);
    }
}
