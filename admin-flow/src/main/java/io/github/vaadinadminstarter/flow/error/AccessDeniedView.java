package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("access-denied")
@PageTitle("Access denied")
@PermitAll
public final class AccessDeniedView extends VerticalLayout implements LocaleChangeObserver {
    private final H1 heading = new H1();
    public AccessDeniedView() { add(heading); updateText(); }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); }
    private void updateText() { heading.setText(getTranslation("flow.access-denied.heading")); }
}
