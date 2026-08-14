package io.github.youngledo.vadmin.contracts.file;

import java.io.InputStream;
import java.util.UUID;

public interface FileStorage {
    StoredFile store(String filename, String contentType, InputStream content);
    InputStream open(UUID id);
    void delete(UUID id);
}
