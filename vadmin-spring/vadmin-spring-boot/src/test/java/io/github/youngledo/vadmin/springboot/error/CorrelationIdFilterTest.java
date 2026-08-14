package io.github.youngledo.vadmin.springboot.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void preservesAValidInboundCorrelationIdForTheRequestAndResponse() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "request-42");
        var response = new MockHttpServletResponse();
        var captured = new String[1];

        new CorrelationIdFilter().doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> captured[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(captured[0]).isEqualTo("request-42");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("request-42");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void replacesAnInvalidInboundCorrelationIdWithUuid() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "bad id with spaces");
        var response = new MockHttpServletResponse();

        new CorrelationIdFilter().doFilter(request, response, (ignoredRequest, ignoredResponse) -> { });

        assertThat(UUID.fromString(response.getHeader(CorrelationIdFilter.HEADER_NAME))).isNotNull();
    }
}
