package io.github.youngledo.vadmin.springflow.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/** Stores a supported administration locale in the current Vaadin session. */
public final class AdminLocalePreference {
    public static final String LOCALE_SESSION_KEY = "io.github.youngledo.vadmin.springflow.locale";

    public Locale selectInitialLocale(Locale browserLocale) {
        return selectInitialLocale(browserLocale == null ? List.of() : List.of(browserLocale));
    }

    /** Returns a stored locale first, then the first browser locale supported by the host. */
    public Locale selectInitialLocale(Iterable<Locale> browserLocales) {
        var session = VaadinSession.getCurrent();
        if (session != null) {
            var storedLocale = session.getAttribute(LOCALE_SESSION_KEY);
            if (storedLocale instanceof Locale locale && isSupported(locale)) {
                return locale;
            }
        }
        if (browserLocales != null) {
            for (var browserLocale : browserLocales) {
                if (isSupported(browserLocale)) {
                    return browserLocale;
                }
            }
        }
        return CompositeAdminI18NProvider.ZH_CN;
    }

    public void select(UI ui, Locale locale) {
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(locale, "locale");
        if (!isSupported(locale)) {
            throw new IllegalArgumentException("Unsupported administration locale: " + locale.toLanguageTag());
        }
        var session = Objects.requireNonNull(VaadinSession.getCurrent(), "No current VaadinSession");
        session.setAttribute(LOCALE_SESSION_KEY, locale);
        ui.setLocale(locale);
    }

    private static boolean isSupported(Locale locale) {
        return CompositeAdminI18NProvider.ZH_CN.equals(locale) || CompositeAdminI18NProvider.EN_US.equals(locale);
    }
}
