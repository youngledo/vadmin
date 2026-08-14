package io.github.youngledo.vadmin.springboot.error;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class BusinessFailureAdviceTest {
    @Test
    void returnsProblemJsonForBusinessFailures() {
        var advice = new BusinessFailureAdvice(new ProblemDetailMapper(() -> "request-42"));

        var response = advice.handle(new BusinessFailure(
                ErrorCode.VALIDATION_FAILED, "validation.failed", Map.of("username", "required")));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody().getProperties())
                .containsEntry("correlationId", "request-42")
                .containsEntry("fieldErrors", Map.of("username", "required"));
    }
}
