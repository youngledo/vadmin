package io.github.youngledo.vadmin.platform.access;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

public record GrantPermissionCommand(String roleCode, PermissionCode permissionCode) { }
