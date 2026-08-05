package io.github.vaadinadminstarter.app.customer;

import java.time.Instant;
import java.util.UUID;

public record Customer(UUID id, String name, String email, boolean active, Instant createdAt) { }
