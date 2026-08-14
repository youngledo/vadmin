package io.github.youngledo.vadmin.contracts.navigation;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

public record ActionDefinition(String actionId, PermissionCode requiredPermission, boolean destructive) { }
