package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

    @Test
    void preventsEscapeAndOutsideClosingWhileBusyAndRestoresThePriorPolicy() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });

        dialog.setBusy(true);

        assertThat(dialog.isCloseOnEsc()).isFalse();
        assertThat(dialog.isCloseOnOutsideClick()).isFalse();

        dialog.setBusy(false);

        assertThat(dialog.isCloseOnEsc()).isTrue();
        assertThat(dialog.isCloseOnOutsideClick()).isTrue();
    }

    @Test
    void preservesAnExistingNoClosePolicyAfterBusyState() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        dialog.setBusy(true);
        dialog.setBusy(false);

        assertThat(dialog.isCloseOnEsc()).isFalse();
        assertThat(dialog.isCloseOnOutsideClick()).isFalse();
    }

    @Test
    void placesVisibleCommandsInAWrappingFooterRowInCancelThenPrimaryOrder() {
        var dialog = new EditorDialog("Create user", "Save", () -> { });
        var footerActions = (HorizontalLayout) dialog.getFooter().getElement().getChildren()
                .findFirst().flatMap(element -> element.getComponent()).orElseThrow();

        assertThat(dialog.getFooter().getElement().getChildren().toList())
                .containsExactly(footerActions.getElement());
        assertThat(footerActions.getChildren().toList())
                .containsExactly(dialog.getCancelAction(), dialog.getPrimaryAction());
        assertThat(footerActions.getWidth()).isEqualTo("100%");
        assertThat(footerActions.getStyle().get("flex-wrap")).isEqualTo("wrap");
    }
}
