package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.util.Objects;

/** Presents non-blocking administration feedback through Vaadin's native notification component. */
public final class AdminNotifications {
    private static final int SUCCESS_DURATION_MS = 3_000;
    private static final int WARNING_DURATION_MS = 4_000;
    private static final int ERROR_DURATION_MS = 5_000;

    private AdminNotifications() {
    }

    /** Shows a successful operation at the shared top-centre location. */
    public static void success(String message) {
        success(message, SUCCESS_DURATION_MS);
    }

    /** Shows a successful operation at the shared top-centre location for the requested duration. */
    public static void success(String message, int durationMs) {
        show(message, durationMs, NotificationVariant.LUMO_SUCCESS);
    }

    /** Shows a warning at the shared top-centre location. */
    public static void warning(String message) {
        show(message, WARNING_DURATION_MS, NotificationVariant.LUMO_WARNING);
    }

    /** Shows an error at the shared top-centre location. */
    public static void error(String message) {
        show(message, ERROR_DURATION_MS, NotificationVariant.LUMO_ERROR);
    }

    private static void show(String message, int durationMs, NotificationVariant variant) {
        var notification = Notification.show(Objects.requireNonNull(message), durationMs, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
}
