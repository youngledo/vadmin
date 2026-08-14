package io.github.youngledo.vadmin.flow.navigation;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.Component;

import io.github.youngledo.vadmin.contracts.auth.PermissionCode;

/** Immutable metadata for a permission-protected administration page. */
public record AdminPage(String pageId, String groupId, String titleKey, String intentKey, String iconKey, int order,
                        String route, PermissionCode requiredPermission, Class<? extends Component> viewType)
        implements Serializable {
    public AdminPage {
        pageId = AdminNavigationGroup.requireText(pageId, "pageId");
        groupId = AdminNavigationGroup.requireText(groupId, "groupId");
        titleKey = AdminNavigationGroup.requireText(titleKey, "titleKey");
        intentKey = AdminNavigationGroup.requireText(intentKey, "intentKey");
        iconKey = AdminNavigationGroup.requireText(iconKey, "iconKey");
        route = AdminNavigationGroup.requireText(route, "route");
        requiredPermission = Objects.requireNonNull(requiredPermission, "requiredPermission");
        viewType = Objects.requireNonNull(viewType, "viewType");
    }
}
