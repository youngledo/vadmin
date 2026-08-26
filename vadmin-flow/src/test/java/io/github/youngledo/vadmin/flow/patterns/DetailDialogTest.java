package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.html.DescriptionList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.Test;

class DetailDialogTest {
    @Test
    void composesSemanticDescriptionsInAResponsiveDetailSurface() {
        var dialog = new DetailDialog("User details");

        var username = dialog.addField("Username", "ada");
        var status = dialog.addField("Status", "Enabled");

        assertThat(dialog.getForm().getChildren()).containsExactly(username, status);
        assertThat(username).isInstanceOf(DescriptionList.class);
        assertThat(username.getElement().getTag()).isEqualTo("dl");
        assertThat(username.getElement().getChildren().map(element -> element.getTag()))
                .containsExactly("dt", "dd");
        assertThat(username.getLabel()).isEqualTo("Username");
        assertThat(username.getValue()).isEqualTo("ada");
        assertThat(status.getLabel()).isEqualTo("Status");
        assertThat(status.getValue()).isEqualTo("Enabled");
    }

    @Test
    void closesFromItsStandardAccessibleFooterAction() {
        var dialog = new DetailDialog("User details");

        dialog.getCloseAction().click();

        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void usesTheNativeWrappingFooterLayout() {
        var dialog = new DetailDialog("User details");
        var footerActions = (HorizontalLayout) dialog.getFooter().getElement().getChildren()
                .findFirst().flatMap(element -> element.getComponent()).orElseThrow();

        assertThat(footerActions.isWrap()).isTrue();
    }
}
