package io.github.youngledo.vadmin.contracts.navigation;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

public record NavigationEntry(String pageId, String titleKey, String iconKey, int order, PermissionCode requiredPermission) { }
