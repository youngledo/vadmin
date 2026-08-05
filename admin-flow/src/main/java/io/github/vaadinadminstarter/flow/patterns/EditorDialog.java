package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;

/** A modal editor surface with responsive fields, validation feedback, and standard commands. */
public final class EditorDialog extends Dialog {
    private final FormLayout form = new FormLayout();
    private final Div validation = new Div();
    private final Button primaryAction;
    private final Button cancelAction;
    private boolean busy;
    private boolean primaryActionEnabledBeforeBusy;
    private boolean cancelActionEnabledBeforeBusy;

    public EditorDialog(String title, String primaryActionLabel, Runnable onPrimaryAction) {
        setHeaderTitle(Objects.requireNonNull(title));
        getElement().setAttribute("aria-label", title);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40em", 2));
        validation.getElement().setAttribute("role", "alert");
        validation.setVisible(false);
        primaryAction = new Button(Objects.requireNonNull(primaryActionLabel),
                event -> Objects.requireNonNull(onPrimaryAction).run());
        cancelAction = new Button("Cancel", event -> close());
        cancelAction.getElement().setAttribute("aria-label", "Cancel editor");
        var commands = new HorizontalLayout(cancelAction, primaryAction);
        commands.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        add(form, validation, commands);
    }

    public FormLayout getForm() {
        return form;
    }

    public void addField(Component... fields) {
        form.add(fields);
    }

    public Button getPrimaryAction() {
        return primaryAction;
    }

    public Button getCancelAction() {
        return cancelAction;
    }

    public String getValidationMessage() {
        return validation.getText();
    }

    public void showValidationMessage(String message) {
        validation.setText(Objects.requireNonNull(message));
        validation.setVisible(!message.isBlank());
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        if (this.busy == busy) {
            return;
        }
        this.busy = busy;
        if (busy) {
            primaryActionEnabledBeforeBusy = primaryAction.isEnabled();
            cancelActionEnabledBeforeBusy = cancelAction.isEnabled();
            primaryAction.setEnabled(false);
            cancelAction.setEnabled(false);
        } else {
            primaryAction.setEnabled(primaryActionEnabledBeforeBusy);
            cancelAction.setEnabled(cancelActionEnabledBeforeBusy);
        }
    }
}
