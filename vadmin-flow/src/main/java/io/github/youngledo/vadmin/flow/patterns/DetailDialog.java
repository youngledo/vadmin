package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.Objects;

/** A read-only, responsive dialog for presenting an entity's already-authorized details. */
public final class DetailDialog extends Dialog implements LocaleChangeObserver {
    private final FormLayout form = new FormLayout();
    private final Button closeAction = new Button();
    private final String titleKey;
    private final boolean translated;
    private String closeActionLabel;

    public DetailDialog(String title) { this(title, false); }
    private DetailDialog(String titleKey, boolean translated) {
        this.titleKey = Objects.requireNonNull(titleKey);
        this.translated = translated;
        closeAction.addClickListener(event -> close());
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("40em", 2));
        var footerActions = new HorizontalLayout(closeAction);
        footerActions.setPadding(false);
        footerActions.setSpacing(true);
        footerActions.setWidthFull();
        footerActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        footerActions.setWrap(true);
        getFooter().add(footerActions);
        add(form);
        updateText();
    }
    public static DetailDialog translated(String titleKey) { return new DetailDialog(titleKey, true); }
    public FormLayout getForm() { return form; }
    public TextField addField(String label, String value) {
        var field = new TextField(Objects.requireNonNull(label));
        field.setValue(Objects.requireNonNull(value));
        field.setReadOnly(true);
        form.add(field);
        return field;
    }
    public Button getCloseAction() { return closeAction; }
    /** Overrides the default close label while retaining the shared dialog action layout. */
    public void setCloseActionLabel(String label) {
        closeActionLabel = Objects.requireNonNull(label);
        closeAction.setText(closeActionLabel);
    }
    @Override public void localeChange(LocaleChangeEvent event) { updateText(); }
    private void updateText() {
        var title = translated ? getTranslation(titleKey) : titleKey;
        setHeaderTitle(title);
        getElement().setAttribute("aria-label", title);
        closeAction.setText(closeActionLabel != null ? closeActionLabel
                : translated ? getTranslation("flow.action.close") : "Close");
    }
}
