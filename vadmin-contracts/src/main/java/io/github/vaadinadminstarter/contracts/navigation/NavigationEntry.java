package io.github.vaadinadminstarter.contracts.navigation;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public record NavigationEntry(String pageId, String titleKey, String iconKey, int order, PermissionCode requiredPermission) { }
