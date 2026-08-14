package io.github.vaadinadminstarter.contracts.file;

import java.util.UUID;

public record StoredFile(UUID id, String filename, String contentType, long size) { }
