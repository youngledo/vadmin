package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;

/** A confirmation surface that executes a command only from its explicit confirm action. */
public final class ConfirmationDialog extends Dialog implements LocaleChangeObserver {
    private final Button confirmAction;
    private final Button cancelAction;
    private final Paragraph consequence = new Paragraph();
    private final Div failure = new Div();
    private final String titleKey;
    private final String consequenceKey;
    private final String confirmActionKey;
    private final boolean translated;
    private boolean busy;
    private boolean confirmActionEnabledBeforeBusy;
    private boolean cancelActionEnabledBeforeBusy;
    private boolean closeOnEscBeforeBusy;
    private boolean closeOnOutsideClickBeforeBusy;

    public ConfirmationDialog(String title, String consequence, String confirmActionLabel, Runnable onConfirm) {
        this(title, consequence, confirmActionLabel, onConfirm, false);
    }

    private ConfirmationDialog(String titleKey, String consequenceKey, String confirmActionKey, Runnable onConfirm,
                               boolean translated) {
        this.titleKey = Objects.requireNonNull(titleKey);
        this.consequenceKey = Objects.requireNonNull(consequenceKey);
        this.confirmActionKey = Objects.requireNonNull(confirmActionKey);
        this.translated = translated;
        failure.getElement().setAttribute("role", "alert");
        failure.setVisible(false);
        add(consequence, failure);
        confirmAction = new Button();
        confirmAction.addClickListener(event -> {
            Objects.requireNonNull(onConfirm).run();
            close();
        });
        cancelAction = new Button();
        cancelAction.addClickListener(event -> close());
        var footerActions = new HorizontalLayout(cancelAction, confirmAction);
        footerActions.setPadding(false);
        footerActions.setSpacing(true);
        footerActions.setWidthFull();
        footerActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        footerActions.getStyle().set("flex-wrap", "wrap");
        getFooter().add(footerActions);
        updateText();
    }

    /** Creates a confirmation surface whose static text follows the active UI locale. */
    public static ConfirmationDialog translated(String titleKey, String consequenceKey, String confirmActionKey,
                                                Runnable onConfirm) {
        return new ConfirmationDialog(titleKey, consequenceKey, confirmActionKey, onConfirm, true);
    }

    public Button getConfirmAction() { return confirmAction; }
    public Button getCancelAction() { return cancelAction; }
    public boolean isBusy() { return busy; }
    public String getFailureMessage() { return failure.getText(); }

    public void showFailureMessage(String message) {
        failure.setText(Objects.requireNonNull(message));
        failure.setVisible(!message.isBlank());
    }

    /** Temporarily disables commands and closing, then restores their previous policies. */
    public void setBusy(boolean busy) {
        if (this.busy == busy) return;
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

    @Override public void localeChange(LocaleChangeEvent event) { updateText(); }

    private void updateText() {
        var title = text(titleKey);
        setHeaderTitle(title);
        getElement().setAttribute("aria-label", title);
        consequence.setText(text(consequenceKey));
        confirmAction.setText(text(confirmActionKey));
        cancelAction.setText(translated ? getTranslation("flow.action.cancel") : "Cancel");
    }

    private String text(String value) { return translated ? getTranslation(value) : value; }
}
