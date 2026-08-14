package io.github.vaadinadminstarter.flow.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

final class VaadinFlowErrorPresenter implements FlowErrorPresenter {
    @Override
    public void present(FlowError error) {
        var ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        switch (error.presentation()) {
            case FIELD_VALIDATION -> Notification.show(ui.getTranslation("flow.error.validation"));
            case ACCESS_DENIED -> ui.navigate(AccessDeniedView.class);
            case FAILURE -> ui.navigate(SystemFailureView.class);
        }
    }
}
