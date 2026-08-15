package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

class PageHeaderTest {
    @Test
    void composesHeadingDescriptionLocationAndPageActions() {
        var header = new PageHeader("Users", "Manage application users");
        var location = new Span("Administration");
        var create = new Button("Create user");

        header.setLocation(location);
        header.addAction(create);

        assertThat(header.getTitle()).isEqualTo("Users");
        assertThat(header.getDescription()).isEqualTo("Manage application users");
        assertThat(header.getLocation()).isSameAs(location);
        assertThat(header.getActions().getChildren()).contains(create);
        assertThat(header.getClassNames()).contains("admin-page-header");
        assertThat(header.isSpacing()).isTrue();
    }
}
