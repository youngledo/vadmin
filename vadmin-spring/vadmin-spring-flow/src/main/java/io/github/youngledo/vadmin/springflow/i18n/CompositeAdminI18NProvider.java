package io.github.youngledo.vadmin.springflow.i18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.i18n.I18NProvider;

import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;

/** I18N provider that resolves each translation only from its declared module resources. */
public final class CompositeAdminI18NProvider implements I18NProvider {
    public static final Locale ZH_CN = Locale.forLanguageTag("zh-CN");
    public static final Locale EN_US = Locale.forLanguageTag("en-US");

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeAdminI18NProvider.class);
    private static final List<Locale> PROVIDED_LOCALES = List.of(ZH_CN, EN_US);
    private static final ResourceBundle.Control EXACT_LOCALE_CONTROL = new ResourceBundle.Control() {
        @Override
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            return List.of(locale);
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
        }
    };

    private final Map<String, List<String>> baseNamesByModule;

    public CompositeAdminI18NProvider(List<AdminMessageBundle> messageBundles) {
        Objects.requireNonNull(messageBundles, "messageBundles");
        var bundles = List.copyOf(messageBundles);
        if (bundles.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("messageBundles must not contain null elements");
        }

        var namesByModule = new LinkedHashMap<String, List<String>>();
        var modulesByBaseName = new LinkedHashMap<String, String>();
        for (var bundle : bundles) {
            var existingModule = modulesByBaseName.putIfAbsent(bundle.baseName(), bundle.moduleId());
            if (existingModule != null) {
                throw new IllegalArgumentException("Message bundle base name '" + bundle.baseName()
                        + "' is contributed by both '" + existingModule + "' and '" + bundle.moduleId() + "'");
            }
            namesByModule.computeIfAbsent(bundle.moduleId(), ignored -> new ArrayList<>()).add(bundle.baseName());
        }
        var immutableNamesByModule = new LinkedHashMap<String, List<String>>();
        namesByModule.forEach((moduleId, baseNames) -> immutableNamesByModule.put(moduleId, List.copyOf(baseNames)));
        baseNamesByModule = Map.copyOf(immutableNamesByModule);
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return PROVIDED_LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... parameters) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(parameters, "parameters");

        var moduleId = moduleId(key);
        var baseNames = baseNamesByModule.get(moduleId);
        if (baseNames != null) {
            for (var candidate : candidateLocales(locale)) {
                for (var baseName : baseNames) {
                    var translation = findTranslation(baseName, candidate, key);
                    if (translation != null) {
                        return new MessageFormat(translation, candidate).format(parameters);
                    }
                }
            }
        }

        var marker = "!" + locale.toLanguageTag() + ": " + key + "!";
        LOGGER.warn("Missing administration translation for key '{}' and locale '{}'; returning {}", key,
                locale.toLanguageTag(), marker);
        return marker;
    }

    private static String moduleId(String key) {
        var separator = key.indexOf('.');
        return separator > 0 ? key.substring(0, separator) : "";
    }

    private static List<Locale> candidateLocales(Locale selectedLocale) {
        return ZH_CN.equals(selectedLocale) ? List.of(ZH_CN) : List.of(selectedLocale, ZH_CN);
    }

    private static String findTranslation(String baseName, Locale locale, String key) {
        try {
            var bundle = ResourceBundle.getBundle(baseName, locale, EXACT_LOCALE_CONTROL);
            return bundle.containsKey(key) ? bundle.getString(key) : null;
        } catch (MissingResourceException ignored) {
            return null;
        }
    }
}
