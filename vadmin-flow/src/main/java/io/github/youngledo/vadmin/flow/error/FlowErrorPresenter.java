package io.github.youngledo.vadmin.flow.error;

import java.io.Serializable;

@FunctionalInterface
public interface FlowErrorPresenter extends Serializable {
    void present(FlowError error);
}
