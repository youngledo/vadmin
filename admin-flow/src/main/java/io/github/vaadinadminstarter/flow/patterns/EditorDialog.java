package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;

/** A modal editor surface with responsive fields, validation feedback, and standard commands. */
public final class EditorDialog extends Dialog implements LocaleChangeObserver {
    private final FormLayout form = new FormLayout();
    private final Div validation = new Div();
    private final Button primaryAction;
    private final Button cancelAction;
    private final String titleKey;
    private final String primaryActionKey;
    private final boolean translated;
    private boolean busy;
    private boolean primaryActionEnabledBeforeBusy;
    private boolean cancelActionEnabledBeforeBusy;
    private boolean closeOnEscBeforeBusy;
    private boolean closeOnOutsideClickBeforeBusy;

    public EditorDialog(String title, String primaryActionLabel, Runnable onPrimaryAction) {
        this(title, primaryActionLabel, onPrimaryAction, false);
    }
    private EditorDialog(String titleKey, String primaryActionKey, Runnable onPrimaryAction, boolean translated) {
        this.titleKey = Objects.requireNonNull(titleKey);
        this.primaryActionKey = Objects.requireNonNull(primaryActionKey);
        this.translated = translated;
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("40em", 2));
        validation.getElement().setAttribute("role", "alert");
        validation.setVisible(false);
        primaryAction = new Button();
        primaryAction.addClickListener(event -> Objects.requireNonNull(onPrimaryAction).run());
        cancelAction = new Button();
        cancelAction.addClickListener(event -> close());
        var footerActions = new HorizontalLayout(cancelAction, primaryAction);
        footerActions.setPadding(false);
        footerActions.setSpacing(true);
        footerActions.setWidthFull();
        footerActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        footerActions.getStyle().set("flex-wrap", "wrap");
        getFooter().add(footerActions);
        add(form, validation);
        updateText();
    }
    public static EditorDialog translated(String titleKey, String primaryActionKey, Runnable onPrimaryAction) {
        return new EditorDialog(titleKey, primaryActionKey, onPrimaryAction, true);
    }
    public FormLayout getForm() { return form; }
    public void addField(Component... fields) { form.add(fields); }
    public Button getPrimaryAction() { return primaryAction; }
    public Button getCancelAction() { return cancelAction; }
    public String getValidationMessage() { return validation.getText(); }
    public void showValidationMessage(String message) { validation.setText(Objects.requireNonNull(message)); validation.setVisible(!message.isBlank()); }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) {
        if (this.busy == busy) return;
        this.busy = busy;
        if (busy) {
            primaryActionEnabledBeforeBusy = primaryAction.isEnabled(); cancelActionEnabledBeforeBusy = cancelAction.isEnabled();
            closeOnEscBeforeBusy = isCloseOnEsc(); closeOnOutsideClickBeforeBusy = isCloseOnOutsideClick();
            primaryAction.setEnabled(false); cancelAction.setEnabled(false); setCloseOnEsc(false); setCloseOnOutsideClick(false);
        } else {
            primaryAction.setEnabled(primaryActionEnabledBeforeBusy); cancelAction.setEnabled(cancelActionEnabledBeforeBusy);
            setCloseOnEsc(closeOnEscBeforeBusy); setCloseOnOutsideClick(closeOnOutsideClickBeforeBusy);
        }
    }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); }
    private void updateText() {
        var title = translated ? getTranslation(titleKey) : titleKey;
        setHeaderTitle(title); getElement().setAttribute("aria-label", title);
        primaryAction.setText(translated ? getTranslation(primaryActionKey) : primaryActionKey);
        cancelAction.setText(translated ? getTranslation("flow.action.cancel") : "Cancel");
    }
}
