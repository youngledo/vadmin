package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;

/** A confirmation surface that executes a command only from its explicit confirm action. */
public final class ConfirmationDialog extends Dialog {
    private final Button confirmAction;
    private final Button cancelAction;
    private final Div failure = new Div();
    private boolean busy;
    private boolean confirmActionEnabledBeforeBusy;
    private boolean cancelActionEnabledBeforeBusy;
    private boolean closeOnEscBeforeBusy;
    private boolean closeOnOutsideClickBeforeBusy;

    public ConfirmationDialog(String title, String consequence, String confirmActionLabel, Runnable onConfirm) {
        setHeaderTitle(Objects.requireNonNull(title));
        getElement().setAttribute("aria-label", title);
        failure.getElement().setAttribute("role", "alert");
        failure.setVisible(false);
        add(new Paragraph(Objects.requireNonNull(consequence)), failure);

        confirmAction = new Button(Objects.requireNonNull(confirmActionLabel),
                event -> Objects.requireNonNull(onConfirm).run());
        cancelAction = new Button("Cancel", event -> close());
        cancelAction.getElement().setAttribute("aria-label", "Cancel confirmation");

        var footerActions = new HorizontalLayout(cancelAction, confirmAction);
        footerActions.setPadding(false);
        footerActions.setSpacing(true);
        footerActions.setWidthFull();
        footerActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        footerActions.getStyle().set("flex-wrap", "wrap");
        getFooter().add(footerActions);
    }

    public Button getConfirmAction() {
        return confirmAction;
    }

    public Button getCancelAction() {
        return cancelAction;
    }

    public boolean isBusy() {
        return busy;
    }

    public String getFailureMessage() {
        return failure.getText();
    }

    public void showFailureMessage(String message) {
        failure.setText(Objects.requireNonNull(message));
        failure.setVisible(!message.isBlank());
    }

    /** Temporarily disables commands and closing, then restores their previous policies. */
    public void setBusy(boolean busy) {
        if (this.busy == busy) {
            return;
        }
        this.busy = busy;
        if (busy) {
            confirmActionEnabledBeforeBusy = confirmAction.isEnabled();
            cancelActionEnabledBeforeBusy = cancelAction.isEnabled();
            closeOnEscBeforeBusy = isCloseOnEsc();
            closeOnOutsideClickBeforeBusy = isCloseOnOutsideClick();
            confirmAction.setEnabled(false);
            cancelAction.setEnabled(false);
            setCloseOnEsc(false);
            setCloseOnOutsideClick(false);
        } else {
            confirmAction.setEnabled(confirmActionEnabledBeforeBusy);
            cancelAction.setEnabled(cancelActionEnabledBeforeBusy);
            setCloseOnEsc(closeOnEscBeforeBusy);
            setCloseOnOutsideClick(closeOnOutsideClickBeforeBusy);
        }
    }
}
