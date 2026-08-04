package io.github.vaadinadminstarter.platform.access;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public record GrantPermissionCommand(String roleCode, PermissionCode permissionCode) { }
