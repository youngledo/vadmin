package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import io.github.youngledo.vadmin.contracts.error.ErrorCode;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

/** Presents successful commands locally while preserving Flow's global failure handling boundary. */
public final class OperationFeedback implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Presenters can be arbitrary lambdas; restore the built-in variants after a Flow session is deserialized.
    private transient Consumer<String> successPresenter;
    private transient Consumer<String> errorPresenter;

    public OperationFeedback() {
        this(OperationFeedback::showSuccess, OperationFeedback::showError);
    }

    public OperationFeedback(Consumer<String> successPresenter) {
        this(successPresenter, OperationFeedback::showError);
    }

    public OperationFeedback(Consumer<String> successPresenter, Consumer<String> errorPresenter) {
        this.successPresenter = Objects.requireNonNull(successPresenter);
        this.errorPresenter = Objects.requireNonNull(errorPresenter);
    }

    public void success(String message) {
        successPresenter.accept(Objects.requireNonNull(message));
    }

    /** Presents a non-blocking warning using the shared operation-feedback placement. */
    public void warning(String message) {
        show(Objects.requireNonNull(message), NotificationVariant.LUMO_WARNING);
    }

    private static void showSuccess(String message) {
        show(message, NotificationVariant.LUMO_SUCCESS);
    }

    public void error(String message) {
        errorPresenter.accept(Objects.requireNonNull(message));
    }

    private static void showError(String message) {
        show(message, NotificationVariant.LUMO_ERROR);
    }

    private static void show(String message, NotificationVariant variant) {
        var notification = Notification.show(message, 5_000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }

    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        successPresenter = OperationFeedback::showSuccess;
        errorPresenter = OperationFeedback::showError;
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
