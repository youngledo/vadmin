package io.github.vaadinadminstarter.springflow.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.VaadinSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminLocalePreferenceTest {
    private static final Locale ZH_CN = Locale.forLanguageTag("zh-CN");
    private static final Locale EN_US = Locale.forLanguageTag("en-US");

    private final AdminLocalePreference preference = new AdminLocalePreference();

    @AfterEach
    void clearCurrentSession() {
        VaadinSession.setCurrent(null);
    }

    @Test
    void usesTheStoredSupportedLocaleBeforeTheBrowserLocale() {
        var session = mock(VaadinSession.class);
        when(session.getAttribute(AdminLocalePreference.LOCALE_SESSION_KEY)).thenReturn(EN_US);
        VaadinSession.setCurrent(session);

        assertThat(preference.selectInitialLocale(ZH_CN)).isEqualTo(EN_US);
    }

    @Test
    void defaultsToChineseWhenTheSessionHasNoLocalePreference() {
        assertThat(preference.selectInitialLocale(EN_US)).isEqualTo(ZH_CN);
    }
}
