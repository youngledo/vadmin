package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ConfirmationDialogTest {
    @Test
    void invokesTheCommandOnlyAfterExplicitConfirmation() {
        var invocations = new AtomicInteger();
        var dialog = new ConfirmationDialog("Disable user", "The user will lose access.", "Disable",
                invocations::incrementAndGet);

        dialog.getConfirmAction().click();

        assertThat(invocations).hasValue(1);
    }

    @Test
    void marksTheConsequenceForHostOwnedDangerPresentation() {
        var dialog = new ConfirmationDialog("Delete customer", "Deletion cannot be undone.", "Delete", () -> { });

        assertThat(dialog.getChildren().filter(component -> component.getClassNames()
                .contains("admin-confirmation-consequence"))).hasSize(1);
    }

    @Test
    void cancelNeverInvokesTheConfirmationCommand() {
        var invocations = new AtomicInteger();
        var dialog = new ConfirmationDialog("Delete customer", "Deletion cannot be undone.", "Delete",
                invocations::incrementAndGet);

        dialog.getCancelAction().click();

        assertThat(invocations).hasValue(0);
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void preventsEscapeAndOutsideClosingWhileBusyAndRestoresThePriorPolicy() {
        var dialog = new ConfirmationDialog("Delete customer", "Deletion cannot be undone.", "Delete", () -> { });
        dialog.setCloseOnEsc(false);

        dialog.setBusy(true);

        assertThat(dialog.getConfirmAction().isEnabled()).isFalse();
        assertThat(dialog.getCancelAction().isEnabled()).isFalse();
        assertThat(dialog.isCloseOnEsc()).isFalse();
        assertThat(dialog.isCloseOnOutsideClick()).isFalse();

        dialog.setBusy(false);

        assertThat(dialog.getConfirmAction().isEnabled()).isTrue();
        assertThat(dialog.getCancelAction().isEnabled()).isTrue();
        assertThat(dialog.isCloseOnEsc()).isFalse();
        assertThat(dialog.isCloseOnOutsideClick()).isTrue();
    }

    @Test
    void putsCancelThenConfirmInAWrappingFooterRow() {
        var dialog = new ConfirmationDialog("Delete customer", "Deletion cannot be undone.", "Delete", () -> { });
        var footerActions = (HorizontalLayout) dialog.getFooter().getElement().getChildren()
                .findFirst().flatMap(element -> element.getComponent()).orElseThrow();

        assertThat(footerActions.getChildren().toList())
                .containsExactly(dialog.getCancelAction(), dialog.getConfirmAction());
        assertThat(footerActions.getWidth()).isEqualTo("100%");
        assertThat(footerActions.isWrap()).isTrue();
    }

    @Test
    void exposesLocalFailureFeedbackWithoutExecutingAnotherCommand() {
        var dialog = new ConfirmationDialog("Delete customer", "Deletion cannot be undone.", "Delete", () -> { });

        dialog.showFailureMessage("The customer has attachments that must be removed first.");

        assertThat(dialog.getFailureMessage())
                .isEqualTo("The customer has attachments that must be removed first.");
    }
}
