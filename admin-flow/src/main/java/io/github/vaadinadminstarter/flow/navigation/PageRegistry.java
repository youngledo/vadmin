package io.github.vaadinadminstarter.flow.navigation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUser;

public final class PageRegistry {
    private final List<PageDefinition> pages;
    public PageRegistry(List<PageDefinition> pages) { this.pages = List.copyOf(pages); }
    public void validate() {
        Set<String> ids = pages.stream().map(PageDefinition::pageId).collect(Collectors.toSet());
        if (ids.size() != pages.size()) throw new IllegalStateException("Duplicate page id");
    }
    public List<PageDefinition> visibleTo(CurrentUser user, AuthorizationService authorization) {
        return pages.stream().filter(page -> authorization.hasPermission(user, page.requiredPermission()))
                .sorted(java.util.Comparator.comparingInt(PageDefinition::order)).toList();
    }
}
