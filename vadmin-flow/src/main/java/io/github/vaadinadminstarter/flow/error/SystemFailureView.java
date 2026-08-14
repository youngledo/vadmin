package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("system-error")
@PermitAll
public final class SystemFailureView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
    private final H1 heading = new H1();
    public SystemFailureView() { add(heading); updateText(); }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }
    @Override public String getPageTitle() { return getTranslation("flow.system-failure.title"); }
    private void updateText() { heading.setText(getTranslation("flow.system-failure.heading")); }
    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
