package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.Objects;

/** A read-only, responsive dialog for presenting an entity's already-authorized details. */
public final class DetailDialog extends Dialog {
    private final FormLayout form = new FormLayout();
    private final Button closeAction = new Button("Close", event -> close());

    public DetailDialog(String title) {
        setHeaderTitle(Objects.requireNonNull(title));
        getElement().setAttribute("aria-label", title);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40em", 2));

        var footerActions = new HorizontalLayout(closeAction);
        footerActions.setPadding(false);
        footerActions.setSpacing(true);
        footerActions.setWidthFull();
        footerActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        footerActions.getStyle().set("flex-wrap", "wrap");
        getFooter().add(footerActions);
        add(form);
    }

    public FormLayout getForm() {
        return form;
    }

    /** Adds a standard Flow text field configured for display-only detail data. */
    public TextField addField(String label, String value) {
        var field = new TextField(Objects.requireNonNull(label));
        field.setValue(Objects.requireNonNull(value));
        field.setReadOnly(true);
        form.add(field);
        return field;
    }

    public Button getCloseAction() {
        return closeAction;
    }
}
