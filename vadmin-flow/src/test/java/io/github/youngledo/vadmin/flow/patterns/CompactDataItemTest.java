package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;

class CompactDataItemTest {
    @Test
    void presentsPrimaryStatusMetadataAndCommandsAsOneCompactEntity() {
        var item = new CompactDataItem("admin");
        item.setStatus("Enabled");
        var version = item.addMetadata("Authentication version", "0");
        var details = new Button("View details");

        item.addActions(details);

        assertThat(item.getPrimaryText()).isEqualTo("admin");
        assertThat(item.getStatus()).isEqualTo("Enabled");
        assertThat(version.getElement().getTag()).isEqualTo("dl");
        assertThat(version.getElement().getChildren().map(element -> element.getTag()))
                .containsExactly("dt", "dd");
        assertThat(item.getMetadata().isVisible()).isTrue();
        assertThat(item.getActions().getChildren()).containsExactly(details);
        assertThat(item.getActions().isVisible()).isTrue();
    }
}
