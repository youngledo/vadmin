package io.github.youngledo.vadmin.starter.shell;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// 定义 VAdmin 默认管理外壳的产品级导航选项。
///
/// 该配置只决定是否显示内置工作台入口，不改变页面布局或 Vaadin Lumo 组件外观。
@ConfigurationProperties("app.shell")
public final class AdminShellProperties implements Serializable {
    private boolean workplaceEnabled = true;

    public boolean workplaceEnabled() {
        return workplaceEnabled;
    }

    public void setWorkplaceEnabled(boolean workplaceEnabled) {
        this.workplaceEnabled = workplaceEnabled;
    }
}
