package io.github.vaadinadminstarter.flow.error;

import java.util.Map;

public record FlowError(FlowErrorPresentation presentation, int status, String messageKey,
                        Map<String, String> fieldErrors) {
    public FlowError {
        fieldErrors = Map.copyOf(fieldErrors);
    }
}
