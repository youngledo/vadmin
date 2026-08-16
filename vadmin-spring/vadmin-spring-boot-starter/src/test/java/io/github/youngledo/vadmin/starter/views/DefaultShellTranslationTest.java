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
                .contains("new Popover()")
                .contains("--vaadin-app-layout-drawer-width")
                .contains("navigation.setWidthFull()")
                .contains("drawer.setPadding(false)")
                .contains("header.setPadding(false)")
                .doesNotContain("new MenuBar()");
        assertThat(home).contains("getTranslation(page.titleKey())").contains("getTranslation(page.intentKey())");
    }
}
