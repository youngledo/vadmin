package io.github.youngledo.vadmin.starter.brand;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// 定义默认管理外壳显示的产品名称。
///
/// 此配置只表达宿主应用的产品身份，不改变 Vaadin Aura 的视觉语言或组件样式。
@ConfigurationProperties("app.brand")
public final class AdminBrandProperties implements Serializable {
    private String name = "VAdmin";

    public String name() {
        if (name == null || name.isBlank()) {
            return "VAdmin";
        }
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }
}
