package io.github.vaadinadminstarter.verification.consumer;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EnableVaadin({
        "io.github.vaadinadminstarter.verification.consumer",
        "io.github.vaadinadminstarter.flow.error",
        "io.github.vaadinadminstarter.springsecurity.ui"
})
@EntityScan(basePackages = "io.github.vaadinadminstarter.springjpa")
public class StandaloneConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StandaloneConsumerApplication.class, args);
    }
}
