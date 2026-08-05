package io.github.vaadinadminstarter.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "io.github.vaadinadminstarter")
@EntityScan(basePackages = {"io.github.vaadinadminstarter.springjpa", "io.github.vaadinadminstarter.app.customer"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
