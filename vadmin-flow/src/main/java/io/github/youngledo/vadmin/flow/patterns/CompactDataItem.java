package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.DescriptionList;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Objects;

/** A compact, semantic entity row for narrow data workspaces. */
public final class CompactDataItem extends VerticalLayout {
    private final Span primaryText = new Span();
    private final Span status = new Span();
    private final HorizontalLayout metadata = new HorizontalLayout();
    private final HorizontalLayout actions = new HorizontalLayout();

    public CompactDataItem(String primaryText) {
        this.primaryText.setText(Objects.requireNonNull(primaryText));
        this.primaryText.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
        this.primaryText.getStyle().set("overflow-wrap", "anywhere");
        status.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        status.setVisible(false);

        var header = new HorizontalLayout(this.primaryText, status);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(true);
        header.setWrap(true);
        header.setAlignItems(Alignment.BASELINE);
        header.setFlexGrow(1, this.primaryText);
        header.setFlexShrink(0, status);

        metadata.setWidthFull();
        metadata.setPadding(false);
        metadata.setSpacing(true);
        metadata.setWrap(true);
        metadata.setVisible(false);

        actions.setWidthFull();
        actions.setPadding(false);
        actions.setSpacing(true);
        actions.setWrap(true);
        actions.setVisible(false);

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_20);
        add(header, metadata, actions);
    }

    public String getPrimaryText() {
        return primaryText.getText();
    }

    public void setStatus(String value) {
        status.setText(Objects.requireNonNull(value));
        status.setVisible(!value.isBlank());
    }

    public String getStatus() {
        return status.getText();
    }

    /** Adds one semantic label-value pair to the wrapping metadata row. */
    public DescriptionList addMetadata(String label, String value) {
        var field = new DescriptionList();
        var term = new DescriptionList.Term(Objects.requireNonNull(label));
        var description = new DescriptionList.Description(Objects.requireNonNull(value));
        field.addClassNames(LumoUtility.Display.INLINE_FLEX, LumoUtility.Gap.XSMALL,
                LumoUtility.Margin.NONE, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        description.addClassNames(LumoUtility.Margin.NONE);
        description.getStyle().set("overflow-wrap", "anywhere");
        field.add(term, description);
        metadata.add(field);
        metadata.setVisible(true);
        return field;
    }

    public void addActions(Component... commands) {
        actions.add(commands);
        actions.setVisible(actions.getComponentCount() > 0);
    }

    public HorizontalLayout getMetadata() {
        return metadata;
    }

    public HorizontalLayout getActions() {
        return actions;
    }
}
