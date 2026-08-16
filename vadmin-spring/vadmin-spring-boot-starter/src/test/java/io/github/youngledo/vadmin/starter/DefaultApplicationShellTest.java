package io.github.youngledo.vadmin.starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DefaultApplicationShellTest {
    @Test
    void loadsLumoAsTheDefaultVaadinVisualLanguage() {
        var stylesheets = Arrays.stream(DefaultApplicationShell.class.getAnnotationsByType(StyleSheet.class))
                .map(StyleSheet::value)
                .toList();

        assertThat(DefaultApplicationShell.class.getAnnotation(Theme.class).themeClass()).isEqualTo(Lumo.class);
        assertThat(stylesheets).containsExactly("vadmin/ant.css");
    }
}
