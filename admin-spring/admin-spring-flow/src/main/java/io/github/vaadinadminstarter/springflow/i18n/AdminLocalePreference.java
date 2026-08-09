package io.github.vaadinadminstarter.springflow.i18n;

import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/** Stores a supported administration locale in the current Vaadin session. */
public final class AdminLocalePreference {
    public static final String LOCALE_SESSION_KEY = "io.github.vaadinadminstarter.springflow.locale";

    public Locale selectInitialLocale(Locale browserLocale) {
        return CompositeAdminI18NProvider.EN_US.equals(browserLocale) ? CompositeAdminI18NProvider.EN_US
                : CompositeAdminI18NProvider.ZH_CN;
    }

    public void select(UI ui, Locale locale) {
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(locale, "locale");
        if (!CompositeAdminI18NProvider.ZH_CN.equals(locale) && !CompositeAdminI18NProvider.EN_US.equals(locale)) {
            throw new IllegalArgumentException("Unsupported administration locale: " + locale.toLanguageTag());
        }
        var session = Objects.requireNonNull(VaadinSession.getCurrent(), "No current VaadinSession");
        session.setAttribute(LOCALE_SESSION_KEY, locale);
        ui.setLocale(locale);
    }
}
