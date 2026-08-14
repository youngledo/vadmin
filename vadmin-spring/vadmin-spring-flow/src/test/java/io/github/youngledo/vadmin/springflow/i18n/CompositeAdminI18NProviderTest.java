package io.github.youngledo.vadmin.springflow.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;

class CompositeAdminI18NProviderTest {
    private static final Locale ZH_CN = Locale.forLanguageTag("zh-CN");
    private static final Locale EN_US = Locale.forLanguageTag("en-US");

    private final CompositeAdminI18NProvider provider = new CompositeAdminI18NProvider(List.of(
            new AdminMessageBundle("core", "i18n.core"),
            new AdminMessageBundle("sample", "i18n.sample")));

    @Test
    void usesTheSelectedModuleBundleAndFormatsParameters() {
        assertThat(provider.getTranslation("sample.greeting", EN_US, "A-1024"))
                .isEqualTo("Sample A-1024");
    }

    @Test
    void fallsBackToChineseWhenTheSelectedLocaleDoesNotContainTheKey() {
        assertThat(provider.getTranslation("sample.status.pending", EN_US))
                .isEqualTo("待处理");
    }

    @Test
    void doesNotSearchOtherModuleBundlesForAKey() {
        assertThat(provider.getTranslation("sample.shared", ZH_CN))
                .isEqualTo("!zh-CN: sample.shared!");
    }

    @Test
    void returnsAnExplicitMarkerForAMissingKey() {
        var logger = (Logger) LoggerFactory.getLogger(CompositeAdminI18NProvider.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            assertThat(provider.getTranslation("sample.missing", EN_US))
                    .isEqualTo("!en-US: sample.missing!");
            assertThat(events.list).extracting(ILoggingEvent::getFormattedMessage)
                    .anySatisfy(message -> assertThat(message).contains("!en-US: sample.missing!"));
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
