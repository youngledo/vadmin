package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("access-denied")
@PermitAll
public final class AccessDeniedView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
    private final H1 heading = new H1();
    public AccessDeniedView() { add(heading); updateText(); }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); updateBrowserTitle(); }
    @Override public String getPageTitle() { return getTranslation("flow.access-denied.title"); }
    private void updateText() { heading.setText(getTranslation("flow.access-denied.heading")); }
    private void updateBrowserTitle() { getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle())); }
}
