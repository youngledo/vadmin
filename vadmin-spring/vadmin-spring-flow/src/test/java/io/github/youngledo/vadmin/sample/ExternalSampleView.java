package io.github.youngledo.vadmin.sample;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.Div;

@Component
public final class ExternalSampleView extends Div {
    private final SampleService service;

    public ExternalSampleView(SampleService service) {
        this.service = service;
    }

    public SampleService service() {
        return service;
    }
}
