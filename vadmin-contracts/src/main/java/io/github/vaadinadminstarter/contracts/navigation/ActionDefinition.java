package io.github.vaadinadminstarter.contracts.navigation;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public record ActionDefinition(String actionId, PermissionCode requiredPermission, boolean destructive) { }
