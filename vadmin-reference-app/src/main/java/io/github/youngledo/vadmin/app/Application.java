package io.github.youngledo.vadmin.app;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "io.github.youngledo.vadmin")
@EnableVaadin({
        "io.github.youngledo.vadmin.app",
        "io.github.youngledo.vadmin.flow.error",
        "io.github.youngledo.vadmin.starter",
        "io.github.youngledo.vadmin.springsecurity.ui"
})
@EntityScan(basePackages = "io.github.youngledo.vadmin.springjpa")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
