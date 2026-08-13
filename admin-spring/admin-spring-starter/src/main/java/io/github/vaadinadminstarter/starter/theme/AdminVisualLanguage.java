package io.github.vaadinadminstarter.starter.theme;

import java.util.Arrays;

public enum AdminVisualLanguage {
    VAADIN("vaadin"),
    ANT("ant");

    private final String cssValue;

    AdminVisualLanguage(String cssValue) {
        this.cssValue = cssValue;
    }

    public static AdminVisualLanguage from(String value) {
        var normalized = value == null ? "" : value.trim();
        return Arrays.stream(values())
                .filter(candidate -> candidate.cssValue.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(VAADIN);
    }

    public String cssValue() {
        return cssValue;
    }
}
