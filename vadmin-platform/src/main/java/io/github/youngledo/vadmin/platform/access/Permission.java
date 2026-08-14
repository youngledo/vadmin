package io.github.youngledo.vadmin.platform.access;

import java.util.UUID;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

public record Permission(UUID id, PermissionCode code) { }
