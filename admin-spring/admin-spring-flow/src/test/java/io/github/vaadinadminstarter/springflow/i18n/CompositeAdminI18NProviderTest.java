package io.github.vaadinadminstarter.springflow.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;

class CompositeAdminI18NProviderTest {
    private static final Locale ZH_CN = Locale.forLanguageTag("zh-CN");
    private static final Locale EN_US = Locale.forLanguageTag("en-US");

    private final CompositeAdminI18NProvider provider = new CompositeAdminI18NProvider(List.of(
            new AdminMessageBundle("core", "i18n.core"),
            new AdminMessageBundle("orders", "i18n.orders")));

    @Test
    void usesTheSelectedModuleBundleAndFormatsParameters() {
        assertThat(provider.getTranslation("orders.greeting", EN_US, "A-1024"))
                .isEqualTo("Order A-1024");
    }

    @Test
    void fallsBackToChineseWhenTheSelectedLocaleDoesNotContainTheKey() {
        assertThat(provider.getTranslation("orders.status.pending", EN_US))
                .isEqualTo("待处理");
    }

    @Test
    void doesNotSearchOtherModuleBundlesForAKey() {
        assertThat(provider.getTranslation("orders.shared", ZH_CN))
                .isEqualTo("!zh-CN: orders.shared!");
    }

    @Test
    void returnsAnExplicitMarkerForAMissingKey() {
        var logger = (Logger) LoggerFactory.getLogger(CompositeAdminI18NProvider.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            assertThat(provider.getTranslation("orders.missing", EN_US))
                    .isEqualTo("!en-US: orders.missing!");
            assertThat(events.list).extracting(ILoggingEvent::getFormattedMessage)
                    .anySatisfy(message -> assertThat(message).contains("!en-US: orders.missing!"));
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    void exposesOnlyTheSupportedLocalesInDeterministicOrder() {
        assertThat(provider.getProvidedLocales()).containsExactly(ZH_CN, EN_US);
    }
}
