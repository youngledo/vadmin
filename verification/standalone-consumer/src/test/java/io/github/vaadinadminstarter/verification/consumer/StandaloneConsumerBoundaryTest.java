package io.github.vaadinadminstarter.verification.consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StandaloneConsumerBoundaryTest {
    @Test
    void pomUsesExternalParentAndNeverReferencesTheReferenceApplication() throws IOException {
        var pom = Files.readString(Path.of("pom.xml"));

        assertTrue(pom.contains("<groupId>org.springframework.boot</groupId>"));
        assertTrue(pom.contains("<artifactId>spring-boot-starter-parent</artifactId>"));
        assertFalse(pom.contains("<artifactId>vaadin-admin-starter</artifactId>"));
        assertFalse(pom.contains("<artifactId>admin-reference-app</artifactId>"));
    }
}
