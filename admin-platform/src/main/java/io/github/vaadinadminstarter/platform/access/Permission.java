package io.github.vaadinadminstarter.platform.access;

import java.util.UUID;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public record Permission(UUID id, PermissionCode code) { }
