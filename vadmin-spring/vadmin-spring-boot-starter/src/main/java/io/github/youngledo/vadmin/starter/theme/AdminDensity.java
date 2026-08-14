package io.github.youngledo.vadmin.starter.theme;

import java.util.Arrays;

public enum AdminDensity {
    COMFORTABLE("comfortable"),
    COMPACT("compact");

    private final String cssValue;

    AdminDensity(String cssValue) {
        this.cssValue = cssValue;
    }

    public static AdminDensity from(String value) {
        var normalized = value == null ? "" : value.trim();
        return Arrays.stream(values())
                .filter(candidate -> candidate.cssValue.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(COMFORTABLE);
    }

    public String cssValue() {
        return cssValue;
    }
}
