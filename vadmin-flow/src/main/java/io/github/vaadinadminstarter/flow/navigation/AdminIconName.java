package io.github.vaadinadminstarter.flow.navigation;

import com.vaadin.flow.component.icon.VaadinIcon;

/** Closed vocabulary of profile-neutral administration icons. */
public enum AdminIconName {
    ADD("add", VaadinIcon.PLUS),
    ATTACHMENT("attachment", VaadinIcon.PAPERCLIP),
    BRIEFCASE("briefcase", VaadinIcon.BRIEFCASE),
    CLOCK("clock", VaadinIcon.CLOCK),
    CUBE("cube", VaadinIcon.CUBE),
    DELETE("delete", VaadinIcon.TRASH),
    EDIT("edit", VaadinIcon.EDIT),
    EYE("eye", VaadinIcon.EYE),
    GLOBE("globe", VaadinIcon.GLOBE),
    HISTORY("history", VaadinIcon.TIME_BACKWARD),
    HOME("home", VaadinIcon.HOME),
    KEY("key", VaadinIcon.KEY),
    PALETTE("palette", VaadinIcon.PALETTE),
    PAUSE("pause", VaadinIcon.PAUSE),
    PLAY("play", VaadinIcon.PLAY),
    SHIELD("shield", VaadinIcon.SHIELD),
    SHOPPING_CART("shopping-cart", VaadinIcon.CART),
    USERS("users", VaadinIcon.USERS);

    private final String cssValue;
    private final VaadinIcon vaadinIcon;

    AdminIconName(String cssValue, VaadinIcon vaadinIcon) {
        this.cssValue = cssValue;
        this.vaadinIcon = vaadinIcon;
    }

    public String cssValue() {
        return cssValue;
    }

    public VaadinIcon vaadinIcon() {
        return vaadinIcon;
    }
}
