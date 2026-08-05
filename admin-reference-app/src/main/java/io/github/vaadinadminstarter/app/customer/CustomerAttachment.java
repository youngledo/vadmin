package io.github.vaadinadminstarter.app.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerAttachment(UUID id, UUID customerId, UUID storedFileId, String filename, String contentType,
                                 long size, Instant createdAt) { }
