package io.github.youngledo.vadmin.flow.error;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.util.Map;

public final class FlowErrorHandler implements ErrorHandler {
    private final FlowErrorMapper mapper;
    private final FlowErrorPresenter presenter;

    public FlowErrorHandler() {
        this(new VaadinFlowErrorPresenter());
    }

    FlowErrorHandler(FlowErrorPresenter presenter) {
        this(new FlowErrorMapper(), presenter);
    }

    FlowErrorHandler(FlowErrorMapper mapper, FlowErrorPresenter presenter) {
        this.mapper = mapper;
        this.presenter = presenter;
    }

    @Override
    public void error(ErrorEvent event) {
        presenter.present(mapper.map(findBusinessFailure(event.getThrowable())));
    }

    private BusinessFailure findBusinessFailure(Throwable throwable) {
        var candidate = throwable;
        while (candidate != null) {
            if (candidate instanceof BusinessFailure failure) {
                return failure;
            }
            candidate = candidate.getCause();
        }
        return new BusinessFailure(ErrorCode.INTERNAL_ERROR, "internal.error", Map.of());
    }
}
