package io.github.vaadinadminstarter.springflow.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import io.github.vaadinadminstarter.flow.navigation.AdminMessageBundle;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;

/** Validates the default-locale translation contract before administration UIs are created. */
public final class AdminMessageBundleValidator {
    private static final ResourceBundle.Control EXACT_LOCALE_CONTROL = new ResourceBundle.Control() {
        @Override
        public List<java.util.Locale> getCandidateLocales(String baseName, java.util.Locale locale) {
            return List.of(CompositeAdminI18NProvider.ZH_CN);
        }

        @Override
        public java.util.Locale getFallbackLocale(String baseName, java.util.Locale locale) {
            return null;
        }
    };

    private AdminMessageBundleValidator() {
    }

    public static void validate(List<AdminMessageBundle> allBundles, List<AdminModule> modules) {
        var bundlesByModule = new HashMap<String, List<BundleResource>>();
        for (var bundle : allBundles) {
            var resource = loadDefaultBundle(bundle);
            bundlesByModule.computeIfAbsent(bundle.moduleId(), ignored -> new ArrayList<>()).add(resource);
        }

        var validatedGroups = new HashSet<String>();
        for (var module : modules) {
            var resources = bundlesByModule.getOrDefault(module.moduleId(), List.of());
            for (var group : module.navigationGroups()) {
                if (validatedGroups.add(group.id())) {
                    requireKey(module.moduleId(), group.titleKey(), resources);
                }
            }
            for (var page : module.pages()) {
                requireKey(module.moduleId(), page.titleKey(), resources);
                requireKey(module.moduleId(), page.intentKey(), resources);
            }
        }
    }

    private static BundleResource loadDefaultBundle(AdminMessageBundle descriptor) {
        try {
            return new BundleResource(descriptor.baseName(), ResourceBundle.getBundle(descriptor.baseName(),
                    CompositeAdminI18NProvider.ZH_CN, EXACT_LOCALE_CONTROL));
        } catch (MissingResourceException exception) {
            throw new IllegalStateException("Missing default zh-CN administration message bundle for module '"
                    + descriptor.moduleId() + "', base name '" + descriptor.baseName() + "'", exception);
        }
    }

    private static void requireKey(String moduleId, String key, List<BundleResource> resources) {
        for (var resource : resources) {
            if (resource.bundle().containsKey(key)) {
                return;
            }
        }
        var baseName = resources.isEmpty() ? "<none>" : resources.stream()
                .map(BundleResource::baseName)
                .sorted()
                .reduce((first, second) -> first + ", " + second)
                .orElse("<none>");
        throw new IllegalStateException("Missing default zh-CN translation for module '" + moduleId
                + "', key '" + key + "', base name '" + baseName + "'");
    }

    private record BundleResource(String baseName, ResourceBundle bundle) {
    }
}
