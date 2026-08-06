package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

class DetailDialogTest {
    @Test
    void composesReadOnlyLabeledFieldsInAResponsiveDetailSurface() {
        var dialog = new DetailDialog("User details");

        var username = dialog.addField("Username", "ada");
        var status = dialog.addField("Status", "Enabled");

        assertThat(dialog.getForm().getChildren()).containsExactly(username, status);
        assertThat(username).isInstanceOf(TextField.class);
        assertThat(username.getLabel()).isEqualTo("Username");
        assertThat(username.getValue()).isEqualTo("ada");
        assertThat(username.isReadOnly()).isTrue();
        assertThat(status.isReadOnly()).isTrue();
    }

    @Test
    void closesFromItsStandardAccessibleFooterAction() {
        var dialog = new DetailDialog("User details");

        dialog.getCloseAction().click();

        assertThat(dialog.isOpened()).isFalse();
    }
}
