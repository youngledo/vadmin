package io.github.youngledo.vadmin.starter.theme;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.appearance")
public final class AdminAppearanceProperties implements Serializable {
    private String visualLanguage = "vaadin";
    private String density = "comfortable";

    public AdminVisualLanguage visualLanguage() {
        return AdminVisualLanguage.from(visualLanguage);
    }

    public AdminDensity density() {
        return AdminDensity.from(density);
    }

    public void setVisualLanguage(String visualLanguage) {
        this.visualLanguage = visualLanguage;
    }

    public void setDensity(String density) {
        this.density = density;
    }
}
