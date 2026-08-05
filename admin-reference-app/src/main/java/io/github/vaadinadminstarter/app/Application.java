package io.github.vaadinadminstarter.app;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "io.github.vaadinadminstarter")
@Theme(value = "admin-theme")
@EnableVaadin({
        "io.github.vaadinadminstarter.app",
        "io.github.vaadinadminstarter.flow.error",
        "io.github.vaadinadminstarter.springsecurity.ui"
})
@EntityScan(basePackages = {"io.github.vaadinadminstarter.springjpa", "io.github.vaadinadminstarter.app.customer"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
