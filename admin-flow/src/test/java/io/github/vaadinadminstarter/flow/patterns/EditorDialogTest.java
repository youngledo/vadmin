package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.textfield.TextField;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class EditorDialogTest {
    @Test
    void invokesPrimaryActionAndProvidesTheNativeResponsiveForm() {
        var saves = new AtomicInteger();
        var dialog = new EditorDialog("Create user", "Save", saves::incrementAndGet);
        var username = new TextField("Username");

        dialog.addField(username);
        dialog.getPrimaryAction().click();

        assertThat(saves).hasValue(1);
        assertThat(dialog.getForm().getChildren()).contains(username);
    }

    @Test
    void closesWhenTheAccessibleCancelActionIsInvoked() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });

        dialog.getCancelAction().click();

        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void disablesActionsAndAnnouncesValidationWhenBusyOrInvalid() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });

        dialog.showValidationMessage("Username is required");
        dialog.setBusy(true);

        assertThat(dialog.getValidationMessage()).isEqualTo("Username is required");
        assertThat(dialog.getPrimaryAction().isEnabled()).isFalse();
        assertThat(dialog.getCancelAction().isEnabled()).isFalse();
    }

    @Test
    void restoresThePriorPrimaryActionAvailabilityAfterBusyState() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });
        dialog.getPrimaryAction().setEnabled(false);

        dialog.setBusy(true);
        dialog.setBusy(false);

        assertThat(dialog.getPrimaryAction().isEnabled()).isFalse();
    }
}
