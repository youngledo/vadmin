package io.github.vaadinadminstarter.flow.navigation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class PageRegistryTest {
    @Test
    void rejectsDuplicatePageIds() {
        var registry = new PageRegistry(List.of(PageDefinition.of("system-users"), PageDefinition.of("system-users")));

        assertThatThrownBy(registry::validate).isInstanceOf(IllegalStateException.class);
    }
}
