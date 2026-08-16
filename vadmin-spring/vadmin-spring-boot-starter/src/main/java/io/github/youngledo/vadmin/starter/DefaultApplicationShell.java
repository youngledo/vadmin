package io.github.youngledo.vadmin.starter;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Theme(themeClass = Lumo.class)
@StyleSheet("vadmin/ant.css")
public final class DefaultApplicationShell implements AppShellConfigurator {
    @Override
    public void configurePage(com.vaadin.flow.server.AppShellSettings settings) {
        if (!(settings.getRequest().getService().getContext() instanceof VaadinServletContext context)) {
            return;
        }
        var applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
                context.getContext());
        var appearance = applicationContext.getBean(io.github.youngledo.vadmin.starter.theme.AdminAppearanceProperties.class);
        var script = "document.documentElement.setAttribute('data-vadmin-visual-language','%s');"
                .formatted(appearance.visualLanguage().cssValue());
        settings.addInlineWithContents(script, Inline.Wrapping.JAVASCRIPT);
    }
}
