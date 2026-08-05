package io.github.vaadinadminstarter.app.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {
    @TempDir
    Path storageDirectory;

    @Test
    void storesMetadataAndMakesContentAvailableByOpaqueIdentifier() throws Exception {
        var storage = new LocalFileStorage(storageDirectory);

        var stored = storage.store("customer-note.txt", "text/plain",
                new ByteArrayInputStream("customer attachment".getBytes()));

        assertThat(stored.filename()).isEqualTo("customer-note.txt");
        assertThat(stored.contentType()).isEqualTo("text/plain");
        assertThat(stored.size()).isEqualTo(19);
        try (var content = storage.open(stored.id())) {
            assertThat(content.readAllBytes()).isEqualTo("customer attachment".getBytes());
        }
    }
}
