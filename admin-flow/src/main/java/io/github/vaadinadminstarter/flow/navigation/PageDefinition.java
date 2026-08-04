package io.github.vaadinadminstarter.flow.navigation;

import io.github.vaadinadminstarter.contracts.auth.PermissionCode;

public record PageDefinition(String pageId, String titleKey, String iconKey, int order, String route,
                             PermissionCode requiredPermission) {
    public static PageDefinition of(String pageId) {
        return new PageDefinition(pageId, pageId, "file", 0, pageId,
                PermissionCode.of("system:user:read"));
    }
}
