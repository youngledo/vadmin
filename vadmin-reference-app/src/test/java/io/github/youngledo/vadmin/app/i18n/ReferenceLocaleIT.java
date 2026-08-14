package io.github.youngledo.vadmin.app.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.vaadin.flow.i18n.I18NProvider;

@SpringBootTest
@Testcontainers
@ActiveProfiles("development")
class ReferenceLocaleIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private I18NProvider translations;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void resolvesBuiltInModuleMetadataInChineseAndEnglish() {
        assertThat(translations.getTranslation("system.users.title", Locale.forLanguageTag("zh-CN")))
                .isEqualTo("用户");
        assertThat(translations.getTranslation("system.users.title", Locale.forLanguageTag("en-US")))
                .isEqualTo("Users");
    }
}
