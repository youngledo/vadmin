package io.github.youngledo.vadmin.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CurrentDocumentationTest {
    private static final List<String> CURRENT_GUIDES = List.of(
            "README.md",
            "docs/en/architecture.md",
            "docs/en/quick-start.md",
            "docs/en/extension-guide.md",
            "docs/en/release-guide.md",
            "docs/zh-CN/architecture.md",
            "docs/zh-CN/quick-start.md",
            "docs/zh-CN/extension-guide.md",
            "docs/zh-CN/release-guide.md");

    @Test
    void currentAdoptionGuidesDescribeTheStarterInsteadOfRetiredExamplesOrARequiredCustomShell() throws IOException {
        var repositoryRoot = Path.of("").toAbsolutePath().getParent();
        for (var guide : CURRENT_GUIDES) {
            var content = Files.readString(repositoryRoot.resolve(guide));
            assertThat(content)
                    .as(guide)
                    .doesNotContain("admin-example-orders", "CustomersView", "/orders", "/customers")
                    .doesNotContain("must create `MainLayout`", "必须创建 `MainLayout`",
                            "InventoryViewProductionAnchor")
                    .contains("admin-spring-starter");
        }
    }
}
