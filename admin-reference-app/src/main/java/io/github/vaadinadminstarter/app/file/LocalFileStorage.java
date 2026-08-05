package io.github.vaadinadminstarter.app.file;

import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.contracts.file.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/** Development-only filesystem adapter. Persistent file references are opaque UUIDs. */
public final class LocalFileStorage implements FileStorage {
    private final Path directory;

    public LocalFileStorage(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot create file storage directory", exception);
        }
    }

    @Override
    public StoredFile store(String filename, String contentType, InputStream content) {
        Objects.requireNonNull(content, "content must not be null");
        var id = UUID.randomUUID();
        var target = pathFor(id);
        try {
            Files.copy(content, target);
            return new StoredFile(id, safeFilename(filename), safeContentType(contentType), Files.size(target));
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot store uploaded file", exception);
        }
    }

    @Override
    public InputStream open(UUID id) {
        try {
            return Files.newInputStream(pathFor(id));
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot open stored file", exception);
        }
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.deleteIfExists(pathFor(id));
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot delete stored file", exception);
        }
    }

    private Path pathFor(UUID id) {
        return directory.resolve(Objects.requireNonNull(id, "id must not be null").toString());
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "upload";
        }
        var normalized = filename.replace('\\', '/');
        var leaf = Path.of(normalized).getFileName().toString();
        return leaf.replaceAll("[\\p{Cntrl}]", "_");
    }

    private String safeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }
}
