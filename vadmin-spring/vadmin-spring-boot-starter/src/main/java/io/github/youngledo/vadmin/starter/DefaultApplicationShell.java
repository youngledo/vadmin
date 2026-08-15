package io.github.youngledo.vadmin.starter;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.web.context.support.WebApplicationContextUtils;

@StyleSheet(Aura.STYLESHEET)
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
        var script = new StringBuilder("document.documentElement.setAttribute('data-vadmin-visual-language','%s');"
                .formatted(appearance.visualLanguage().cssValue()));
        appearance.auraBaseSize().ifPresent(size -> script.append(
                "document.documentElement.style.setProperty('--aura-base-size','%d');".formatted(size)));
        settings.addInlineWithContents(script.toString(), Inline.Wrapping.JAVASCRIPT);
    }
}
