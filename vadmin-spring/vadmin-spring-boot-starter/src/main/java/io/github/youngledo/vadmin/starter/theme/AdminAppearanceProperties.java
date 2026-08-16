package io.github.youngledo.vadmin.starter.theme;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.appearance")
public final class AdminAppearanceProperties implements Serializable {
    private String visualLanguage = "vaadin";

    public AdminVisualLanguage visualLanguage() {
        return AdminVisualLanguage.from(visualLanguage);
    }

    public void setVisualLanguage(String visualLanguage) {
        this.visualLanguage = visualLanguage;
    }

}
