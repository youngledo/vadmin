package io.github.youngledo.vadmin.starter.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DefaultShellTranslationTest {
    @Test
    void resolvesModuleMetadataThroughTranslationsInsteadOfAHostSpecificLabelMap() throws Exception {
        var source = Files.readString(Path.of("src/main/java/io/github/youngledo/vadmin/starter/views/DefaultMainLayout.java"));
        var home = Files.readString(Path.of("src/main/java/io/github/youngledo/vadmin/starter/views/DefaultHomeView.java"));

        assertThat(source).contains("return text(page.titleKey());").doesNotContain("legacyLabel");
        assertThat(source).contains("new Avatar(user.username())")
                .contains("new MenuBar()")
                .contains("MenuBarVariant.LUMO_ICON")
                .contains("option.setCheckable(true)")
                .contains("option.setChecked(locale.equals(selectedLocale))")
                .contains("rebuildUserMenu(userMenu)")
                .contains("actions.addSeparator()")
                .contains("VaadinIcon.GLOBE.create()")
                .contains("--vaadin-app-layout-drawer-width")
                .contains("margin-inline-start", "var(--lumo-space-m)")
                .contains("margin-inline-end", "var(--lumo-space-m)")
                .contains("navigation.getElement().getStyle().set(\"flex-grow\", \"1\")")
                .contains("var showGroupLabels = visibleGroups.size() > 1;")
                .contains("AdminIconCatalog.create(iconKey)")
                .contains("AdminIconCatalog.createAdminIcon(iconKey)")
                .contains("drawer.setPadding(true)")
                .doesNotContain("addHeader(brand)")
                .doesNotContain("new Popover()");
        assertThat(home).contains("getTranslation(page.titleKey())").contains("getTranslation(page.intentKey())");
    }
}
