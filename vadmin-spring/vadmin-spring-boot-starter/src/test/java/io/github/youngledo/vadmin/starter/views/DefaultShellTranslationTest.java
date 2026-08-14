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
        assertThat(home).contains("getTranslation(page.titleKey())").contains("getTranslation(page.intentKey())");
    }
}
