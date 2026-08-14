package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.notification.Notification;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.error.ErrorCode;
import java.util.Objects;
import java.util.function.Consumer;

/** Presents successful commands locally while preserving Flow's global failure handling boundary. */
public final class OperationFeedback {
    private final Consumer<String> successPresenter;

    public OperationFeedback() {
        this(Notification::show);
    }

    public OperationFeedback(Consumer<String> successPresenter) {
        this.successPresenter = Objects.requireNonNull(successPresenter);
    }

    public void success(String message) {
        successPresenter.accept(Objects.requireNonNull(message));
    }

    /**
     * Delegates only validation failures to the local command surface. Every other exception is
     * rethrown unchanged so Flow's global error handler retains ownership.
     */
    public void handleFailure(Throwable failure, Consumer<BusinessFailure> validationHandler) {
        Objects.requireNonNull(failure);
        if (failure instanceof BusinessFailure businessFailure
                && businessFailure.errorCode() == ErrorCode.VALIDATION_FAILED) {
            Objects.requireNonNull(validationHandler).accept(businessFailure);
            return;
        }
        rethrow(failure);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable failure) throws T {
        throw (T) failure;
    }
}
