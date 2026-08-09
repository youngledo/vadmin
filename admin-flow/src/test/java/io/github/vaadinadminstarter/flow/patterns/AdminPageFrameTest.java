package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;

class AdminPageFrameTest {
    @Test
    void composesHeaderControlsAndWorkspaceInStableSemanticOrder() {
        var header = new PageHeader("Users", "Manage application users");
        var controls = new PageToolbar();
        var workspace = new Div();

        var frame = new AdminPageFrame(header, controls, workspace);

        assertThat(frame.getClassNames()).contains("admin-page-frame");
        assertThat(frame.getComponentCount()).isEqualTo(3);
        assertThat(frame.getComponentAt(0)).isSameAs(header);
        assertThat(frame.getComponentAt(1)).isSameAs(controls);
        assertThat(frame.getComponentAt(2)).isSameAs(workspace);
        assertThat(header.getClassNames()).contains("admin-page-header");
        assertThat(controls.getClassNames()).contains("admin-page-controls");
        assertThat(workspace.getClassNames()).contains("admin-page-workspace");
    }

    @Test
    void omitsControlsWithoutWrappingThePageInAnAdditionalSurface() {
        var header = new PageHeader("Audit log");
        var workspace = new Div();

        var frame = new AdminPageFrame(header, null, workspace);

        assertThat(frame.getComponentCount()).isEqualTo(2);
        assertThat(frame.getComponentAt(0)).isSameAs(header);
        assertThat(frame.getComponentAt(1)).isSameAs(workspace);
        assertThat(frame.getClassNames()).doesNotContain("admin-page-card", "admin-page-surface");
    }
}
