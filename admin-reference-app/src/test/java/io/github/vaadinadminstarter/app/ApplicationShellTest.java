package io.github.vaadinadminstarter.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.junit.jupiter.api.Test;

class ApplicationShellTest {
    @Test
    void registersTheThemeOnADedicatedApplicationShell() {
        assertThat(Application.class.isAnnotationPresent(Theme.class)).isFalse();

        assertThat(AppShellConfigurator.class.isAssignableFrom(ApplicationShell.class)).isTrue();
        assertThat(ApplicationShell.class.getAnnotation(Theme.class)).isNotNull();
        assertThat(ApplicationShell.class.getAnnotation(Theme.class).value()).isEqualTo("admin-theme");
    }
}
