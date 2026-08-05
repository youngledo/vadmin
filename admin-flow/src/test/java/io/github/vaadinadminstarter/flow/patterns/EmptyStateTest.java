package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;

class EmptyStateTest {
    @Test
    void composesClearCopyAndAnOptionalRecoveryAction() {
        var emptyState = new EmptyState("No users", "Create a user to begin.");
        var create = new Button("Create user");

        emptyState.setAction(create);

        assertThat(emptyState.getTitle()).isEqualTo("No users");
        assertThat(emptyState.getDescription()).isEqualTo("Create a user to begin.");
        assertThat(emptyState.getAction()).isSameAs(create);
    }
}
