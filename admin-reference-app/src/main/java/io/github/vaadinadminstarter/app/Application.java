package io.github.vaadinadminstarter.app;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "io.github.vaadinadminstarter")
@EnableVaadin({
        "io.github.vaadinadminstarter.app",
        "io.github.vaadinadminstarter.flow.error",
        "io.github.vaadinadminstarter.starter",
        "io.github.vaadinadminstarter.springsecurity.ui"
})
@EntityScan(basePackages = "io.github.vaadinadminstarter.springjpa")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
