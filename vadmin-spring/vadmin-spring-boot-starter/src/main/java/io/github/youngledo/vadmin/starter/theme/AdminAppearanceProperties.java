package io.github.youngledo.vadmin.starter.theme;

import java.io.Serializable;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.appearance")
public final class AdminAppearanceProperties implements Serializable {
    private String visualLanguage = "vaadin";
    private Integer auraBaseSize;

    public AdminVisualLanguage visualLanguage() {
        return AdminVisualLanguage.from(visualLanguage);
    }

    public Optional<Integer> auraBaseSize() {
        if (auraBaseSize == null || auraBaseSize < 12 || auraBaseSize > 24) {
            return Optional.empty();
        }
        return Optional.of(auraBaseSize);
    }

    public void setVisualLanguage(String visualLanguage) {
        this.visualLanguage = visualLanguage;
    }

    public void setAuraBaseSize(Integer auraBaseSize) {
        this.auraBaseSize = auraBaseSize;
    }
}
