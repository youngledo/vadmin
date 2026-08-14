package io.github.youngledo.vadmin.flow.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class FlowErrorHandlerTest {
    @Test
    void presentsAnAccessDeniedViewForAuthorizationFailures() {
        var presenter = new CapturingPresenter();
        var handler = new FlowErrorHandler(presenter);

        handler.error(new ErrorEvent(new BusinessFailure(
                ErrorCode.AUTHORIZATION_DENIED, "authorization.denied", Map.of())));

        assertThat(presenter.error.presentation()).isEqualTo(FlowErrorPresentation.ACCESS_DENIED);
        assertThat(presenter.error.status()).isEqualTo(403);
    }

    @Test
    void presentsSystemFailureForUnexpectedExceptions() {
        var presenter = new CapturingPresenter();
        var handler = new FlowErrorHandler(presenter);

        handler.error(new ErrorEvent(new IllegalStateException("unexpected")));

        assertThat(presenter.error.presentation()).isEqualTo(FlowErrorPresentation.FAILURE);
        assertThat(presenter.error.status()).isEqualTo(500);
    }

    @Test
    void registersTheGlobalErrorHandlerThroughVaadinServiceLoader() {
        var listenerTypes = ServiceLoader.load(VaadinServiceInitListener.class).stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertThat(listenerTypes).contains(FlowErrorHandlingServiceInitListener.class);
    }

    private static final class CapturingPresenter implements FlowErrorPresenter {
        private FlowError error;

        @Override
        public void present(FlowError error) {
            this.error = error;
        }
    }
}
