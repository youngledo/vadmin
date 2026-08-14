package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

class PageToolbarTest {
    @Test
    void keepsFiltersSeparateFromResetPrimaryAndSupplementaryActions() {
        var toolbar = new PageToolbar();
        var search = new TextField("Search users");
        var reset = new Button("Reset");
        var create = new Button("Create user");
        var export = new Button("Export");

        toolbar.addFilter(search);
        toolbar.setResetAction(reset);
        toolbar.setPrimaryAction(create);
        toolbar.addAction(export);

        assertThat(toolbar.getFilters().getChildren()).contains(search);
        assertThat(toolbar.getResetAction()).isSameAs(reset);
        assertThat(toolbar.getPrimaryAction()).isSameAs(create);
        assertThat(toolbar.getActions().getChildren()).contains(export, create);
        assertThat(toolbar.getClassNames()).contains("admin-page-controls");
    }

    @Test
    void disablesCommandActionsWhileBusy() {
        var toolbar = new PageToolbar();
        var reset = new Button("Reset");
        var create = new Button("Create user");
        toolbar.setResetAction(reset);
        toolbar.setPrimaryAction(create);

        toolbar.setBusy(true);

        assertThat(reset.isEnabled()).isFalse();
        assertThat(create.isEnabled()).isFalse();
    }
}
