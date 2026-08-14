package io.github.youngledo.vadmin.contracts.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NamespaceMigrationTest {

    private static final String OLD_NAMESPACE = "io.github.vaadinadminstarter";

    @Test
    void usesTheVadminJavaNamespaceEverywhere() throws IOException {
        Path root = Path.of("..").toAbsolutePath().normalize();
        List<Path> sourceFiles = sourceFiles(root);

        assertThat(sourceFiles.stream().filter(path -> read(path).contains(OLD_NAMESPACE)))
                .isEmpty();
        assertThat(sourceFiles.stream()
                .filter(path -> path.getFileName().toString().equals("Application.java"))
                .map(this::read)
                .findFirst()
                .orElseThrow())
                .contains("@SpringBootApplication(scanBasePackages = \"io.github.youngledo.vadmin\")");
    }

    private static List<Path> sourceFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(NamespaceMigrationTest::isSourceOrRuntimeResource)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .filter(path -> !isNegativeAssertionTest(path))
                    .toList();
        }
    }

    private static boolean isSourceOrRuntimeResource(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return !normalized.contains("/.worktrees/")
                && (normalized.contains("/src/main/java/")
                || normalized.contains("/src/test/java/")
                || normalized.contains("/src/main/resources/"));
    }

    private static boolean isNegativeAssertionTest(Path path) {
        String filename = path.getFileName().toString();
        return filename.equals("NamespaceMigrationTest.java")
                || filename.equals("CurrentDocumentationTest.java");
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }
}
